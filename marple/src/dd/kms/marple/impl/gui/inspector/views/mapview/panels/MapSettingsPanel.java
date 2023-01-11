package dd.kms.marple.impl.gui.inspector.views.mapview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.evaluator.textfields.LambdaExpressionInputTextField;
import dd.kms.marple.impl.gui.inspector.views.mapview.MapOperationExecutor;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.MapSettings;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.OperationSettings;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class MapSettingsPanel extends AbstractOperationSettingsPanel
{
	private final JLabel							keyMappingLabel		= new JLabel("Key mapping:");
	private final JPanel							keyMappingPanel;
	private final LambdaExpressionInputTextField	keyMappingTF;

	private final JLabel							valueMappingLabel	= new JLabel("Value mapping:");
	private final JPanel							valueMappingPanel;
	private final LambdaExpressionInputTextField	valueMappingTF;

	MapSettingsPanel(Class<?> commonKeyType, Class<?> commonValueType, InspectionContext context) {
		keyMappingTF = new LambdaExpressionInputTextField(MapOperationExecutor.FUNCTIONAL_INTERFACE_KEYS, context);
		keyMappingPanel = new EvaluationTextFieldPanel(keyMappingTF, context);
		keyMappingTF.setParameterTypes(commonKeyType);
		keyMappingTF.setExpression("x -> x");

		valueMappingTF = new LambdaExpressionInputTextField(MapOperationExecutor.FUNCTIONAL_INTERFACE_VALUES, context);
		valueMappingPanel = new EvaluationTextFieldPanel(valueMappingTF, context);
		valueMappingTF.setParameterTypes(commonValueType);
		valueMappingTF.setExpression("x -> x");

		int yPos = 0;
		int xPos = 0;
		add(keyMappingLabel,	new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(keyMappingPanel,	new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(valueMappingLabel,	new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(valueMappingPanel,	new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
	}

	@Override
	void setMapType(Class<? extends Map> mapType) {
		keyMappingTF.setThisType(mapType);
		valueMappingTF.setThisType(mapType);
	}

	@Override
	void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
		keyMappingTF.setExceptionConsumer(exceptionConsumer);
		valueMappingTF.setExceptionConsumer(exceptionConsumer);
	}

	@Override
	void setAction(Runnable action) {
		keyMappingTF.setEvaluationResultConsumer(ignored -> action.run());
		valueMappingTF.setEvaluationResultConsumer(ignored -> action.run());
	}

	@Override
	OperationSettings getSettings() {
		String keyMappingExpression = keyMappingTF.getText();
		String valueMappingExpression = valueMappingTF.getText();
		return new MapSettings(keyMappingExpression, valueMappingExpression);
	}

	@Override
	void setSettings(OperationSettings settings) {
		MapSettings mapSettings = (MapSettings) settings;
		keyMappingTF.setText(mapSettings.getKeyMappingExpression());
		valueMappingTF.setText(mapSettings.getValueMappingExpression());
	}
}
