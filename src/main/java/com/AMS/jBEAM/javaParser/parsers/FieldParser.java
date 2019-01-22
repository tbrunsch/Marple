package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.FieldInfo;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.google.common.reflect.TypeToken;

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
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes) {
		int startPosition = tokenStream.getPosition();

		if (tokenStream.isCaretAtPosition()) {
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				insertionEnd = startPosition;
			}
			return parserContext.getFieldDataProvider().suggestFields(currentContextInfo, expectedResultTypes, startPosition, insertionEnd, staticOnly);
		}

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

		TypeToken<?> currentContextType = parserContext.getObjectInfoProvider().getType(currentContextInfo);
		List<FieldInfo> fieldInfos = parserContext.getInspectionDataProvider().getFieldInfos(currentContextType, staticOnly);

		// check for code completion
		if (fieldNameToken.isContainsCaret()) {
			Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
				fieldInfos,
				fieldInfo -> new CompletionSuggestionField(fieldInfo, startPosition, endPosition),
				ParseUtils.rateFieldByNameAndTypesFunc(fieldName, expectedResultTypes)
			);
			return new CompletionSuggestions(ratedSuggestions);
		}

		if (tokenStream.hasMore() && tokenStream.peekCharacter() == '(') {
			return new ParseError(tokenStream.getPosition() + 1, "Unexpected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		// no code completion requested => field name must exist
		Optional<FieldInfo> firstFieldInfoMatch = fieldInfos.stream().filter(fieldInfo -> fieldInfo.getName().equals(fieldName)).findFirst();
		if (!firstFieldInfoMatch.isPresent()) {
			return new ParseError(startPosition, "Unknown field '" + fieldName + "'", ErrorType.SEMANTIC_ERROR);
		}

		FieldInfo fieldInfo = firstFieldInfoMatch.get();
		ObjectInfo matchingFieldInfo = parserContext.getObjectInfoProvider().getFieldInfo(currentContextInfo, fieldInfo);

		return parserContext.getTailParser(false).parse(tokenStream, matchingFieldInfo, expectedResultTypes);
	}
}
