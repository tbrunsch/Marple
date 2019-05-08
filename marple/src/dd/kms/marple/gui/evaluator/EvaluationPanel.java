package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.common.GuiCommons;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.marple.gui.inspector.views.FieldView;
import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionField;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionVariable;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.settings.ParserSettings;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

import static java.awt.GridBagConstraints.*;

public class EvaluationPanel extends JPanel
{
	private final JPanel				expressionPanel			= new JPanel(new GridBagLayout());
	private final EvaluationTextField	evaluationTextField;
	private final JButton				settingsButton			= new JButton("...");
	private final DynamicTypingControls	dynamicTypingControls;

	private final JPanel				evaluationResultPanel	= new JPanel(new GridBagLayout());

	private final InspectionContext		inspectionContext;

	private Object						thisValue;

	public EvaluationPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;
		this.evaluationTextField = new EvaluationTextField(this::evaluateExpression, inspectionContext);
		this.dynamicTypingControls = new DynamicTypingControls(inspectionContext);

		add(expressionPanel,		new GridBagConstraints(0, 0, REMAINDER, 1, 1.0, 0.0, CENTER, HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		add(evaluationResultPanel,	new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 0.8, CENTER, BOTH, new Insets(5, 0, 0, 0), 0, 0));

		expressionPanel.setBorder(BorderFactory.createTitledBorder("Expression"));
		expressionPanel.add(evaluationTextField,								new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, CENTER, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		expressionPanel.add(settingsButton,										new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, EAST, NONE, new Insets(5, 5, 5, 5), 0, 0));
		expressionPanel.add(dynamicTypingControls.getDynamicTypingCheckBox(),	new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 0.0, WEST, NONE, new Insets(5, 5, 5, 5), 0, 0));

		evaluationResultPanel.setBorder(BorderFactory.createTitledBorder("Result"));

		settingsButton.addActionListener(e -> openSettingsDialog());
	}

	public void setThisValue(Object thisValue) {
		this.thisValue = thisValue;
		evaluationTextField.setThisValue(thisValue);
	}

	public void setExpression(String expression) {
		evaluationTextField.setExpression(expression);
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
		JLabel exceptionLabel = new JLabel(CodeCompletionDecorators.formatExceptionMessage(evaluationTextField.getText(), e));
		CodeCompletionDecorators.configureExceptionComponent(exceptionLabel);
		evaluationResultPanel.add(exceptionLabel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, NORTH, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		evaluationResultPanel.revalidate();
		evaluationResultPanel.repaint();
	}

	private void openSettingsDialog() {
		EvaluationSettingsPane settingsPane = new EvaluationSettingsPane(inspectionContext);
		GuiCommons.showInDialog("Settings", settingsPane);
		dynamicTypingControls.updateControls();
	}
}
