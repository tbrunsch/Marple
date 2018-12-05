package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;

/**
 * Parses a sub expression starting with a field {@code <field>}, assuming the context
 * <ul>
 *     <li>{@code <context instance>.<field>},</li>
 *     <li>{@code <context class>.<field>}, or</li>
 *     <li>{@code <field>} (like {@code <context instance>.<field>} for {@code <context instance> = this})</li>
 * </ul>
 */
public class FieldParser extends AbstractEntityParser
{
	private final boolean staticOnly;

	public FieldParser(ParserContext parserContext, ObjectInfo thisInfo, boolean staticOnly) {
		super(parserContext, thisInfo);
		this.staticOnly = staticOnly;
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		int startPosition = tokenStream.getPosition();

		if (thisInfo.getObject() == null && !staticOnly) {
			return new ParseError(startPosition, "Null object does not have any fields", ErrorType.WRONG_PARSER);
		}

		Token fieldNameToken;
		try {
			fieldNameToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			return new ParseError(startPosition, "Expected an identifier", ErrorType.WRONG_PARSER);
		}
		String fieldName = fieldNameToken.getValue();
		int endPosition = tokenStream.getPosition();

		Class<?> currentContextClass = parserContext.getObjectInfoProvider().getClass(currentContextInfo);
		List<Field> fields = parserContext.getInspectionDataProvider().getFields(currentContextClass, staticOnly);

		// check for code completion
		if (fieldNameToken.isContainsCaret()) {
			Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
				fields,
				field -> new CompletionSuggestionField(field, startPosition, endPosition),
				ParseUtils.rateFieldByNameAndClassesFunc(fieldName, expectedResultClasses)
			);
			return new CompletionSuggestions(ratedSuggestions);
		}

		if (tokenStream.hasMore() && tokenStream.peekCharacter() == '(') {
			return new ParseError(tokenStream.getPosition() + 1, "Unexpected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		// no code completion requested => field name must exist
		Optional<Field> firstFieldMatch = fields.stream().filter(field -> field.getName().equals(fieldName)).findFirst();
		if (!firstFieldMatch.isPresent()) {
			return new ParseError(startPosition, "Unknown field '" + fieldName + "'", ErrorType.SEMANTIC_ERROR);
		}

		Field matchingField = firstFieldMatch.get();
		ObjectInfo matchingFieldInfo = parserContext.getObjectInfoProvider().getFieldInfo(currentContextInfo, matchingField);

		return parserContext.getTailParser(false).parse(tokenStream, matchingFieldInfo, expectedResultClasses);
	}
}
