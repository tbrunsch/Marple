package dd.kms.marple.swing.gui.actionprovidertree;

import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.swing.actions.Actions;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ActionProviderTreeMouseListener extends MouseAdapter
{
	@Override
	public void mouseClicked(MouseEvent e) {
		ActionProvider actionProvider = ActionProviderTreeNodes.getActionProvider(e);
		if (actionProvider != null) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				Actions.runDefaultAction(actionProvider);
			} else if (SwingUtilities.isRightMouseButton(e)) {
				Actions.showActionPopup(e.getComponent(), actionProvider, e);
			}
		}
	}
}
