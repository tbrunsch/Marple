package dd.kms.marple.impl.actions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;

import javax.swing.*;
import java.awt.*;

public class TriggerBreakpointAction implements InspectionAction
{
	private final InspectionContext	context;
	private final Object			thisValue;

	public TriggerBreakpointAction(InspectionContext context, Object thisValue) {
		this.context = context;
		this.thisValue = thisValue;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return "Trigger breakpoint";
	}

	@Override
	public String getDescription() {
		return "Evaluates the specified breakpoint trigger expression (see Debug Support)";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		String triggerBreakpointExpression = context.getSettings().getTriggerBreakpointExpression();
		ExpressionParser expressionParser = Parsers.createExpressionParserBuilder(context.getEvaluator().getParserSettings())
			.createExpressionParser();
		try {
			expressionParser.evaluate(triggerBreakpointExpression, thisValue);
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(thisValue instanceof Component ? (Component) thisValue : null,
				e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
