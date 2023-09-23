package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.List;
import java.util.Optional;

public interface ParserMediator
{
	List<CodeCompletion> provideCodeCompletions(String text, int caretPosition) throws ParseException;
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String expression, int caretPosition) throws ParseException;
}
