package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.CompiledExpressionInputTextField;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.CollectSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class CollectSettingsPanel extends AbstractOperationSettingsPanel
{
	private final JLabel							constructorLabel		= new JLabel("Constructor expression:");
	private final JPanel							constructorPanel;
	private final CompiledExpressionInputTextField	constructorTF;
	private final JLabel 							constructorInfoLabel	= new JLabel("Create a Collection or an array for holding the elements");

	CollectSettingsPanel(TypeInfo commonElementType, InspectionContext context) {
		constructorTF = new CompiledExpressionInputTextField(context);
		constructorPanel = new EvaluationTextFieldPanel(constructorTF, context);
		constructorTF.setThisType(commonElementType);
		constructorTF.setExpression("new java.util.ArrayList()");

		int yPos = 0;
		int xPos = 0;
		add(constructorLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(constructorPanel,		new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 1.0, NORTHWEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(constructorInfoLabel,	new GridBagConstraints(xPos++, yPos++, REMAINDER, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
	}

	@Override
	void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
		constructorTF.setExceptionConsumer(exceptionConsumer);
	}

	@Override
	void setAction(Runnable action) {
		constructorTF.setEvaluationResultConsumer(ignored -> action.run());
	}

	@Override
	OperationSettings getSettings() {
		String constructorExpression = constructorTF.getText();
		return new CollectSettings(constructorExpression);
	}

	@Override
	void setSettings(OperationSettings settings) {
		CollectSettings collectSettings = (CollectSettings) settings;
		constructorTF.setText(collectSettings.getConstructorExpression());
	}
}
