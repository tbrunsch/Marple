package dd.kms.marple.impl.gui.help;

import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.keys.KeyFunction;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.api.settings.keys.KeySettings;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class QuickHelpPanel extends JPanel implements Disposable
{
	public static final String	TITLE						= "Marple Quick Help";

	private static final Map<KeyFunction, FunctionMetaData>	FUNCTION_META_DATA	= new LinkedHashMap<>();

	private static void registerFunction(KeyFunction function, String title, String description, String info) {
		FunctionMetaData metaData = new FunctionMetaData(title, description, info);
		FUNCTION_META_DATA.put(function, metaData);
	}

	static {
		registerFunction(KeyFunction.INSPECTION,
			"Inspect",
			"Open the inspection dialog for the component under the mouse",
			"Use this feature to analyze your GUI and any data that is connected to it."
		);
		registerFunction(KeyFunction.EVALUATION,
			"Evaluate",
			"Evaluate an expression",
			"Use the literal \"this\" to refer to the component under the mouse."
		);
		registerFunction(KeyFunction.FIND_INSTANCES,
			"Find Instances",
			"Find a concrete instance or all instances of a class that match a certain filter",
			"The search starts at the component under the mouse."
		);
		registerFunction(KeyFunction.DEBUG_SUPPORT,
			"Debug Support",
			"Open the debug support dialog",
			"Use this feature for exchanging data with your debugger."
		);
		registerFunction(KeyFunction.CUSTOM_ACTIONS,
			"Custom Actions",
			"Define custom actions including shortcuts",
			"Each action is defined via an expression."
		);
	}

	private int yPos;

	public QuickHelpPanel(KeySettings keySettings, CustomActionSettings customActionSettings) {
		super(new GridBagLayout());

		for (KeyFunction function : FUNCTION_META_DATA.keySet()) {
			FunctionMetaData metaData = FUNCTION_META_DATA.get(function);
			KeyRepresentation key = keySettings.getKey(function);
			addCategory(metaData, key);
		}

		JPanel evaluationKeysPanel = new EvaluationKeysPanel(keySettings);
		add(evaluationKeysPanel, new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));

		JPanel customActionKeysPanel = new CustomActionKeysPanel(customActionSettings);
		add(customActionKeysPanel, new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
	}

	private void addCategory(FunctionMetaData metaData, KeyRepresentation key) {
		String htmlDescription = "<html><p>" + metaData.description + "</p></html>";
		String htmlInfo = "<html><p>" + metaData.info + "</p></html>";

		JPanel categoryPanel = new CategoryPanel(metaData.title, key);
		JPanel descriptionPanel = new DescriptionPanel(htmlDescription, htmlInfo);

		add(categoryPanel,		new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
		add(descriptionPanel,	new GridBagConstraints(1, yPos++, 1, 1, 1.0, 0.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
	}

	@Override
	public void dispose() {
		/* Currently there is nothing to do */
	}

	private static class FunctionMetaData
	{
		final String	title;
		final String	description;
		final String	info;

		FunctionMetaData(String title, String description, String info) {
			this.title = title;
			this.description = description;
			this.info = info;
		}
	}
}
