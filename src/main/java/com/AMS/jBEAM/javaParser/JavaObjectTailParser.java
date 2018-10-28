package com.AMS.jBEAM.javaParser;

/**
 * Parses a sub expression following a complete Java expression, assuming the context
 * <ul>
 *     <li>{@code <object>.} or</li>
 *     <li>{@code <object>[]</li>
 * </ul>
 */
class JavaObjectTailParser extends AbstractJavaEntityParser
{
    JavaObjectTailParser(JavaParserSettings parserSettings, Class<?> thisContextClass) {
        super(parserSettings, thisContextClass);
    }

    @Override
    ParseResultIF doParse(JavaTokenStream tokenStream, Class<?> currentContextClass, Class<?> expectedResultClass) {
        if (tokenStream.hasMore()) {
            char nextChar = tokenStream.peekCharacter();
            if (nextChar == '.') {
                return parserSettings.getDotParser(false).parse(tokenStream, currentContextClass, expectedResultClass);
            } else if (nextChar == '[') {
                Class<?> elementClass = currentContextClass.getComponentType();
                if (elementClass == null) {
                    // no array type
                    return new ParseError(tokenStream.getPosition(), "Cannot apply [] to non-array types");
                }
                ParseResultIF arrayIndexParseResult = parserSettings.getArrayAccessParser().parse(tokenStream, thisContextClass, int.class);
                switch (arrayIndexParseResult.getResultType()) {
                    case COMPLETION_SUGGESTIONS:
                        // code completion inside "[]" => propagate completion suggestions
                        return arrayIndexParseResult;
                    case PARSE_ERROR:
                        // always propagate errors
                        return arrayIndexParseResult;
                    case PARSE_RESULT: {
                        int parsedToPosition = ((ParseResult) arrayIndexParseResult).getParsedToPosition();
                        tokenStream.moveTo(parsedToPosition);
                        return parserSettings.getObjectTailParser().parse(tokenStream, elementClass, expectedResultClass);
                    }
                    default:
                        throw new IllegalStateException("Unsupported parse result type: " + arrayIndexParseResult.getResultType());
                }
            }
        }
        // finished parsing

		if (expectedResultClass != null && !ParseUtils.isConvertibleTo(currentContextClass, expectedResultClass)) {
			return new ParseError(tokenStream.getPosition(), "The class '" + currentContextClass.getSimpleName() + "' cannot be casted to the expected class '" + expectedResultClass.getSimpleName() + "");
		}

		// TODO: Determine real object when doing real parsing
        return new ParseResult(tokenStream.getPosition(), new ObjectInfo(null, currentContextClass));
    }
}
