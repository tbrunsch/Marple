package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ActionWrapper extends AbstractAction
{
	private static final int	MAX_DESCRIPTION_LENGTH	= 100;

	private final InspectionAction action;

	public ActionWrapper(InspectionAction action) {
		super(Actions.trimName(action.getName()));
		String description = action.getDescription();
		if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
			description = description.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
		}
		putValue(Action.SHORT_DESCRIPTION, description);
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
