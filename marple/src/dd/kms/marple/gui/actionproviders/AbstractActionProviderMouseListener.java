package dd.kms.marple.gui.actionproviders;

import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.Actions;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

abstract class AbstractActionProviderMouseListener extends MouseAdapter
{
	abstract ActionProvider getActionProvider(MouseEvent e);

	@Override
	public void mousePressed(MouseEvent e) {
		ActionProvider actionProvider = getActionProvider(e);
		if (actionProvider != null) {
			if (e.isPopupTrigger()) {
				Actions.showActionPopup(e.getComponent(), actionProvider, e);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		ActionProvider actionProvider = getActionProvider(e);
		if (actionProvider != null) {
			if (SwingUtilities.isLeftMouseButton(e) && Actions.hasDefaultAction(actionProvider)) {
				Actions.performDefaultAction(actionProvider);
			} else if (e.isPopupTrigger()) {
				Actions.showActionPopup(e.getComponent(), actionProvider, e);
			}
		}
	}
}
