package dd.kms.marple.gui.evaluator.completion;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.Optional;

@FunctionalInterface
public interface ExecutableArgumentInfoProvider
{
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String expression, int caretPosition) throws ParseException;
}
