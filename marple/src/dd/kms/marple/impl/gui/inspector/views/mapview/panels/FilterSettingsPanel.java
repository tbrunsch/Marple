package dd.kms.marple.impl.gui.inspector.views.mapview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.evaluator.textfields.LambdaExpressionInputTextField;
import dd.kms.marple.impl.gui.inspector.views.mapview.FilterOperationExecutor;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.FilterSettings;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.OperationSettings;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class FilterSettingsPanel extends AbstractOperationSettingsPanel
{
	private final JLabel							keyFilterLabel		= new JLabel("Key filter condition:");
	private final JPanel							keyFilterPanel;
	private final LambdaExpressionInputTextField	keyFilterTF;

	private final JLabel							valueFilterLabel	= new JLabel("Value filter condition:");
	private final JPanel							valueFilterPanel;
	private final LambdaExpressionInputTextField	valueFilterTF;

	FilterSettingsPanel(Class<?> commonKeyType, Class<?> commonValueType, InspectionContext context) {
		keyFilterTF = new LambdaExpressionInputTextField(FilterOperationExecutor.FUNCTIONAL_INTERFACE_KEYS, context);
		keyFilterPanel = new EvaluationTextFieldPanel(keyFilterTF, context);
		keyFilterTF.setParameterTypes(commonKeyType);
		keyFilterTF.setExpression("x -> true");

		valueFilterTF = new LambdaExpressionInputTextField(FilterOperationExecutor.FUNCTIONAL_INTERFACE_VALUES, context);
		valueFilterPanel = new EvaluationTextFieldPanel(valueFilterTF, context);
		valueFilterTF.setParameterTypes(commonValueType);
		valueFilterTF.setExpression("x -> true");

		int yPos = 0;
		int xPos = 0;
		add(keyFilterLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(keyFilterPanel,		new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(valueFilterLabel,	new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(valueFilterPanel,	new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
	}

	@Override
	void setMapType(Class<? extends Map> mapType) {
		keyFilterTF.setThisType(mapType);
		valueFilterTF.setThisType(mapType);
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
