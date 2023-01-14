package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.evaluator.textfields.LambdaExpressionInputTextField;
import dd.kms.marple.impl.gui.inspector.views.iterableview.ForEachOperationExecutor;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.ForEachSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class ForEachSettingsPanel extends AbstractOperationSettingsPanel
{
	private final JLabel							consumerLabel			= new JLabel("Consumer:");
	private final JPanel							consumerPanel;
	private final LambdaExpressionInputTextField	consumerTF;

	ForEachSettingsPanel(Class<?> commonElementType, InspectionContext context) {
		consumerTF = new LambdaExpressionInputTextField(ForEachOperationExecutor.FUNCTIONAL_INTERFACE, context);
		consumerPanel = new EvaluationTextFieldPanel(consumerTF, context);
		consumerTF.setParameterTypes(commonElementType);
		consumerTF.setExpression("x -> System.out.println(x)");

		int yPos = 0;
		int xPos = 0;
		add(consumerLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(consumerPanel,		new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
	}

	@Override
	void setIterableType(Class<?> iterableType) {
		consumerTF.setThisType(iterableType);
	}

	@Override
	void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
		consumerTF.setExceptionConsumer(exceptionConsumer);
	}

	@Override
	void setAction(Runnable action) {
		consumerTF.setEvaluationResultConsumer(ignored -> action.run());
	}

	@Override
	OperationSettings getSettings() {
		String consumerExpression = consumerTF.getText();
		return new ForEachSettings(consumerExpression);
	}

	@Override
	void setSettings(OperationSettings settings) {
		ForEachSettings forEachSettings = (ForEachSettings) settings;
		consumerTF.setText(forEachSettings.getConsumerExpression());
	}
}
