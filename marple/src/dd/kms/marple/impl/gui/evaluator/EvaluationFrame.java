package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.gui.common.CurrentObjectPanel;
import dd.kms.marple.impl.gui.common.WindowManager;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
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

		mainPanel.add(currentObjectPanel, 	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, CENTER, HORIZONTAL,	DEFAULT_INSETS, 0, 0));
		mainPanel.add(evaluationPanel, 		new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, CENTER, BOTH,			DEFAULT_INSETS, 0, 0));

		addListeners();
	}

	private void addListeners() {
		WindowManager.updateFrameOnFocusGained(this, evaluationPanel::updateContent);
	}

	public void setThisValue(ObjectInfo thisValue) {
		ObjectInfo runtimeInfo = ReflectionUtils.getRuntimeInfo(thisValue);
		currentObjectPanel.setCurrentObject(runtimeInfo);
		evaluationPanel.setThisValue(runtimeInfo);
	}

	public void setExpression(String expression) {
		evaluationPanel.setExpression(expression);
	}

	public void setCaretPosition(int caretPosition) {
		evaluationPanel.setCaretPosition(caretPosition);
	}
}
