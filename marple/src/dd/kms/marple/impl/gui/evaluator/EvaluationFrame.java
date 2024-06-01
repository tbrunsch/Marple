package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.impl.actions.ActionWrapper;
import dd.kms.marple.impl.actions.HistoryBackAction;
import dd.kms.marple.impl.actions.HistoryForwardAction;
import dd.kms.marple.impl.gui.common.CurrentObjectPanel;
import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.marple.impl.gui.common.History;
import dd.kms.marple.impl.gui.common.WindowManager;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class EvaluationFrame extends JFrame implements Disposable
{
	public static final Dimension	INITIAL_PREFERRED_SIZE	= new Dimension(600, 400);

	private final JPanel							mainPanel				= new JPanel(new GridBagLayout());

	private final JPanel							navigationPanel			= new JPanel(new GridBagLayout());
	private final JButton							prevButton				= new JButton();
	private final JButton							nextButton				= new JButton();
	private final CurrentObjectPanel				currentObjectPanel;

	private final RelatedObjectsPanel				relatedObjectsPanel;
	private final EvaluationPanel					evaluationPanel;

	private final InspectionContext					context;

	private final History<EvaluationViewSettings>	history					= new History<>();

	/**
	 * Used to distinguish whether {@link #evaluate(String, int, Object)} is called by the user or because a
	 * state from the history is currently restored.
	 */
	private boolean									restoringStateFromHistory;


	public EvaluationFrame(InspectionContext context) {
		this.context = context;
		this.currentObjectPanel = new CurrentObjectPanel(context);
		this.relatedObjectsPanel = new RelatedObjectsPanel(context);
		this.evaluationPanel = new EvaluationPanel(context);
		configure();
	}

	private void configure() {
		setTitle("Evaluate");

		getContentPane().add(mainPanel);

		mainPanel.add(navigationPanel,		new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL,	DEFAULT_INSETS, 0, 0));
		mainPanel.add(relatedObjectsPanel,	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, NORTHWEST, HORIZONTAL,	DEFAULT_INSETS, 0, 0));
		mainPanel.add(evaluationPanel,		new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, CENTER, BOTH,			DEFAULT_INSETS, 0, 0));

		navigationPanel.add(prevButton,   		new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,	GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));
		navigationPanel.add(currentObjectPanel,	new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,	GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));
		navigationPanel.add(nextButton,   		new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,	GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));

		prevButton.setAction(new ActionWrapper(new HistoryBackAction<>(history, this::getStringRepresentation, this::updateHistoryElement, this::applyHistoryViewSettings)));
		nextButton.setAction(new ActionWrapper(new HistoryForwardAction<>(history, this::getStringRepresentation, this::updateHistoryElement, this::applyHistoryViewSettings)));

		addListeners();
	}

	private void addListeners() {
		WindowManager.updateFrameOnFocusGained(this, evaluationPanel::updateContent);
	}

	public void evaluate(String expression, int caretPosition, Object thisValue) {
		if (!restoringStateFromHistory) {
			updateHistoryElement();
		}

		setThisValue(thisValue);
		evaluationPanel.setExpression(expression, caretPosition);

		if (!restoringStateFromHistory) {
			EvaluationViewSettings newViewSettings = getViewSettings();
			history.add(newViewSettings);
		}

		GuiCommons.reevaluateButtonAction(prevButton);
		GuiCommons.reevaluateButtonAction(nextButton);

		evaluationPanel.evaluate();

		setVisible(true);
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

	private String getStringRepresentation(EvaluationViewSettings viewSettings) {
		Object currentObject = viewSettings.getCurrentObject();
		return context.getDisplayText(currentObject);
	}

	private void applyHistoryViewSettings(EvaluationViewSettings viewSettings) {
		restoringStateFromHistory = true;
		try {
			evaluate(viewSettings.getExpression(), viewSettings.getCaretPosition(), viewSettings.getCurrentObject());
		} finally {
			restoringStateFromHistory = false;
		}
	}

	private void updateHistoryElement() {
		if (currentObjectPanel.getCurrentObject() != null) {
			EvaluationViewSettings viewSettings = getViewSettings();
			history.set(viewSettings);
		}
	}

	private EvaluationViewSettings getViewSettings() {
		Object currentObject = currentObjectPanel.getCurrentObject();
		String expression = evaluationPanel.getExpression();
		int caretPosition = evaluationPanel.getCaretPosition();
		return new EvaluationViewSettings(currentObject, expression, caretPosition);
	}

	@Override
	public void dispose() {
		currentObjectPanel.dispose();
		relatedObjectsPanel.dispose();
		evaluationPanel.dispose();
		history.clear();

		super.dispose();
	}

	private static class EvaluationViewSettings
	{
		private final Object	currentObject;
		private final String	expression;
		private final int		caretPosition;

		private EvaluationViewSettings(Object currentObject, String expression, int caretPosition) {
			this.currentObject = currentObject;
			this.expression = expression;
			this.caretPosition = caretPosition;
		}

		Object getCurrentObject() {
			return currentObject;
		}

		String getExpression() {
			return expression;
		}

		int getCaretPosition() {
			return caretPosition;
		}
	}
}
