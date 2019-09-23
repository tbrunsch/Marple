package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class AbstractExpressionInputTextField<T> extends AbstractInputTextField<T>
{
	public AbstractExpressionInputTextField(InspectionContext context) {
		super(context);
	}

	public void setExpression(String expression) {
		setText(expression);
		setCaretPosition(expression == null ? 0 : expression.length());
	}

	abstract Map<CompletionSuggestion, MatchRating> suggestCodeCompletion(String text, int caretPosition) throws ParseException;

	@Override
	Map<CompletionSuggestion, Integer> provideRatedSuggestions(String text, int caretPosition) throws ParseException {
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = suggestCodeCompletion(text, caretPosition);
		return Ratings.filterAndTransformMatchRatings(ratedSuggestions);
	}
}
