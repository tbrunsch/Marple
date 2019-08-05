package dd.kms.marple.gui.actionproviders;

import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.Actions;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

abstract class AbstractActionProviderMouseMotionListener extends MouseMotionAdapter
{
	abstract ActionProvider getActionProvider(MouseEvent e);

	@Override
	public void mouseMoved(MouseEvent e) {
		ActionProvider actionProvider = getActionProvider(e);
		Cursor cursor = Actions.hasDefaultAction(actionProvider)
				? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
				: Cursor.getDefaultCursor();
		e.getComponent().setCursor(cursor);

		Actions.performImmediateActions(actionProvider);
	}
}
