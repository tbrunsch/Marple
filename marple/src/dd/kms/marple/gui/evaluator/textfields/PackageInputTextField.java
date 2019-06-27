package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.PackageParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.*;
import java.util.function.Consumer;

public class PackageInputTextField extends AbstractInputTextField<PackageInfo>
{
	public PackageInputTextField(InspectionContext inspectionContext) {
		super(inspectionContext);
	}

	@Override
	List<CompletionSuggestion> suggestCodeCompletions(String text, int caretPosition) throws ParseException {
		PackageParser parser = createParser(text);
		Map<CompletionSuggestion, StringMatch> ratedSuggestions = parser.suggestCodeCompletion(caretPosition);
		List<CompletionSuggestion> suggestions = new ArrayList<>(ratedSuggestions.keySet());
		suggestions.removeIf(suggestion -> ratedSuggestions.get(suggestion) == StringMatch.NONE);
		suggestions.sort(Comparator.comparing(CompletionSuggestion::toString));
		suggestions.sort(Comparator.comparing(ratedSuggestions::get));
		return suggestions;
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
