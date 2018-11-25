package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.ParseError.ErrorType;

import java.util.List;

class JavaCastParser extends AbstractJavaEntityParser
{
	JavaCastParser(JavaParserPool parserPool, ObjectInfo thisInfo) {
		super(parserPool, thisInfo);
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		int position = tokenStream.getPosition();
		JavaToken characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			return new ParseError(position, "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}
		if (characterToken.isContainsCaret()) {
			// nothing we can suggest after '('
			return CompletionSuggestions.NONE;
		}

		ParseResultIF classParseResult = readClass(tokenStream, expectedResultClasses);
		switch (classParseResult.getResultType()) {
			case COMPLETION_SUGGESTIONS:
				// code completion inside "()" => propagate completion suggestions
				return classParseResult;
			case PARSE_ERROR:
			case AMBIGUOUS_PARSE_RESULT:
				// always propagate errors
				return classParseResult;
			case PARSE_RESULT: {
				ParseResult parseResult = (ParseResult) classParseResult;
				int parsedToPosition = parseResult.getParsedToPosition();
				ObjectInfo classInfo = parseResult.getObjectInfo();
				if (!(classInfo.getObject() instanceof Class<?>)) {
					return new ParseError(parsedToPosition, "Internal error: Did not parse class correctly", ErrorType.INTERNAL_ERROR);
				}
				Class<?> targetClass = (Class<?>) classInfo.getObject();

				tokenStream.moveTo(parsedToPosition);

				characterToken = tokenStream.readCharacterUnchecked();
				if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
					return new ParseError(position, "Expected closing parenthesis ')'", ErrorType.SYNTAX_ERROR);
				}
				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after ')'
					return CompletionSuggestions.NONE;
				}

				return parseAndCast(tokenStream, targetClass);
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + classParseResult.getResultType());
		}
	}

	private ParseResultIF parseAndCast(JavaTokenStream tokenStream, Class<?> targetClass) {
		ParseResultIF objectParseResult = parserPool.getExpressionParser().parse(tokenStream, thisInfo, null);
		switch (objectParseResult.getResultType()) {
			case COMPLETION_SUGGESTIONS:
				// code completion after "()" => propagate completion suggestions
				return objectParseResult;
			case PARSE_ERROR:
			case AMBIGUOUS_PARSE_RESULT:
				// always propagate errors
				return objectParseResult;
			case PARSE_RESULT: {
				ParseResult parseResult = (ParseResult) objectParseResult;
				int parsedToPosition = parseResult.getParsedToPosition();
				ObjectInfo objectInfo = parseResult.getObjectInfo();
				tokenStream.moveTo(parsedToPosition);

				try {
					ObjectInfo castInfo = getCastInfo(objectInfo, targetClass);
					return new ParseResult(parsedToPosition, castInfo);
				} catch (ClassCastException e) {
					return new ParseError(tokenStream.getPosition(), "Cannot cast expression to '" + targetClass + "'", ErrorType.SEMANTIC_ERROR, e);
				}
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + objectParseResult.getResultType());
		}

	}
}
