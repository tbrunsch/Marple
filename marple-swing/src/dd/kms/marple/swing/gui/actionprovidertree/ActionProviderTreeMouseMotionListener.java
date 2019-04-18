package dd.kms.marple.swing.gui.actionprovidertree;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class ActionProviderTreeMouseMotionListener extends MouseMotionAdapter
{
	@Override
	public void mouseMoved(MouseEvent e) {
		Cursor cursor = ActionProviderTreeNodes.getActionProvider(e) == null
				? Cursor.getDefaultCursor()
				: Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		e.getComponent().setCursor(cursor);
	}
}
