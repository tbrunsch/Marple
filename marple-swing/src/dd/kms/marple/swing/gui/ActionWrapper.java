package dd.kms.marple.swing.gui;

import dd.kms.marple.actions.InspectionAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionWrapper extends AbstractAction
{
	private final InspectionAction action;

	public ActionWrapper(InspectionAction action) {
		super(Actions.trimName(action.getName()));
		putValue(Action.SHORT_DESCRIPTION, action.getDescription());
		this.action = action;
	}

	@Override
	public boolean isEnabled() {
		return action.isEnabled();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		action.perform();
	}
}
