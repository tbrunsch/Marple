package dd.kms.marple.impl.gui.inspector.views.mapview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.CompiledExpressionInputTextField;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.FilterSettings;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.OperationSettings;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class FilterSettingsPanel extends AbstractOperationSettingsPanel
{
	private final JLabel							keyFilterLabel		= new JLabel("Key filter condition:");
	private final JPanel							keyFilterPanel;
	private final CompiledExpressionInputTextField	keyFilterTF;

	private final JLabel							valueFilterLabel	= new JLabel("Value filter condition:");
	private final JPanel							valueFilterPanel;
	private final CompiledExpressionInputTextField	valueFilterTF;

	private final JLabel 							filterInfoLabel		= new JLabel("'this' always refers to the element currently processed");

	FilterSettingsPanel(TypeInfo commonKeyType, TypeInfo commonValueType, InspectionContext context) {
		keyFilterTF = new CompiledExpressionInputTextField(context);
		keyFilterPanel = new EvaluationTextFieldPanel(keyFilterTF, context);
		keyFilterTF.setThisType(commonKeyType);
		keyFilterTF.setExpression("true");

		valueFilterTF = new CompiledExpressionInputTextField(context);
		valueFilterPanel = new EvaluationTextFieldPanel(valueFilterTF, context);
		valueFilterTF.setThisType(commonValueType);
		valueFilterTF.setExpression("true");

		int yPos = 0;
		int xPos = 0;
		add(keyFilterLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(keyFilterPanel,		new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(valueFilterLabel,	new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(valueFilterPanel,	new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(filterInfoLabel,	new GridBagConstraints(xPos++, yPos++, REMAINDER, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
	}

	@Override
	void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
		keyFilterTF.setExceptionConsumer(exceptionConsumer);
		valueFilterTF.setExceptionConsumer(exceptionConsumer);
	}

	@Override
	void setAction(Runnable action) {
		keyFilterTF.setEvaluationResultConsumer(ignored -> action.run());
		valueFilterTF.setEvaluationResultConsumer(ignored -> action.run());
	}

	@Override
	OperationSettings getSettings() {
		String keyFilterExpression = keyFilterTF.getText();
		String valueFilterExpression = valueFilterTF.getText();
		return new FilterSettings(keyFilterExpression, valueFilterExpression);
	}

	@Override
	void setSettings(OperationSettings settings) {
		FilterSettings filterSettings = (FilterSettings) settings;
		keyFilterTF.setText(filterSettings.getKeyFilterExpression());
		valueFilterTF.setText(filterSettings.getValueFilterExpression());
	}
}
