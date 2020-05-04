package dd.kms.marple.gui.common;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.gui.actionproviders.ActionProviderListeners;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;

public class CurrentObjectPanel extends JPanel
{
	private final JLabel			classInfoLabel		= new JLabel();
	private final JLabel			toStringLabel		= new JLabel();

	private final InspectionContext	inspectionContext;

	private ObjectInfo				currentObject;

	public CurrentObjectPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());
		this.inspectionContext = inspectionContext;

		setBorder(BorderFactory.createTitledBorder("Context"));

		add(toStringLabel,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, DEFAULT_INSETS, 0, 0));
		add(classInfoLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, DEFAULT_INSETS, 0, 0));

		ActionProviderListeners.addMouseListeners(toStringLabel, e -> createActionProvider());
	}

	public void setCurrentObject(ObjectInfo currentObject) {
		this.currentObject = currentObject;

		toStringLabel.setText(getDisplayText());
		classInfoLabel.setText(ReflectionUtils.getRuntimeTypeInfo(currentObject).toString());
	}

	private String getDisplayText() {
		return inspectionContext.getDisplayText(currentObject);
	}

	private ActionProvider createActionProvider() {
		return new ActionProviderBuilder(getDisplayText(), currentObject, inspectionContext)
			.executeDefaultAction(true)
			.build();
	}
}
