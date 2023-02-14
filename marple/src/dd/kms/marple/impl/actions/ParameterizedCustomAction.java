package dd.kms.marple.impl.actions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.actions.InspectionAction;
import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;

import javax.swing.*;
import java.awt.*;

public class ParameterizedCustomAction implements InspectionAction
{
	private final InspectionContext context;
	private final CustomAction		customAction;
	private final Object			thisValue;

	public ParameterizedCustomAction(InspectionContext context, CustomAction customAction, Object thisValue) {
		this.context = context;
		this.customAction = customAction;
		this.thisValue = thisValue;
	}

	@Override
	public boolean isDefaultAction() {
		return false;
	}

	@Override
	public String getName() {
		return customAction.getName();
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return customAction.getThisClass().isInstance(thisValue);
	}

	@Override
	public void perform() {
		String expression = customAction.getActionExpression();
		ExpressionParser expressionParser = Parsers.createExpressionParserBuilder(context.getEvaluator().getParserSettings())
			.createExpressionParser();
		try {
			expressionParser.evaluate(expression, thisValue);
		} catch (ParseException e) {
			JOptionPane.showMessageDialog(thisValue instanceof Component ? (Component) thisValue : null,
				e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
