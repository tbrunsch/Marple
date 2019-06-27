package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.ExpressionParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionField;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionVariable;

import java.util.*;
import java.util.function.Consumer;

public class ExpressionInputTextField extends AbstractInputTextField<Object>
{
	private Object	thisValue;

	public ExpressionInputTextField(InspectionContext context) {
		super(context);
	}

	public void setThisValue(Object thisValue) {
		this.thisValue = thisValue;
	}

	public void setExpression(String expression) {
		setText(expression);
		setCaretPosition(expression == null ? 0 : expression.length());
	}

	@Override
	List<CompletionSuggestion> suggestCodeCompletions(String text, int caretPosition) throws ParseException {
		ExpressionParser parser = createParser(text);
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = parser.suggestCodeCompletion(caretPosition);
		List<CompletionSuggestion> suggestions = new ArrayList<>(ratedSuggestions.keySet());
		suggestions.removeIf(suggestion -> ratedSuggestions.get(suggestion).getNameMatch() == StringMatch.NONE);
		suggestions.sort(Comparator.comparingInt(ExpressionInputTextField::getCompletionSuggestionPriorityByClass));
		suggestions.sort(Comparator.comparing(ratedSuggestions::get));
		return suggestions;
	}

	@Override
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException  {
		ExpressionParser parser = createParser(text);
		return parser.getExecutableArgumentInfo(caretPosition);
	}

	@Override
	Object evaluate(String text) throws ParseException {
		ExpressionParser parser = createParser(text);
		return parser.evaluate();

	}

	private ExpressionParser createParser(String text) {
		return Parsers.createExpressionParser(text, getParserSettings(), thisValue);
	}

	/**
	 * Prefer variables ({@link CompletionSuggestionVariable}) over fields ({@link CompletionSuggestionField}) over other suggestions.
	 */
	private static int getCompletionSuggestionPriorityByClass(CompletionSuggestion suggestion) {
		Class<? extends CompletionSuggestion> suggestionClass = suggestion.getClass();
		return	suggestionClass == CompletionSuggestionVariable.class	? 0 :
				suggestionClass == CompletionSuggestionField.class		? 1
																		: 2;
	}
}
