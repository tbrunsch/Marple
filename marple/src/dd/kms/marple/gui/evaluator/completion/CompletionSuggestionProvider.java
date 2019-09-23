package dd.kms.marple.gui.evaluator.completion;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface CompletionSuggestionProvider
{
	Map<CompletionSuggestion, Integer> provideRatedSuggestions(String text, int caretPosition) throws ParseException;
}
