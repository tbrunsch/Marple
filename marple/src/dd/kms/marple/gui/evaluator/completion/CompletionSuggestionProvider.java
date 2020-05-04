package dd.kms.marple.gui.evaluator.completion;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.result.CompletionSuggestion;

import java.util.Map;

@FunctionalInterface
public interface CompletionSuggestionProvider
{
	Map<CompletionSuggestion, Integer> provideRatedSuggestions(String text, int caretPosition) throws ParseException;
}
