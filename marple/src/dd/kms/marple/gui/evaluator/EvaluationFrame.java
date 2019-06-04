package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.common.CurrentObjectPanel;
import dd.kms.marple.gui.common.WindowManager;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.*;

public class EvaluationFrame extends JFrame
{
	private final JPanel				mainPanel			= new JPanel(new GridBagLayout());

	private final CurrentObjectPanel	currentObjectPanel;
	private final EvaluationPanel		evaluationPanel;

	public EvaluationFrame(InspectionContext context) {
		this.currentObjectPanel = new CurrentObjectPanel(context);
		this.evaluationPanel = new EvaluationPanel(context);
		configure();
	}

	private void configure() {
		setTitle("Evaluate");

		getContentPane().add(mainPanel);

		mainPanel.add(currentObjectPanel, 	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, CENTER, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		mainPanel.add(evaluationPanel, 		new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, CENTER, BOTH, new Insets(5, 5, 5, 5), 0, 0));

		addListeners();
	}

	private void addListeners() {
		WindowManager.updateFrameOnFocusGained(this, evaluationPanel::updateContent);
	}

	public void setThisValue(Object thisValue) {
		currentObjectPanel.setCurrentObject(thisValue);
		evaluationPanel.setThisValue(thisValue);
	}

	public void setExpression(String expression) {
		evaluationPanel.setExpression(expression);
	}

	public void setCaretPosition(int caretPosition) {
		evaluationPanel.setCaretPosition(caretPosition);
	}
}
