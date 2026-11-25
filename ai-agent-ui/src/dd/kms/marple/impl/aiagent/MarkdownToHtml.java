package dd.kms.marple.impl.aiagent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal Markdown to HTML converter for the chat transcript. It supports the subset that is
 * commonly used in agent answers and that can be rendered by Swing's HTML 3.2 renderer:
 * fenced code blocks, inline code, bold, italic, links, headings, bullet and numbered lists,
 * simple tables and paragraphs. Everything else is rendered as escaped text.
 */
class MarkdownToHtml
{
	private static final Pattern	HEADING			= Pattern.compile("^(#{1,6})\\s+(.*)$");
	private static final Pattern	BULLET_ITEM		= Pattern.compile("^\\s*[-*+]\\s+(.*)$");
	private static final Pattern	NUMBERED_ITEM	= Pattern.compile("^\\s*\\d+[.)]\\s+(.*)$");
	private static final Pattern	TABLE_SEPARATOR	= Pattern.compile("^\\s*\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)*\\|?\\s*$");
	private static final Pattern	BOLD			= Pattern.compile("\\*\\*(.+?)\\*\\*|__(.+?)__");
	private static final Pattern	ITALIC			= Pattern.compile("(?<![\\w*])\\*(?!\\s)(.+?)(?<!\\s)\\*(?![\\w*])");
	private static final Pattern	LINK			= Pattern.compile("\\[([^\\]]+)]\\(([^)\\s]+)\\)");
	private static final Pattern	CODE_SPAN		= Pattern.compile("`([^`]+)`");
	private static final String		PLACEHOLDER_DELIMITER	= "\u0000";
	private static final Pattern	PLACEHOLDER		= Pattern.compile("\u0000(\\d+)\u0000");

	static String convert(String markdown) {
		return new MarkdownToHtml(markdown).doConvert();
	}

	/**
	 * The lines that have not been processed yet. Each block parser consumes the lines of its block.
	 */
	private final Queue<String>	lines;
	private final StringBuilder	html	= new StringBuilder();

	private MarkdownToHtml(String markdown) {
		String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
		this.lines = new ArrayDeque<>(Arrays.asList(lines));
	}

	private String doConvert() {
		String line;
		while ((line = lines.peek()) != null) {
			if (isBlank(line)) {
				// blank lines only separate blocks
				lines.remove();
			} else if (isCodeFence(line)) {
				parseCodeBlock();
			} else if (isTableRow(line)) {
				parseTable();
			} else if (isHeading(line)) {
				parseHeading();
			} else if (isBulletItem(line)) {
				parseBulletItemList();
			} else if (isNumberedItem(line)) {
				parseNumberedItemList();
			} else {
				parseParagraph();
			}
		}
		return html.toString();
	}

	/**
	 * Parses a fenced code block. A code block that is not closed ends with the last line.
	 */
	private void parseCodeBlock() {
		lines.remove();
		html.append("<pre>");
		while (nextLineMatches(line -> !isCodeFence(line))) {
			html.append(escape(lines.remove())).append("\n");
		}
		lines.poll();
		html.append("</pre>");
	}

