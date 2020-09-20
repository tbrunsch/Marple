package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.common.ResultPanel;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.evaluator.textfields.ExpressionInputTextField;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class EvaluationPanel extends JPanel
{
	private final JPanel					expressionPanel			= new JPanel(new GridBagLayout());
	private final JPanel					evaluationTextFieldPanel;
	private final ExpressionInputTextField	evaluationTextField;
	private final DynamicTypingControls		dynamicTypingControls;

	private final ResultPanel				resultPanel;

	public EvaluationPanel(InspectionContext context) {
		super(new GridBagLayout());

		this.evaluationTextField = new ExpressionInputTextField(context);
		this.evaluationTextFieldPanel = new EvaluationTextFieldPanel(evaluationTextField, context);
		this.dynamicTypingControls = new DynamicTypingControls(context);
		this.resultPanel = new ResultPanel(context);

		add(expressionPanel,	new GridBagConstraints(0, 0, REMAINDER, 1, 1.0, 0.0, CENTER, HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		add(resultPanel,		new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 0.8, CENTER, BOTH, new Insets(5, 0, 0, 0), 0, 0));

		expressionPanel.setBorder(BorderFactory.createTitledBorder("Expression"));
		expressionPanel.add(evaluationTextFieldPanel,							new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
		expressionPanel.add(dynamicTypingControls.getDynamicTypingCheckBox(),	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		evaluationTextField.setEvaluationResultConsumer(resultPanel::displayResult);
		evaluationTextField.setExceptionConsumer(resultPanel::displayException);
	}

	public void setThisValue(ObjectInfo thisValue) {
		evaluationTextField.setThisValue(thisValue);
	}

	public void setExpression(String expression) {
		evaluationTextField.setExpression(expression);
	}

	public void setCaretPosition(int caretPosition) {
		evaluationTextField.setCaretPosition(caretPosition);
	}

	void updateContent() {
		dynamicTypingControls.updateControls();
	}
}
