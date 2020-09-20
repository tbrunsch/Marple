package dd.kms.marple.impl.gui.inspector.views.iterableview.panels;

import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.evaluator.textfields.CompiledExpressionInputTextField;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.FilterResultType;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.FilterSettings;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class FilterSettingsPanel extends AbstractOperationSettingsPanel
{
	private final JLabel								resultTypeLabel			= new JLabel("Result:");
	private final JToggleButton							listTB					= new JToggleButton();
	private final JToggleButton							indexMapTB				= new JToggleButton();
	private final ButtonGroup							resultTypeButtonGroup	= new ButtonGroup();
	private final Map<FilterResultType, JToggleButton>	resultTypeToButton		= ImmutableMap.<FilterResultType, JToggleButton>builder()
																					.put(FilterResultType.LIST, listTB)
																					.put(FilterResultType.INDEX_MAP, indexMapTB)
																					.build();

	private final JLabel								filterLabel				= new JLabel("Filter condition:");
	private final JPanel								filterPanel;
	private final CompiledExpressionInputTextField		filterTF;
	private final JLabel 								filterInfoLabel			= new JLabel("'this' always refers to the element currently processed");

	FilterSettingsPanel(TypeInfo commonElementType, InspectionContext context) {
		filterTF = new CompiledExpressionInputTextField(context);
		filterPanel = new EvaluationTextFieldPanel(filterTF, context);
		filterTF.setThisType(commonElementType);
		filterTF.setExpression("true");

		for (FilterResultType resultType : resultTypeToButton.keySet()) {
			JToggleButton button = resultTypeToButton.get(resultType);
			button.setText(resultType.toString());
			resultTypeButtonGroup.add(button);
		}
		listTB.setSelected(true);

		int yPos = 0;
		int xPos = 0;
		add(resultTypeLabel,	new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(listTB,				new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(indexMapTB,			new GridBagConstraints(xPos++, yPos++, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(filterLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		add(filterPanel,		new GridBagConstraints(xPos++, yPos++, 2, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		xPos = 0;
		add(filterInfoLabel,	new GridBagConstraints(xPos++, yPos++, REMAINDER, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
	}

	@Override
	void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
		filterTF.setExceptionConsumer(exceptionConsumer);
	}

	@Override
	void setAction(Runnable action) {
		filterTF.setEvaluationResultConsumer(ignored -> action.run());
	}

	@Override
	OperationSettings getSettings() {
		FilterResultType resultType = indexMapTB.isSelected() ? FilterResultType.INDEX_MAP : FilterResultType.LIST;
		String filterExpression = filterTF.getText();
		return new FilterSettings(resultType, filterExpression);
	}

	@Override
	void setSettings(OperationSettings settings) {
		FilterSettings filterSettings = (FilterSettings) settings;
		JToggleButton button = resultTypeToButton.get(filterSettings.getResultType());
		button.setSelected(true);
		filterTF.setText(filterSettings.getFilterExpression());
	}
}
