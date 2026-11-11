package dd.kms.marple.impl.aiagent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal Markdown to HTML converter for the chat transcript. It supports the subset that is
 * commonly used in agent answers and that can be rendered by Swing's HTML 3.2 renderer:
 * fenced code blocks, inline code, bold, italic, links, headings, bullet and numbered lists,
 * simple tables and paragraphs. Everything else is rendered as escaped text.
 */
final class MarkdownToHtml
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
		return new MarkdownToHtml().doConvert(markdown);
	}

	private final StringBuilder	html			= new StringBuilder();
	private final StringBuilder	paragraph		= new StringBuilder();
	private final List<String>	tableRows		= new ArrayList<>();
	private String				openListTag		= null;
	private boolean				inCodeBlock		= false;

	private MarkdownToHtml() {}

	private String doConvert(String markdown) {
		String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
		for (String line : lines) {
			processLine(line);
		}
		if (inCodeBlock) {
			html.append("</pre>");
		}
		flushAll();
		return html.toString();
	}

	private void processLine(String line) {
		if (line.trim().startsWith("```")) {
			if (inCodeBlock) {
				html.append("</pre>");
				inCodeBlock = false;
			} else {
				flushAll();
				html.append("<pre>");
				inCodeBlock = true;
			}
			return;
		}
		if (inCodeBlock) {
			html.append(escape(line)).append("\n");
			return;
		}
		String trimmedLine = line.trim();
		if (trimmedLine.isEmpty()) {
			flushAll();
			return;
		}
		if (trimmedLine.startsWith("|")) {
			flushParagraph();
			closeList();
			tableRows.add(trimmedLine);
			return;
		}
		flushTable();

		Matcher matcher = HEADING.matcher(trimmedLine);
		if (matcher.matches()) {
			flushAll();
			int level = Math.min(matcher.group(1).length() + 1, 4);
			html.append("<h").append(level).append(">").append(formatInline(matcher.group(2))).append("</h").append(level).append(">");
			return;
		}
		matcher = BULLET_ITEM.matcher(line);
		if (matcher.matches()) {
			addListItem("ul", matcher.group(1));
			return;
		}
		matcher = NUMBERED_ITEM.matcher(line);
		if (matcher.matches()) {
			addListItem("ol", matcher.group(1));
			return;
		}
		if (openListTag != null && Character.isWhitespace(line.charAt(0))) {
			// continuation of a list item
			html.append(" ").append(formatInline(trimmedLine));
			return;
		}
		closeList();
		if (paragraph.length() > 0) {
			paragraph.append("<br>");
		}
		paragraph.append(formatInline(trimmedLine));
	}

	private void addListItem(String listTag, String content) {
		flushParagraph();
		if (!listTag.equals(openListTag)) {
			closeList();
			html.append("<").append(listTag).append(">");
			openListTag = listTag;
		}
		html.append("<li>").append(formatInline(content));
	}

	private void flushAll() {
		flushParagraph();
		closeList();
		flushTable();
	}

	private void flushParagraph() {
		if (paragraph.length() > 0) {
			html.append("<p>").append(paragraph).append("</p>");
			paragraph.setLength(0);
		}
	}

	private void closeList() {
		if (openListTag != null) {
			html.append("</").append(openListTag).append(">");
			openListTag = null;
		}
	}

	private void flushTable() {
		if (tableRows.isEmpty()) {
			return;
		}
		html.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">");
		boolean headerRow = tableRows.size() > 1 && TABLE_SEPARATOR.matcher(tableRows.get(1)).matches();
		for (int i = 0; i < tableRows.size(); i++) {
			String row = tableRows.get(i);
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
		tableRows.clear();
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
