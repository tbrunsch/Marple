package dd.kms.marple.impl.gui.evaluator.textfields;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.PackageParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.List;
import java.util.Optional;

public class PackageInputTextField extends AbstractInputTextField<String>
{
	public PackageInputTextField(InspectionContext context) {
		super(context);
	}

	@Override
	List<CodeCompletion> doProvideCompletions(String text, int caretPosition) throws ParseException {
		PackageParser parser = createParser();
		List<CodeCompletion> completions = parser.getCompletions(text, caretPosition);
		return filterCompletions(completions);
	}

	@Override
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) {
		return Optional.empty();
	}

	@Override
	String evaluate(String text) throws ParseException {
		PackageParser parser = createParser();
		return parser.evaluate(text);
	}

	private PackageParser createParser() {
		return Parsers.createPackageParser(getParserSettings());
	}
}