	private void parseTable() {
		List<String> rows = new ArrayList<>();
		while (nextLineMatches(MarkdownToHtml::isTableRow)) {
			rows.add(lines.remove().trim());
		}
		html.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">");
		boolean headerRow = rows.size() > 1 && TABLE_SEPARATOR.matcher(rows.get(1)).matches();
		for (int i = 0; i < rows.size(); i++) {
			String row = rows.get(i);
			if (TABLE_SEPARATOR.matcher(row).matches()) {
				continue;
			}
			String cellTag = headerRow && i == 0 ? "th" : "td";
			html.append("<tr>");
			for (String cell : splitTableRow(row)) {
				html.append("<").append(cellTag).append(">").append(formatInline(cell.trim())).append("</").append(cellTag).append(">");
			}
			html.append("</tr>");
		}
		html.append("</table>");
	}

	private void parseHeading() {
		Matcher matcher = HEADING.matcher(lines.remove().trim());
		matcher.matches();
		int level = Math.min(matcher.group(1).length() + 1, 4);
		html.append("<h").append(level).append(">").append(formatInline(matcher.group(2))).append("</h").append(level).append(">");
	}

	private void parseBulletItemList() {
		parseList("ul", BULLET_ITEM);
	}

	private void parseNumberedItemList() {
		parseList("ol", NUMBERED_ITEM);
	}

	/**
	 * Parses a list whose items match {@code itemPattern}. Indented lines that do not start a new
	 * block continue the previous item; if they are separated from it by blank lines, they start a
	 * new paragraph within the item. Blank lines between items do not end the list. Nested lists
	 * are not supported: An indented item of another list type ends the list.
	 */
	private void parseList(String listTag, Pattern itemPattern) {
		Predicate<String> isItem = line -> itemPattern.matcher(line).matches();
		html.append("<").append(listTag).append(">");
		String nextNonBlankLine;
		while ((nextNonBlankLine = peekFirstNonBlankLine()) != null && isItem.test(nextNonBlankLine)) {
			removeLeadingBlankLines();
			Matcher matcher = itemPattern.matcher(lines.remove());
			matcher.matches();
			html.append("<li>").append(formatInline(matcher.group(1)));
			while ((nextNonBlankLine = peekFirstNonBlankLine()) != null && isListItemContinuation(nextNonBlankLine)) {
				String separator = nextLineMatches(MarkdownToHtml::isBlank) ? "<br><br>" : " ";
				removeLeadingBlankLines();
				html.append(separator).append(formatInline(lines.remove().trim()));
			}
			html.append("</li>");
		}
		html.append("</").append(listTag).append(">");
	}

	/**
	 * Parses a paragraph. Its lines are separated by line breaks.
	 */
	private void parseParagraph() {
		html.append("<p>").append(formatInline(lines.remove().trim()));
		while (nextLineMatches(MarkdownToHtml::isParagraphLine)) {
			html.append("<br>").append(formatInline(lines.remove().trim()));
		}
		html.append("</p>");
	}

	private boolean nextLineMatches(Predicate<String> condition) {
		String line = lines.peek();
		return line != null && condition.test(line);
	}

	/**
	 * Returns the first non-blank line that has not been processed yet without removing any line,
	 * or {@code null} if there is no such line.
	 */
	private String peekFirstNonBlankLine() {
		for (String line : lines) {
			if (!isBlank(line)) {
				return line;
			}
		}
		return null;
	}

	private void removeLeadingBlankLines() {
		while (nextLineMatches(MarkdownToHtml::isBlank)) {
			lines.remove();
		}
	}

	private static boolean isBlank(String line) {
		return line.trim().isEmpty();
	}

	private static boolean isCodeFence(String line) {
		return line.trim().startsWith("```");
	}

	private static boolean isTableRow(String line) {
		return line.trim().startsWith("|");
	}

	private static boolean isHeading(String line) {
		return HEADING.matcher(line.trim()).matches();
	}

	private static boolean isListItem(String line) {
		return isBulletItem(line) || isNumberedItem(line);
	}

	private static boolean isBulletItem(String line) {
		return BULLET_ITEM.matcher(line).matches();
	}

	private static boolean isNumberedItem(String line) {
		return NUMBERED_ITEM.matcher(line).matches();
	}

	private static boolean startsBlock(String line) {
		return isCodeFence(line) || isTableRow(line) || isHeading(line) || isListItem(line);
	}

	private static boolean isParagraphLine(String line) {
		return !isBlank(line) && !startsBlock(line);
	}

	private static boolean isListItemContinuation(String line) {
		return isParagraphLine(line) && Character.isWhitespace(line.charAt(0));
	}

	private static List<String> splitTableRow(String row) {
		String content = row.trim();
		if (content.startsWith("|")) {
			content = content.substring(1);
		}
		if (content.endsWith("|")) {
			content = content.substring(0, content.length() - 1);
		}
		List<String> cells = new ArrayList<>();
		StringBuilder cell = new StringBuilder();
		boolean inCode = false;
		for (char c : content.toCharArray()) {
			if (c == '`') {
				inCode = !inCode;
			}
			if (c == '|' && !inCode) {
				cells.add(cell.toString());
				cell.setLength(0);
			} else {
				cell.append(c);
			}
		}
		cells.add(cell.toString());
		return cells;
	}

	/**
	 * Formats inline elements. Text within backticks is rendered as code and not formatted further.
	 */
	static String formatInline(String text) {
		// replace code spans by placeholders such that emphasis around code spans (e.g., **`code`**) works
		List<String> codeSpans = new ArrayList<>();
		Matcher codeMatcher = CODE_SPAN.matcher(text.replace(PLACEHOLDER_DELIMITER, ""));
		StringBuffer textWithPlaceholders = new StringBuffer();
		while (codeMatcher.find()) {
			codeMatcher.appendReplacement(textWithPlaceholders, Matcher.quoteReplacement(PLACEHOLDER_DELIMITER + codeSpans.size() + PLACEHOLDER_DELIMITER));
			codeSpans.add("<code>" + escape(codeMatcher.group(1)) + "</code>");
		}
		codeMatcher.appendTail(textWithPlaceholders);

		String formatted = formatEmphasis(escape(textWithPlaceholders.toString()));

		Matcher placeholderMatcher = PLACEHOLDER.matcher(formatted);
		StringBuffer result = new StringBuffer();
		while (placeholderMatcher.find()) {
			int index = Integer.parseInt(placeholderMatcher.group(1));
			placeholderMatcher.appendReplacement(result, Matcher.quoteReplacement(codeSpans.get(index)));
		}
		placeholderMatcher.appendTail(result);
		return result.toString();
	}

	private static String formatEmphasis(String escapedText) {
		String text = LINK.matcher(escapedText).replaceAll("<a href=\"$2\">$1</a>");
		Matcher boldMatcher = BOLD.matcher(text);
		StringBuffer buffer = new StringBuffer();
		while (boldMatcher.find()) {
			String content = boldMatcher.group(1) != null ? boldMatcher.group(1) : boldMatcher.group(2);
			boldMatcher.appendReplacement(buffer, Matcher.quoteReplacement("<b>" + content + "</b>"));
		}
		boldMatcher.appendTail(buffer);
		return ITALIC.matcher(buffer.toString()).replaceAll("<i>$1</i>");
	}

	static String escape(String text) {
		StringBuilder escaped = new StringBuilder(text.length());
		for (char c : text.toCharArray()) {
			switch (c) {
				case '&':	escaped.append("&amp;");	break;
				case '<':	escaped.append("&lt;");		break;
				case '>':	escaped.append("&gt;");		break;
				case '"':	escaped.append("&quot;");	break;
				default:	escaped.append(c);			break;
			}
		}
		return escaped.toString();
	}
}
