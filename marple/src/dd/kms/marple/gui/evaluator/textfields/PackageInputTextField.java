package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.PackageParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.Map;
import java.util.Optional;

public class PackageInputTextField extends AbstractInputTextField<PackageInfo>
{
	public PackageInputTextField(InspectionContext inspectionContext) {
		super(inspectionContext);
	}

	@Override
	Map<CompletionSuggestion, Integer> doProvideRatedSuggestions(String text, int caretPosition) throws ParseException {
		PackageParser parser = createParser(text);
		Map<CompletionSuggestion, StringMatch> ratedSuggestions = parser.suggestCodeCompletion(caretPosition);
		return Ratings.filterAndTransformStringMatches(ratedSuggestions);
	}

	@Override
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) {
		return Optional.empty();
	}

	@Override
	PackageInfo evaluate(String text) throws ParseException {
		PackageParser parser = createParser(text);
		return parser.evaluate();
	}

	private PackageParser createParser(String text) {
		return Parsers.createPackageParser(text, getParserSettings());
	}
}
