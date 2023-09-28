package dd.kms.marple.impl.gui.evaluator.textfields;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.ClassParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.List;
import java.util.Optional;

public class ClassInputTextField extends AbstractInputTextField<Class<?>>
{
	public ClassInputTextField(InspectionContext context) {
		super(context);
	}

	@Override
	List<CodeCompletion> doProvideCompletions(String text, int caretPosition) throws ParseException {
		ClassParser parser = createParser();
		List<CodeCompletion> completions = parser.getCompletions(text, caretPosition);
		return filterCompletions(completions);
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) {
		return Optional.empty();
	}

	@Override
	Class<?> evaluate(String text) throws ParseException {
		ClassParser parser = createParser();
		return parser.evaluate(text);
	}

	private ClassParser createParser() {
		return Parsers.createClassParser(getParserSettings());
	}
}
