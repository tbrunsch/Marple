package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;

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
			// Prefer fields over methods
			return suggestionClass1 == CompletionSuggestionField.class ? -1 : 1;
		}
	};

	public List<CompletionSuggestionIF> suggestCodeCompletion(String javaExpression, JavaParserSettings settings, int caret, Object thisContext) throws JavaParseException {
		ParseResultIF parseResult;

		EvaluationMode evaluationMode = settings.getEvaluationModeCodeCompletion();
		if (evaluationMode == EvaluationMode.STRONGLY_TYPED) {
			// First iteration without evaluation to avoid side effects when errors occur
			parseResult = parse(javaExpression, settings, EvaluationMode.NONE, caret, thisContext);
			if (parseResult.getResultType() == ParseResultType.COMPLETION_SUGGESTIONS) {
				// Second iteration with evaluation (side effects cannot be avoided)
				parseResult = parse(javaExpression, settings, evaluationMode, caret, thisContext);
			}
		} else {
			parseResult = parse(javaExpression, settings, evaluationMode, caret, thisContext);
		}

		switch (parseResult.getResultType()) {
			case PARSE_RESULT: {
				ParseResult result = (ParseResult) parseResult;
				if (result.getParsedToPosition() != javaExpression.length()) {
					throw new JavaParseException(result.getParsedToPosition(), "Unexpected character");
				} else {
					throw new IllegalStateException("Internal error: No completions available");
				}
			}
			case PARSE_ERROR: {
				ParseError error = (ParseError) parseResult;
				throw new JavaParseException(error.getPosition(), error.getMessage());
			}
			case AMBIGUOUS_PARSE_RESULT: {
				AmbiguousParseResult result = (AmbiguousParseResult) parseResult;
				throw new JavaParseException(result.getPosition(), result.getMessage());
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

	public Object evaluate(String javaExpression, JavaParserSettings settings, Object thisContext) throws JavaParseException {
		ParseResultIF parseResult;

		EvaluationMode evaluationMode = settings.getEvaluationModeCodeEvaluation();
		if (evaluationMode == EvaluationMode.STRONGLY_TYPED) {
			// First iteration without evaluation to avoid side effects when errors occur
			parseResult = parse(javaExpression, settings, EvaluationMode.NONE,-1, thisContext);
			if (parseResult.getResultType() == ParseResultType.PARSE_RESULT) {
				// Second iteration with evaluation (side effects cannot be avoided)
				parseResult = parse(javaExpression, settings, evaluationMode,-1, thisContext);
			}
		} else {
			parseResult = parse(javaExpression, settings, evaluationMode,-1, thisContext);
		}

		switch (parseResult.getResultType()) {
			case PARSE_RESULT: {
				ParseResult result = (ParseResult) parseResult;
				if (result.getParsedToPosition() != javaExpression.length()) {
					throw new JavaParseException(result.getParsedToPosition(), "Unexpected character");
				} else {
					return result.getObjectInfo().getObject();
				}
			}
			case PARSE_ERROR: {
				ParseError error = (ParseError) parseResult;
				throw new JavaParseException(error.getPosition(), error.getMessage(), error.getException());
			}
			case AMBIGUOUS_PARSE_RESULT: {
				AmbiguousParseResult result = (AmbiguousParseResult) parseResult;
				throw new JavaParseException(result.getPosition(), result.getMessage());
			}
			case COMPLETION_SUGGESTIONS: {
				throw new IllegalStateException("Internal error: Unexpected code completion");
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + parseResult.getResultType());
		}
	}

	private ParseResultIF parse(String javaExpression, JavaParserSettings settings, EvaluationMode evaluationMode, int caret, Object thisContext) {
		ObjectInfo thisInfo = new ObjectInfo(thisContext);
		JavaParserContext parserPool  = new JavaParserContext(thisInfo, settings, evaluationMode);
		TokenStream tokenStream = new TokenStream(javaExpression, caret);
		try {
			return parserPool.getCompoundExpressionParser().parse(tokenStream, thisInfo, null);
		} catch (Exception e) {
			String message = e.getClass().getSimpleName();
			String error = e.getMessage();
			if (error != null) {
				message += ("\n" + error);
			}
			return new ParseError(-1, message, ErrorType.EVALUATION_EXCEPTION, e);
		}
	}
}
