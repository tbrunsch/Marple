package dd.kms.marple.gui.actionprovidertree;

import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.Actions;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class ActionProviderTreeMouseMotionListener extends MouseMotionAdapter
{
	@Override
	public void mouseMoved(MouseEvent e) {
		ActionProvider actionProvider = ActionProviderTreeNodes.getActionProvider(e);
		Cursor cursor = Actions.hasDefaultAction(actionProvider)
				? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
				: Cursor.getDefaultCursor();
		e.getComponent().setCursor(cursor);
	}
}
