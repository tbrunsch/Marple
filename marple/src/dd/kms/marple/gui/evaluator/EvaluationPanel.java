package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.common.ExceptionFormatter;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.marple.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.gui.evaluator.textfields.ExpressionInputTextField;
import dd.kms.marple.gui.inspector.views.fieldview.FieldView;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class EvaluationPanel extends JPanel
{
	private final JPanel					expressionPanel			= new JPanel(new GridBagLayout());
	private final JPanel					evaluationTextFieldPanel;
	private final ExpressionInputTextField	evaluationTextField;
	private final DynamicTypingControls		dynamicTypingControls;

	private final JPanel					evaluationResultPanel	= new JPanel(new GridBagLayout());

	private final InspectionContext			inspectionContext;

	public EvaluationPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;
		this.evaluationTextField = new ExpressionInputTextField(inspectionContext);
		this.evaluationTextFieldPanel = new EvaluationTextFieldPanel(evaluationTextField, inspectionContext);
		this.dynamicTypingControls = new DynamicTypingControls(inspectionContext);

		evaluationTextField.setEvaluationResultConsumer(this::displayObject);
		evaluationTextField.setExceptionConsumer(this::displayException);

		add(expressionPanel,		new GridBagConstraints(0, 0, REMAINDER, 1, 1.0, 0.0, CENTER, HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));
		add(evaluationResultPanel,	new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 0.8, CENTER, BOTH, new Insets(5, 0, 0, 0), 0, 0));

		expressionPanel.setBorder(BorderFactory.createTitledBorder("Expression"));
		expressionPanel.add(evaluationTextFieldPanel,							new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
		expressionPanel.add(dynamicTypingControls.getDynamicTypingCheckBox(),	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		evaluationResultPanel.setBorder(BorderFactory.createTitledBorder("Result"));
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

	private void displayObject(ObjectInfo objectInfo) {
		evaluationResultPanel.removeAll();
		FieldView objectView = new FieldView(objectInfo, inspectionContext);
		evaluationResultPanel.add(objectView,		new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		evaluationResultPanel.revalidate();
		evaluationResultPanel.repaint();
	}

	private void displayException(@Nullable Throwable t) {
		evaluationResultPanel.removeAll();
		JLabel exceptionLabel = new JLabel(ExceptionFormatter.formatParseException(evaluationTextField.getText(), t));
		CodeCompletionDecorators.configureExceptionComponent(exceptionLabel);
		evaluationResultPanel.add(exceptionLabel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, NORTH, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		evaluationResultPanel.revalidate();
		evaluationResultPanel.repaint();
	}
}
