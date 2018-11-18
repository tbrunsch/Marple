package com.AMS.jBEAM.javaParser;

import java.util.*;
import java.util.stream.Collectors;

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

	public List<CompletionSuggestionIF> suggestCodeCompletion(String javaExpression, EvaluationMode evaluationMode, int caret, Object thisContext) throws JavaParseException {
		ParseResultIF parseResult;

		if (evaluationMode == EvaluationMode.STRONGLY_TYPED) {
			// First iteration without evaluation to avoid side effects when errors occur
			parseResult = parse(javaExpression, EvaluationMode.NONE, caret, thisContext);
			if (parseResult.getResultType() == ParseResultType.COMPLETION_SUGGESTIONS) {
				// Second iteration with evaluation (side effects cannot be avoided)
				parseResult = parse(javaExpression, evaluationMode, caret, thisContext);
			}
		} else {
			parseResult = parse(javaExpression, evaluationMode, caret, thisContext);
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

	public Object evaluate(String javaExpression, EvaluationMode evaluationMode, Object thisContext) throws JavaParseException {
		ParseResultIF parseResult;

		if (evaluationMode == EvaluationMode.STRONGLY_TYPED) {
			// First iteration without evaluation to avoid side effects when errors occur
			parseResult = parse(javaExpression, EvaluationMode.NONE,-1, thisContext);
			if (parseResult.getResultType() == ParseResultType.PARSE_RESULT) {
				// Second iteration with evaluation (side effects cannot be avoided)
				parseResult = parse(javaExpression, evaluationMode,-1, thisContext);
			}
		} else {
			parseResult = parse(javaExpression, evaluationMode,-1, thisContext);
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
				throw new JavaParseException(error.getPosition(), error.getMessage());
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

	private ParseResultIF parse(String javaExpression, EvaluationMode evaluationMode, int caret, Object thisContext) {
		// TODO: Static parsing should also work for null
		if (thisContext == null) {
			throw new IllegalArgumentException("this is null");
		}
		ObjectInfo thisInfo = new ObjectInfo(thisContext);
		JavaParserPool parserPool  = new JavaParserPool(thisInfo, evaluationMode);
		JavaTokenStream tokenStream = new JavaTokenStream(javaExpression, caret);
		try {
			return parserPool.getExpressionParser().parse(tokenStream, thisInfo, null);
		} catch (Exception e) {
			String message = e.getClass().getSimpleName();
			String error = e.getMessage();
			if (error != null) {
				message += ("\n" + error);
			}
			return new ParseError(-1, message);
		}
	}

	static ParseResultIF parse(final JavaTokenStream tokenStream, final ObjectInfo currentContextInfo, final List<Class<?>> expectedResultClasses, AbstractJavaEntityParser... parsers) {
		List<ParseResultIF> parseResults = Arrays.stream(parsers)
			.map(parser -> parser.parse(tokenStream, currentContextInfo, expectedResultClasses))
			.collect(Collectors.toList());
		return mergeParseResults(parseResults);
	}

	static ParseResultIF mergeParseResults(List<ParseResultIF> parseResults) {
		if (parseResults.isEmpty()) {
			throw new IllegalArgumentException("Cannot merge 0 parse results");
		}

		List<AmbiguousParseResult> ambiguousResults = filterParseResults(parseResults, AmbiguousParseResult.class);
		List<ParseResult> results = filterParseResults(parseResults, ParseResult.class);
		List<CompletionSuggestions> completionSuggestions = filterParseResults(parseResults, CompletionSuggestions.class);
		List<ParseError> errors = filterParseResults(parseResults, ParseError.class);

		if (!completionSuggestions.isEmpty()) {
			// Merge and return suggestions
			Map<CompletionSuggestionIF, Integer> mergedRatedSuggestions = new LinkedHashMap<>();
			for (CompletionSuggestions suggestions : completionSuggestions) {
				Map<CompletionSuggestionIF, Integer> ratedSuggestions = suggestions.getRatedSuggestions();
				for (CompletionSuggestionIF suggestion : ratedSuggestions.keySet()) {
					int currentRating = mergedRatedSuggestions.containsKey(suggestion)
										? mergedRatedSuggestions.get(suggestion)
										: Integer.MAX_VALUE;
					int newRating = ratedSuggestions.get(suggestion);
					int bestRating = Math.min(currentRating, newRating);
					mergedRatedSuggestions.put(suggestion, bestRating);
				}
			}
			return new CompletionSuggestions(mergedRatedSuggestions);
		}

		boolean ambiguous = !ambiguousResults.isEmpty() || results.size() > 1;
		if (ambiguous) {
			int position = ambiguousResults.isEmpty() ? results.get(0).getParsedToPosition() : ambiguousResults.get(0).getPosition();
			String message = "Ambiguous expression:\n"
							+ ambiguousResults.stream().map(AmbiguousParseResult::getMessage).collect(Collectors.joining("\n"))
							+ results.stream().map(result -> "Expression can be evaluated to object of type " + result.getObjectInfo().getDeclaredClass().getSimpleName());
			return new AmbiguousParseResult(position, message);
		}

		if (results.size() == 1) {
			return results.get(0);
		}

		if (errors.size() > 1) {
			int position = errors.get(0).getPosition();
			return new ParseError(position, "Could not parse expression");
		} else {
			return errors.get(0);
		}
	}

	private static <T> List<T> filterParseResults(List<ParseResultIF> parseResults, Class<T> filterClass) {
		return parseResults.stream().filter(filterClass::isInstance).map(filterClass::cast).collect(Collectors.toList());
	}
}
