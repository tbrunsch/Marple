package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserToolbox;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.result.ParseError;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.wrappers.FieldInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;

import java.util.List;
import java.util.Optional;

import static com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;

abstract class AbstractFieldParser<C> extends AbstractEntityParser<C>
{
	AbstractFieldParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	abstract boolean contextCausesNullPointerException(C context);
	abstract Object getContextObject(C context);
	abstract List<FieldInfo> getFieldInfos(C context);

	@Override
	ParseResultIF doParse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();

		if (contextCausesNullPointerException(context)) {
			log(LogLevel.ERROR, "null pointer exception");
			return new ParseError(startPosition, "Null pointer exception", ErrorType.WRONG_PARSER);
		}

		if (tokenStream.isCaretAtPosition()) {
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				insertionEnd = startPosition;
			}
			log(LogLevel.INFO, "suggesting fields for completion...");
			return suggestFields("", context, expectation, startPosition, insertionEnd);
		}

		Token fieldNameToken;
		try {
			fieldNameToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "missing field name at " + tokenStream);
			return new ParseError(startPosition, "Expected an identifier", ErrorType.WRONG_PARSER);
		}
		String fieldName = fieldNameToken.getValue();
		int endPosition = tokenStream.getPosition();

		// check for code completion
		if (fieldNameToken.isContainsCaret()) {
			log(LogLevel.SUCCESS, "suggesting fields matching '" + fieldName + "'");
			return suggestFields(fieldName, context, expectation, startPosition, endPosition);
		}

		if (tokenStream.hasMore() && tokenStream.peekCharacter() == '(') {
			log(LogLevel.ERROR, "unexpected '(' at " + tokenStream);
			return new ParseError(tokenStream.getPosition() + 1, "Unexpected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		// no code completion requested => field name must exist
		List<FieldInfo> fieldInfos = getFieldInfos(context);
		Optional<FieldInfo> firstFieldInfoMatch = fieldInfos.stream().filter(fieldInfo -> fieldInfo.getName().equals(fieldName)).findFirst();
		if (!firstFieldInfoMatch.isPresent()) {
			log(LogLevel.ERROR, "unknown field '" + fieldName + "'");
			return new ParseError(startPosition, "Unknown field '" + fieldName + "'", ErrorType.SEMANTIC_ERROR);
		}
		log(LogLevel.SUCCESS, "detected field '" + fieldName + "'");

		FieldInfo fieldInfo = firstFieldInfoMatch.get();
		Object contextObject = getContextObject(context);
		ObjectInfo matchingFieldInfo = parserToolbox.getObjectInfoProvider().getFieldValueInfo(contextObject, fieldInfo);

		return parserToolbox.getObjectTailParser().parse(tokenStream, matchingFieldInfo, expectation);
	}

	private CompletionSuggestions suggestFields(String expectedName, C context, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		Object contextObject = getContextObject(context);
		return parserToolbox.getFieldDataProvider().suggestFields(expectedName, contextObject, getFieldInfos(context), expectation, insertionBegin, insertionEnd);
	}
}
