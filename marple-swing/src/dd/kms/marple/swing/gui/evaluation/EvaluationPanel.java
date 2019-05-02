package dd.kms.marple.swing.gui.evaluation;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.swing.SwingObjectInspectionFramework;
import dd.kms.marple.swing.gui.CurrentObjectPanel;
import dd.kms.marple.swing.gui.evaluation.completion.CodeCompletionDecorators;
import dd.kms.marple.swing.gui.views.FieldView;
import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionField;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionVariable;
import dd.kms.zenodot.settings.ParserSettings;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static java.awt.GridBagConstraints.*;

public class EvaluationPanel extends JPanel
{
	private final CurrentObjectPanel			currentObjectPanel;

	private final JLabel						expressionLabel			= new JLabel("Expression:");
	private final JTextField					evaluationTextField		= new JTextField();

	private final JLabel						resultLabel				= new JLabel("Result:");
	private final JPanel						evaluationResultPanel	= new JPanel(new GridBagLayout());

	private final Object						thisValue;
	private final InspectionContext<Component>	inspectionContext;

	public EvaluationPanel(Object thisValue, InspectionContext<Component> inspectionContext) {
		super(new GridBagLayout());

		this.thisValue = thisValue;
		this.inspectionContext = inspectionContext;

		if (thisValue != null) {
			currentObjectPanel = new CurrentObjectPanel(inspectionContext);
			currentObjectPanel.setCurrentObject(thisValue);
			add(currentObjectPanel,		new GridBagConstraints(0, 0, REMAINDER, 1, 1.0, 0.2, CENTER, BOTH, new Insets(5, 5, 5, 5), 0, 0));
		} else {
			currentObjectPanel = null;
		}

		add(expressionLabel,		new GridBagConstraints(0, 1, 1, 1, 0.0, 0.2, WEST, NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(evaluationTextField,	new GridBagConstraints(1, 1, REMAINDER, 1, 1.0, 0.0, CENTER, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		add(resultLabel,			new GridBagConstraints(0, 2, REMAINDER, 1, 1.0, 0.0, WEST, NONE, new Insets(5, 5, 5, 5), 0, 0));
		add(evaluationResultPanel,	new GridBagConstraints(0, 3, REMAINDER, 1, 1.0, 0.6, CENTER, BOTH, new Insets(5, 5, 5, 5), 0, 0));

		evaluationResultPanel.setBorder(BorderFactory.createEtchedBorder());

		CodeCompletionDecorators.decorate(
			evaluationTextField,
			this::suggestCodeCompletions,
			SwingObjectInspectionFramework.getCodeCompletionKey(),
			this::getExecutableArgumentInfo,
			SwingObjectInspectionFramework.getShowExecutableArgumentsKey(),
			this::evaluateExpression
		);
	}

	public void setExpression(String expression) {
		evaluationTextField.setText(expression);
		evaluationTextField.setCaretPosition(expression == null ? 0 : expression.length());
	}

	private List<CompletionSuggestion> suggestCodeCompletions(String expression, int caretPosition) throws ParseException  {
		JavaParser parser = new JavaParser();
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = parser.suggestCodeCompletion(expression, caretPosition, getParserSettings(), thisValue);
		List<CompletionSuggestion> suggestions = new ArrayList<>(ratedSuggestions.keySet());
		suggestions.removeIf(suggestion -> ratedSuggestions.get(suggestion).getNameMatch() == StringMatch.NONE);
		suggestions.sort(Comparator.comparingInt(EvaluationPanel::getCompletionSuggestionPriorityByClass));
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

	private void evaluateExpression(String expression) {
		JavaParser parser = new JavaParser();
		try {
			Object evaluationResult = parser.evaluate(expression, getParserSettings(), thisValue);
			displayObject(evaluationResult);
		} catch (ParseException e) {
			displayException(e);
		}
	}

	private ParserSettings getParserSettings() {
		return inspectionContext.getEvaluator().getParserSettings();
	}

	private void displayObject(Object object) {
		evaluationResultPanel.removeAll();
		FieldView objectView = new FieldView(object, inspectionContext);
		evaluationResultPanel.add(objectView,		new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		evaluationResultPanel.revalidate();
		evaluationResultPanel.repaint();
	}

	private void displayException(ParseException e) {
		evaluationResultPanel.removeAll();
		JLabel exceptionLabel = new JLabel("<html><p>" + e.getMessage().replace("\n", "<br/>") + "</p></html>");
		exceptionLabel.setForeground(Color.RED);
		evaluationResultPanel.add(exceptionLabel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, NORTH, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		evaluationResultPanel.revalidate();
		evaluationResultPanel.repaint();
	}
}
