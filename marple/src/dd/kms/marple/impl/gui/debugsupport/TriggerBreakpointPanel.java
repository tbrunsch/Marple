package dd.kms.marple.impl.gui.debugsupport;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.evaluator.textfields.ExpressionInputTextField;
import dd.kms.zenodot.api.ParseException;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class TriggerBreakpointPanel extends JPanel implements Disposable
{
	private final InspectionContext			context;

	private final JLabel					expressionLabel = new JLabel("Expression:");
	private final JPanel					expressionPanel;
	private final ExpressionInputTextField	expressionTF;

	private final JLabel					descriptionLabel		= new JLabel("<html><p>Set a breakpoint in the specified method and click the 'Trigger' button</p></html>");
	private final JButton					triggerButton			= new JButton("Trigger");

	TriggerBreakpointPanel(Consumer<Throwable> exceptionConsumer, InspectionContext context) {
		super(new GridBagLayout());

		this.context = context;

		expressionTF = new ExpressionInputTextField(context);
		expressionTF.setExpression(context.getSettings().getTriggerBreakpointExpression());
		expressionTF.setExceptionConsumer(exceptionConsumer);
		expressionPanel = new EvaluationTextFieldPanel(expressionTF, context);

		setBorder(BorderFactory.createTitledBorder("Breakpoint Trigger"));

		int yPos = 0;

		add(expressionLabel,	new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(expressionPanel,	new GridBagConstraints(1, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		add(descriptionLabel,	new GridBagConstraints(0, yPos, 2, 1, 10.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
		add(triggerButton,		new GridBagConstraints(2, yPos++, REMAINDER, 1, 0.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));

		addListeners();
	}

	private void addListeners() {
		triggerButton.addActionListener(e -> triggerBreakpoint());
	}

	void setThisValue(Object thisValue) {
		expressionTF.setThisValue(thisValue);
	}

	private void triggerBreakpoint() {
		try {
			context.getSettings().setTriggerBreakpointExpression(expressionTF.getExpression());
			expressionTF.evaluateText();
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void dispose() {
		/* currently there is nothing to do */
	}
}
