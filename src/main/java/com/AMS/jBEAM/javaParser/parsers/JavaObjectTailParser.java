package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.JavaParserContext;
import com.AMS.jBEAM.javaParser.result.CompletionSuggestions;
import com.AMS.jBEAM.javaParser.result.ParseError;
import com.AMS.jBEAM.javaParser.result.ParseResult;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.JavaToken;
import com.AMS.jBEAM.javaParser.tokenizer.JavaTokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;

/**
 * Parses a sub expression following a complete Java expression, assuming the context
 * <ul>
 *     <li>{@code <object>.} or</li>
 *     <li>{@code <object>[]</li>
 * </ul>
 */
public class JavaObjectTailParser extends AbstractJavaEntityParser
{
	public JavaObjectTailParser(JavaParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();
		Class<?> currentContextClass = parserContext.getObjectInfoProvider().getClass(currentContextInfo);
		if (tokenStream.hasMore()) {
			char nextChar = tokenStream.peekCharacter();
			if (nextChar == '.') {
				return parseDot(tokenStream, currentContextInfo, expectedResultClasses);
			} else if (nextChar == '[') {
				Class<?> elementClass = currentContextClass.getComponentType();
				if (elementClass == null) {
					// no array type
					return new ParseError(tokenStream.getPosition(), "Cannot apply [] to non-array types", ErrorType.SEMANTIC_ERROR);
				}
				ParseResultIF arrayIndexParseResult = parseArrayIndex(tokenStream);
				switch (arrayIndexParseResult.getResultType()) {
					case COMPLETION_SUGGESTIONS:
						// code completion inside "[]" => propagate completion suggestions
						return arrayIndexParseResult;
					case PARSE_ERROR:
					case AMBIGUOUS_PARSE_RESULT:
						// always propagate errors
						return arrayIndexParseResult;
					case PARSE_RESULT: {
						ParseResult parseResult = (ParseResult) arrayIndexParseResult;
						int parsedToPosition = parseResult.getParsedToPosition();
						ObjectInfo indexInfo = parseResult.getObjectInfo();
						ObjectInfo elementInfo;
						try {
							elementInfo = parserContext.getObjectInfoProvider().getArrayElementInfo(currentContextInfo, indexInfo);
						} catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
							return new ParseError(startPosition, e.getClass().getSimpleName() + " during array index evaluation", ErrorType.EVALUATION_EXCEPTION, e);
						}
						tokenStream.moveTo(parsedToPosition);
						return parserContext.getObjectTailParser().parse(tokenStream, elementInfo, expectedResultClasses);
					}
					default:
						throw new IllegalStateException("Unsupported parse result type: " + arrayIndexParseResult.getResultType());
				}
			}
		}
		// finished parsing
		if (expectedResultClasses != null && expectedResultClasses.stream().noneMatch(expectedResultClass -> ParseUtils.isConvertibleTo(currentContextClass, expectedResultClass))) {
			String messagePrefix = "The class '" + currentContextClass.getSimpleName() + "' is not assignable to ";
			String messageMiddle = expectedResultClasses.size() > 1
									? "any of the expected classes "
									: "the expected class ";
			String messageSuffix = "'" + expectedResultClasses.stream().map(clazz -> clazz.getSimpleName()).collect(Collectors.joining("', '")) + "'";

			return new ParseError(tokenStream.getPosition(), messagePrefix + messageMiddle + messageSuffix, ErrorType.SEMANTIC_ERROR);
		}

		return new ParseResult(tokenStream.getPosition(), currentContextInfo);
	}

	private ParseResultIF parseDot(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		JavaToken characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals(".");
		if (characterToken.isContainsCaret()) {
			int insertionBegin = tokenStream.getPosition();
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (JavaTokenStream.JavaTokenParseException e) {
				insertionEnd = insertionBegin;
			}
			return parserContext.getFieldAndMethodDataProvider().suggestFieldsAndMethods(currentContextInfo, expectedResultClasses, insertionBegin, insertionEnd);
		}

		return ParseUtils.parse(tokenStream, currentContextInfo, expectedResultClasses,
				parserContext.getFieldParser(false),
				parserContext.getMethodParser(false)
		);
	}

	private ParseResultIF parseArrayIndex(JavaTokenStream tokenStream) {
		List<Class<?>> expectedResultClasses = Arrays.asList(int.class);
		JavaToken characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");
		if (characterToken.isContainsCaret()) {
			return parserContext.getFieldAndMethodDataProvider().suggestFieldsAndMethods(tokenStream, expectedResultClasses);
		}

		ParseResultIF arrayIndexParseResult = parserContext.getExpressionParser().parse(tokenStream, thisInfo, expectedResultClasses);
		switch (arrayIndexParseResult.getResultType()) {
			case COMPLETION_SUGGESTIONS:
				// code completion inside "[]" => propagate completion suggestions
				return arrayIndexParseResult;
			case PARSE_ERROR:
			case AMBIGUOUS_PARSE_RESULT:
				// always propagate errors
				return arrayIndexParseResult;
			case PARSE_RESULT: {
				ParseResult parseResult = ((ParseResult) arrayIndexParseResult);
				int parsedToPosition = parseResult.getParsedToPosition();

				tokenStream.moveTo(parsedToPosition);
				characterToken = tokenStream.readCharacterUnchecked();

				if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
					return new ParseError(parsedToPosition, "Expected closing bracket ']'", ErrorType.SYNTAX_ERROR);
				}

				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after ']'
					return CompletionSuggestions.NONE;
				}

				// delegate parse result with corrected position (includes ']')
				return new ParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + arrayIndexParseResult.getResultType());
		}
	}
}
