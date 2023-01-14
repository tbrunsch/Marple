package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.evaluator.textfields.LambdaExpressionInputTextField;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

abstract class AbstractMapSettingsPanel extends AbstractOperationSettingsPanel
{
	private final JLabel							mappingLabel		= new JLabel("Mapping:");
	private final JPanel							mappingPanel;
	private final LambdaExpressionInputTextField	mappingTF;

	AbstractMapSettingsPanel(Class<?> functionalInterface, Class<?> commonElementType, InspectionContext context) {
		mappingTF = new LambdaExpressionInputTextField(functionalInterface, context);
		mappingPanel = new EvaluationTextFieldPanel(mappingTF, context);
		mappingTF.setParameterTypes(commonElementType);
		mappingTF.setExpression("x -> x");

		int yPos = 0;
		int xPos = 0;
		add(mappingLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(mappingPanel,		new GridBagConstraints(xPos++, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
	}

	@Override
	void setIterableType(Class<?> iterableType) {
		mappingTF.setThisType(iterableType);
	}

	@Override
	void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
		mappingTF.setExceptionConsumer(exceptionConsumer);
	}

	@Override
	void setAction(Runnable action) {
		mappingTF.setEvaluationResultConsumer(ignored -> action.run());
	}

	String getMappingExpression() {
		return mappingTF.getText();
	}

	void setMappingExpression(String mappingExpression) {
		mappingTF.setText(mappingExpression);
	}
}
