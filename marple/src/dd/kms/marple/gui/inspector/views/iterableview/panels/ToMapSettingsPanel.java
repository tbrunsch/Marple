package dd.kms.marple.gui.inspector.views.iterableview.panels;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.evaluator.textfields.CompiledExpressionInputTextField;
import dd.kms.marple.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.gui.inspector.views.iterableview.settings.OperationSettings;
import dd.kms.marple.gui.inspector.views.iterableview.settings.ToMapSettings;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class ToMapSettingsPanel extends AbstractOperationSettingsPanel
{
	private final JLabel							keyMappingLabel			= new JLabel("Key mapping:");
	private final JPanel							keyMappingPanel;
	private final CompiledExpressionInputTextField	keyMappingTF;

	private final JLabel							valueMappingLabel		= new JLabel("Value mapping:");
	private final JPanel							valueMappingPanel;
	private final CompiledExpressionInputTextField	valueMappingTF;

	private final JLabel 							mappingInfoLabel		= new JLabel("'this' always refers to the element currently processed");

	ToMapSettingsPanel(TypeInfo commonElementType, InspectionContext context) {
		keyMappingTF = new CompiledExpressionInputTextField(context);
		keyMappingPanel = new EvaluationTextFieldPanel(keyMappingTF, context);
		keyMappingTF.setThisType(commonElementType);
		keyMappingTF.setExpression("this");

		valueMappingTF = new CompiledExpressionInputTextField(context);
		valueMappingPanel = new EvaluationTextFieldPanel(valueMappingTF, context);
		valueMappingTF.setThisType(commonElementType);
		valueMappingTF.setExpression("1");

		int yPos = 0;
		int xPos = 0;
		add(keyMappingLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(keyMappingPanel,		new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(valueMappingLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(valueMappingPanel,		new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(mappingInfoLabel,	new GridBagConstraints(xPos++, yPos++, REMAINDER, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
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
		return new ToMapSettings(keyMappingExpression, valueMappingExpression);
	}

	@Override
	void setSettings(OperationSettings settings) {
		ToMapSettings toMapSettings = (ToMapSettings) settings;
		keyMappingTF.setText(toMapSettings.getKeyMappingExpression());
		valueMappingTF.setText(toMapSettings.getValueMappingExpression());
	}
}
