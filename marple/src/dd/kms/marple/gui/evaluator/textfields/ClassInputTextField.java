package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.api.ClassParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.wrappers.ClassInfo;

import java.util.List;
import java.util.Optional;

public class ClassInputTextField extends AbstractInputTextField<ClassInfo>
{
	public ClassInputTextField(InspectionContext inspectionContext) {
		super(inspectionContext);
	}

	@Override
	List<CodeCompletion> doProvideCompletions(String text, int caretPosition) throws ParseException {
		ClassParser parser = createParser();
		List<CodeCompletion> completions = parser.getCompletions(text, caretPosition);
		return filterCompletions(completions);
	}

	@Override
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) {
		return Optional.empty();
	}

	@Override
	ClassInfo evaluate(String text) throws ParseException {
		ClassParser parser = createParser();
		return parser.evaluate(text);
	}

	private ClassParser createParser() {
		return Parsers.createClassParser(getParserSettings());
	}
}
