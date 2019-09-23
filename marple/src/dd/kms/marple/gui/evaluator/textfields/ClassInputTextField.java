package dd.kms.marple.gui.evaluator.textfields;

import com.google.common.collect.Maps;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.ClassParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassInputTextField extends AbstractInputTextField<ClassInfo>
{
	public ClassInputTextField(InspectionContext inspectionContext) {
		super(inspectionContext);
	}

	@Override
	Map<CompletionSuggestion, Integer> provideRatedSuggestions(String text, int caretPosition) throws ParseException {
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
