package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.common.RegexUtils;
import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.result.ParseError;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.settings.ObjectTreeNodeIF;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.TypeInfo;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class CustomHierarchyParser extends AbstractEntityParser<ObjectInfo>
{
	private static final char		HIERARCHY_BEGIN		= '{';
	private static final char		HIERARCHY_SEPARATOR	= '#';
	private static final char		HIERARCHY_END		= '}';

	private static final Pattern	HIERARCHY_NODE_PATTERN	= Pattern.compile("^([^" + RegexUtils.escapeIfSpecial(HIERARCHY_SEPARATOR) + RegexUtils.escapeIfSpecial(HIERARCHY_END) + "]*).*");

	public CustomHierarchyParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		if (tokenStream.isCaretAtPosition()) {
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != HIERARCHY_BEGIN) {
			tokenStream.moveTo(position);
			log(LogLevel.ERROR, "missing '" + HIERARCHY_BEGIN + "' at " + tokenStream);
			return new ParseError(position, "Expected hierarchy begin character ('" + HIERARCHY_BEGIN + "')", ParseError.ErrorType.WRONG_PARSER);
		}

		ObjectTreeNodeIF hierarchyNode = parserToolbox.getSettings().getCustomHierarchyRoot();
		return parseHierarchyNode(tokenStream, hierarchyNode, expectation);
	}

	private ParseResultIF parseHierarchyNode(TokenStream tokenStream, ObjectTreeNodeIF contextNode, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		if (tokenStream.isCaretAtPosition()) {
			log(LogLevel.INFO, "suggesting custom hierarchy nodes for completion...");
			return parserToolbox.getObjectTreeNodeDataProvider().suggestNodes("", contextNode, startPosition, startPosition);
		}

		Token nodeToken = tokenStream.readRegexUnchecked(HIERARCHY_NODE_PATTERN, 1);
		if (nodeToken == null) {
			log(LogLevel.ERROR, "missing hierarchy node name at " + tokenStream);
			return new ParseError(startPosition, "Expected a hierarchy node name", ParseError.ErrorType.WRONG_PARSER);
		}
		String nodeName = nodeToken.getValue();
		int endPosition = tokenStream.getPosition();

		// check for code completion
		if (nodeToken.isContainsCaret()) {
			log(LogLevel.SUCCESS, "suggesting hierarchy nodes matching '" + nodeName + "'");
			return parserToolbox.getObjectTreeNodeDataProvider().suggestNodes(nodeName, contextNode, startPosition, endPosition);
		}

		List<ObjectTreeNodeIF> childNodes = contextNode.getChildNodes();
		Optional<ObjectTreeNodeIF> firstChildNodeMatch = childNodes.stream().filter(node -> node.getName().equals(nodeName)).findFirst();
		if (!firstChildNodeMatch.isPresent()) {
			log(LogLevel.ERROR, "unknown hierarchy node '" + nodeName + "'");
			return new ParseError(startPosition, "Unknown hierarchy node '" + nodeName + "'", ParseError.ErrorType.SEMANTIC_ERROR);
		}
		log(LogLevel.SUCCESS, "detected hierarchy node '" + nodeName + "'");

		ObjectTreeNodeIF childNode = firstChildNodeMatch.get();

		Token characterToken = tokenStream.readCharacterUnchecked();
		char character = characterToken == null ? (char) 0 : characterToken.toString().charAt(0);

		if (character == HIERARCHY_SEPARATOR) {
			return parseHierarchyNode(tokenStream, childNode, expectation);
		} else if (character == HIERARCHY_END) {
			Object userObject = childNode.getUserObject();
			ObjectInfo userObjectInfo = new ObjectInfo(userObject, TypeInfo.UNKNOWN);
			return parserToolbox.getObjectTailParser().parse(tokenStream, userObjectInfo, expectation);
		}

		log(LogLevel.ERROR, "expected '" + HIERARCHY_SEPARATOR + "' or '" + HIERARCHY_END + "'");
		return new ParseError(endPosition, "Expected hierarchy separator ('" + HIERARCHY_SEPARATOR + "') or hierarchy end character ('" + HIERARCHY_END + "')", ParseError.ErrorType.SEMANTIC_ERROR);
	}
}
