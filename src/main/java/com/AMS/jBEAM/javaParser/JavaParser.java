package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.debug.ParserLogEntry;
import com.AMS.jBEAM.javaParser.parsers.ParseExpectation;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.settings.ParseMode;
import com.AMS.jBEAM.javaParser.settings.ParserSettings;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;

import java.util.*;

import static com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;

public class JavaParser
{
	private static final Comparator<CompletionSuggestionIF> SUGGESTION_COMPARATOR_BY_CLASS = new Comparator<CompletionSuggestionIF>() {
		@Override
		public int compare(CompletionSuggestionIF suggestion1, CompletionSuggestionIF suggestion2) {
			Class<? extends CompletionSuggestionIF> suggestionClass1 = suggestion1.getClass();
			Class<? extends CompletionSuggestionIF> suggestionClass2 = suggestion2.getClass();
			if (suggestionClass1 == suggestionClass2) {
				return 0;
			}
			// Prefer variables over fields over methods
			return	suggestionClass1 == CompletionSuggestionVariable.class	? -1 :
					suggestionClass1 == CompletionSuggestionField.class		? (suggestionClass2 == CompletionSuggestionVariable.class ? 1 : -1)
																			: 1;
		}
	};

	public List<CompletionSuggestionIF> suggestCodeCompletion(String javaExpression, ParserSettings settings, int caret, Object valueOfThis) throws ParseException {
		ParseResultIF parseResult = parse(javaExpression, settings, ParseMode.CODE_COMPLETION, caret, valueOfThis);

		switch (parseResult.getResultType()) {
			case OBJECT_PARSE_RESULT: {
				if (parseResult.getPosition() != javaExpression.length()) {
					throw new ParseException(parseResult.getPosition(), "Unexpected character");
				} else {
					throw new IllegalStateException("Internal error: No completions available");
				}
			}
			case CLASS_PARSE_RESULT: {
				throw new IllegalStateException("Internal error: Class parse results should have been transformed to ParseErrors in AbstractEntityParser.parse()");
			}
			case PARSE_ERROR: {
				ParseError error = (ParseError) parseResult;
				throw new ParseException(error.getPosition(), error.getMessage());
			}
			case AMBIGUOUS_PARSE_RESULT: {
				AmbiguousParseResult result = (AmbiguousParseResult) parseResult;
				throw new ParseException(result.getPosition(), result.getMessage());
			}
			case COMPLETION_SUGGESTIONS: {
				CompletionSuggestions completionSuggestions = (CompletionSuggestions) parseResult;
				Map<CompletionSuggestionIF, Integer> ratedSuggestions = completionSuggestions.getRatedSuggestions();
				List<CompletionSuggestionIF> sortedSuggestions = new ArrayList<>(ratedSuggestions.keySet());
				Collections.sort(sortedSuggestions, SUGGESTION_COMPARATOR_BY_CLASS);
				Collections.sort(sortedSuggestions, Comparator.comparingInt(ratedSuggestions::get));
				return sortedSuggestions;
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + parseResult.getResultType());
		}
	}

	public Object evaluate(String javaExpression, ParserSettings settings, Object valueOfThis) throws ParseException {
		ParseResultIF parseResult;

		if (!settings.isEnableDynamicTyping()) {
			// First iteration without evaluation to avoid side effects when errors occur
			parseResult = parse(javaExpression, settings, ParseMode.WITHOUT_EVALUATION,-1, valueOfThis);
			if (parseResult.getResultType() == ParseResultType.OBJECT_PARSE_RESULT) {
				// Second iteration with evaluation (side effects cannot be avoided)
				parseResult = parse(javaExpression, settings, ParseMode.EVALUATION,-1, valueOfThis);
			}
		} else {
			parseResult = parse(javaExpression, settings, ParseMode.EVALUATION,-1, valueOfThis);
		}

		switch (parseResult.getResultType()) {
			case OBJECT_PARSE_RESULT: {
				ObjectParseResult result = (ObjectParseResult) parseResult;
				if (result.getPosition() != javaExpression.length()) {
					throw new ParseException(result.getPosition(), "Unexpected character");
				} else {
					return result.getObjectInfo().getObject();
				}
			}
			case CLASS_PARSE_RESULT: {
				throw new IllegalStateException("Internal error: Class parse results should have been transformed to ParseErrors in AbstractEntityParser.parse()");
			}
			case PARSE_ERROR: {
				ParseError error = (ParseError) parseResult;
				throw new ParseException(error.getPosition(), error.getMessage(), error.getThrowable());
			}
			case AMBIGUOUS_PARSE_RESULT: {
				AmbiguousParseResult result = (AmbiguousParseResult) parseResult;
				throw new ParseException(result.getPosition(), result.getMessage());
			}
			case COMPLETION_SUGGESTIONS: {
				throw new IllegalStateException("Internal error: Unexpected code completion");
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + parseResult.getResultType());
		}
	}

	private ParseResultIF parse(String javaExpression, ParserSettings settings, ParseMode parseMode, int caret, Object valueOfThis) {
		ObjectInfo thisInfo = new ObjectInfo(valueOfThis, null);
		ParserToolbox parserPool  = new ParserToolbox(thisInfo, settings, parseMode);
		TokenStream tokenStream = new TokenStream(javaExpression, caret);
		try {
			return parserPool.getRootParser().parse(tokenStream, thisInfo, ParseExpectation.OBJECT);
		} catch (Exception e) {
			String exceptionClassName = e.getClass().getSimpleName();
			String exceptionMessage = e.getMessage();

			StringBuilder logMessageBuilder = new StringBuilder();
			logMessageBuilder.append(exceptionClassName);
			if (exceptionMessage != null) {
				logMessageBuilder.append(": ").append(exceptionMessage);
			}
			for (StackTraceElement element : e.getStackTrace()) {
				logMessageBuilder.append("\n").append(element);
			}
			settings.getLogger().log(new ParserLogEntry(LogLevel.ERROR, getClass().getSimpleName(), logMessageBuilder.toString()));

			String message = exceptionClassName;
			if (exceptionMessage != null) {
				message += ("\n" + exceptionMessage);
			}
			return new ParseError(-1, message, ErrorType.EVALUATION_EXCEPTION, e);
		}
	}
}
