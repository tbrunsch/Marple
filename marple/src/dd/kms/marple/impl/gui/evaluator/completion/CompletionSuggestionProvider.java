package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.CodeCompletion;

import java.util.List;

@FunctionalInterface
public interface CompletionSuggestionProvider
{
	List<CodeCompletion> provideCodeCompletions(String text, int caretPosition) throws ParseException;
}
