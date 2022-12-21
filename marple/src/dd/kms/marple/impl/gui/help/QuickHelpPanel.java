package dd.kms.marple.impl.gui.help;

import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.api.settings.keys.KeySettings;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class QuickHelpPanel extends JPanel implements Disposable
{
	public static final String	TITLE						= "Marple Quick Help";

	private static final String INSPECTION_TITLE			= "Inspect";
	private static final String INSPECTION_DESCRIPTION		= "Open the inspection dialog for the component under the mouse";
	private static final String INSPECTION_INFO				= "Use this feature to analyze your GUI and any data that is connected to it.";
	private static final String EVALUATION_TITLE			= "Evaluate";
	private static final String EVALUATION_DESCRIPTION		= "Evaluate an expression";
	private static final String EVALUATION_INFO				= "Use the literal \"this\" to refer to the component under the mouse.";
	private static final String FIND_INSTANCES_TITLE		= "Find Instances";
	private static final String FIND_INSTANCES_DESCRIPTION	= "Find a concrete instance or all instances of a class that match a certain filter";
	private static final String FIND_INSTANCES_INFO			= "The search starts at the component under the mouse.";
	private static final String DEBUG_SUPPORT_TITLE			= "Debug Support";
	private static final String DEBUG_SUPPORT_DESCRIPTION	= "Open the debug support dialog";
	private static final String DEBUG_SUPPORT_INFO			= "Use this feature for exchanging data with your debugger or to trigger breakpoints.";

	private int yPos;

	public QuickHelpPanel(KeySettings keySettings) {
		super(new GridBagLayout());

		addCategory(INSPECTION_TITLE,		INSPECTION_DESCRIPTION,		INSPECTION_INFO,		keySettings.getInspectionKey());
		addCategory(EVALUATION_TITLE,		EVALUATION_DESCRIPTION,		EVALUATION_INFO,		keySettings.getEvaluationKey());
		addCategory(FIND_INSTANCES_TITLE,	FIND_INSTANCES_DESCRIPTION,	FIND_INSTANCES_INFO,	keySettings.getFindInstancesKey());
		addCategory(DEBUG_SUPPORT_TITLE,	DEBUG_SUPPORT_DESCRIPTION,	DEBUG_SUPPORT_INFO,		keySettings.getDebugSupportKey());

		JPanel evaluationKeysPanel = new EvaluationKeysPanel(keySettings);
		add(evaluationKeysPanel, new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
	}

	private void addCategory(String title, String description, String info, KeyRepresentation key) {
		String htmlDescription = "<html><p>" + description + "</p></html>";
		String htmlInfo = "<html><p>" + info + "</p></html>";

		JPanel categoryPanel = new CategoryPanel(title, key);
		JPanel descriptionPanel = new DescriptionPanel(htmlDescription, htmlInfo);

		add(categoryPanel,		new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
		add(descriptionPanel,	new GridBagConstraints(1, yPos++, 1, 1, 1.0, 0.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
	}

	@Override
	public void dispose() {
		/* Currently there is nothing to do */
	}
}
