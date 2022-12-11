package dd.kms.marple.impl.gui.common;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.gui.actionproviders.ActionProviderListeners;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;

public class CurrentObjectPanel extends JPanel
{
	private final JLabel			classInfoLabel		= new JLabel();
	private final JLabel			toStringLabel		= new JLabel();

	private final InspectionContext	context;

	private Object					currentObject;

	public CurrentObjectPanel(InspectionContext context) {
		super(new GridBagLayout());
		this.context = context;

		setBorder(BorderFactory.createTitledBorder("Context"));

		add(toStringLabel,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, DEFAULT_INSETS, 0, 0));
		add(classInfoLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, DEFAULT_INSETS, 0, 0));

		ActionProviderListeners.addMouseListeners(toStringLabel, e -> createActionProvider());
	}

	public Object getCurrentObject() {
		return currentObject;
	}

	public void setCurrentObject(Object currentObject) {
		this.currentObject = currentObject;

		toStringLabel.setText(getDisplayText());
		classInfoLabel.setText(currentObject != null ? context.getDisplayText(currentObject.getClass()) : null);
	}

	private String getDisplayText() {
		return context.getDisplayText(currentObject);
	}

	private ActionProvider createActionProvider() {
		return new ActionProviderBuilder(getDisplayText(), currentObject, context)
			.executeDefaultAction(true)
			.build();
	}
}
