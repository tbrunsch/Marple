package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionField;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionVariable;
import dd.kms.zenodot.settings.ParserSettings;

import javax.swing.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class EvaluationTextField extends JTextField
{
	private final InspectionContext		inspectionContext;

	private Object						thisValue;

	public EvaluationTextField(Consumer<String> expressionConsumer, InspectionContext context) {
		this.inspectionContext = context;

		CodeCompletionDecorators.decorate(
			this,
			this::suggestCodeCompletions,
			context.getSettings().getCodeCompletionKey(),
			this::getExecutableArgumentInfo,
			context.getSettings().getShowMethodArgumentsKey(),
			expressionConsumer
		);
	}

	public void setThisValue(Object thisValue) {
		this.thisValue = thisValue;
	}

	void setExpression(String expression) {
		setText(expression);
		setCaretPosition(expression == null ? 0 : expression.length());
	}

	private List<CompletionSuggestion> suggestCodeCompletions(String expression, int caretPosition) throws ParseException  {
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = JavaParser.suggestCodeCompletion(expression, caretPosition, getParserSettings(), thisValue);
		List<CompletionSuggestion> suggestions = new ArrayList<>(ratedSuggestions.keySet());
		suggestions.removeIf(suggestion -> ratedSuggestions.get(suggestion).getNameMatch() == StringMatch.NONE);
		suggestions.sort(Comparator.comparingInt(EvaluationTextField::getCompletionSuggestionPriorityByClass));
		suggestions.sort(Comparator.comparing(ratedSuggestions::get));
		return suggestions;
	}

	private Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String expression, int caretPosition) throws ParseException  {
		JavaParser parser = new JavaParser();
		return parser.getExecutableArgumentInfo(expression, caretPosition, getParserSettings(), thisValue);
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

	private ParserSettings getParserSettings() {
		return inspectionContext.getEvaluator().getParserSettings();
	}
}
