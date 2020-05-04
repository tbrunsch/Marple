package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.ClassParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.Map;
import java.util.Optional;

public class ClassInputTextField extends AbstractInputTextField<ClassInfo>
{
	public ClassInputTextField(InspectionContext inspectionContext) {
		super(inspectionContext);
	}

	@Override
	Map<CompletionSuggestion, Integer> doProvideRatedSuggestions(String text, int caretPosition) throws ParseException {
		ClassParser parser = createParser(text);
		Map<CompletionSuggestion, StringMatch> ratedSuggestions = parser.suggestCodeCompletion(caretPosition);
		return Ratings.filterAndTransformStringMatches(ratedSuggestions);
	}

	@Override
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) {
		return Optional.empty();
	}

	@Override
	ClassInfo evaluate(String text) throws ParseException {
		ClassParser parser = createParser(text);
		return parser.evaluate();
	}

	private ClassParser createParser(String text) {
		return Parsers.createClassParser(text, getParserSettings());
	}
}
