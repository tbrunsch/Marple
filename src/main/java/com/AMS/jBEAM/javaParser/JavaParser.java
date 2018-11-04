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

    public List<CompletionSuggestionIF> suggestCodeCompletion(String javaExpression, int caret, Object thisContext) throws JavaParseException {
        // TODO: Static parsing should also work for null
        if (thisContext == null) {
            throw new IllegalArgumentException("this is null");
        }
        Class<?> thisContextClass = thisContext.getClass();
        JavaParserSettings parserSettings  = new JavaParserSettings(thisContextClass);
        JavaTokenStream tokenStream = new JavaTokenStream(javaExpression, caret);
        ParseResultIF parseResult = parserSettings.getExpressionParser().parse(tokenStream, thisContextClass, null);
        switch (parseResult.getResultType()) {
            case PARSE_RESULT: {
				ParseResult result = (ParseResult) parseResult;
				if (result.getParsedToPosition() != javaExpression.length()) {
					throw new JavaParseException(new ParseError(result.getParsedToPosition(), "Unexpected character"));
				} else {
					throw new IllegalStateException("Internal error: No completions available");
				}
			}
            case PARSE_ERROR:
                throw new JavaParseException((ParseError) parseResult);
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

    static ParseResultIF parse(final JavaTokenStream tokenStream, final Class<?> currentContextClass, final Class<?> expectedResultClass, AbstractJavaEntityParser... parsers) {
        List<ParseResultIF> parseResults = Arrays.stream(parsers)
                .map(parser -> parser.parse(tokenStream, currentContextClass, expectedResultClass))
                .collect(Collectors.toList());
        return mergeParseResults(parseResults);
    }

    static ParseResultIF mergeParseResults(List<ParseResultIF> parseResults) {
        if (parseResults.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge 0 parse results");
        }

        List<CompletionSuggestions> completionSuggestions = parseResults.stream()
            .filter(CompletionSuggestions.class::isInstance)
            .map(CompletionSuggestions.class::cast)
            .collect(Collectors.toList());

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

        Optional<ParseResult> firstParseResult = parseResults.stream()
            .filter(ParseResult.class::isInstance)
            .map(ParseResult.class::cast)
            .findFirst();
        if (firstParseResult.isPresent()) {
            return firstParseResult.get();
        }

        Optional<ParseError> firstParseError = parseResults.stream()
            .filter(ParseError.class::isInstance)
            .map(ParseError.class::cast)
            .findFirst();
        if (firstParseError.isPresent()) {
            return firstParseError.get();
        }

        throw new IllegalStateException("Neither suggestions nor parse results nor errors found");
    }
}
