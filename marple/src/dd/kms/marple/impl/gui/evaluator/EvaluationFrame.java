package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.common.CurrentObjectPanel;
import dd.kms.marple.impl.gui.common.WindowManager;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class EvaluationFrame extends JFrame
{
	public static final Dimension	INITIAL_PREFERRED_SIZE	= new Dimension(600, 400);

	private final JPanel				mainPanel			= new JPanel(new GridBagLayout());

	private final CurrentObjectPanel	currentObjectPanel;
	private final RelatedObjectsPanel	relatedObjectsPanel;
	private final EvaluationPanel		evaluationPanel;

	public EvaluationFrame(InspectionContext context) {
		this.currentObjectPanel = new CurrentObjectPanel(context);
		this.relatedObjectsPanel = new RelatedObjectsPanel(context);
		this.evaluationPanel = new EvaluationPanel(context);
		configure();
	}

	private void configure() {
		setTitle("Evaluate");

		getContentPane().add(mainPanel);

		mainPanel.add(currentObjectPanel, 	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL,	DEFAULT_INSETS, 0, 0));
		mainPanel.add(relatedObjectsPanel, 	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL,	DEFAULT_INSETS, 0, 0));
		mainPanel.add(evaluationPanel, 		new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, CENTER, BOTH,			DEFAULT_INSETS, 0, 0));

		addListeners();
	}

	private void addListeners() {
		WindowManager.updateFrameOnFocusGained(this, evaluationPanel::updateContent);
	}

	public void evaluate(String expression, Object thisValue, int caretPosition) {
		setThisValue(thisValue);
		evaluationPanel.setExpression(expression, caretPosition);
	}

	private void setThisValue(Object thisValue) {
		currentObjectPanel.setCurrentObject(thisValue);
		relatedObjectsPanel.setCurrentObject(thisValue);
		evaluationPanel.setThisValue(thisValue);

		if (relatedObjectsPanel.isVisible()) {
			// possibly increase preferred frame height because size of relatedObjectsPanel is dynamic
			pack();
			Dimension preferredSize = getPreferredSize();
			int suggestedPreferredHeight = INITIAL_PREFERRED_SIZE.height + Math.min(relatedObjectsPanel.getHeight(), 100);
			if (preferredSize.height < suggestedPreferredHeight) {
				preferredSize.height = suggestedPreferredHeight;
				setPreferredSize(preferredSize);
				pack();
			}
		}
	}
}
