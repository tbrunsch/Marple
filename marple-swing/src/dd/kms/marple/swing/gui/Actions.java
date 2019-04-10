package dd.kms.marple.swing.gui;

import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class Actions
{
	private static final int	MAX_NAME_LENGTH	= 40;

	public static String trimName(String name) {
		return name.length() > MAX_NAME_LENGTH
			? name.substring(0, MAX_NAME_LENGTH) + "..."
			: name;
	}

	public static void runDefaultAction(ActionProvider actionProvider) {
		List<InspectionAction> actions = actionProvider.getActions();
		if (!actions.isEmpty()) {
			InspectionAction defaultAction = actions.get(0);
			defaultAction.perform();
		}
	}

	public static void showActionPopup(Component parent, ActionProvider actionProvider, MouseEvent e) {
		JPopupMenu popup = createActionPopup(actionProvider);
		popup.show(parent, e.getX(), e.getY());
	}

	private static JPopupMenu createActionPopup(ActionProvider actionProvider) {
		JPopupMenu popup = new JPopupMenu();
		boolean defaultAction = true;
		for (InspectionAction action : actionProvider.getActions()) {
			JMenuItem actionItem = new JMenuItem(new ActionWrapper(action));
			Font font = actionItem.getFont().deriveFont(defaultAction ? Font.BOLD : Font.PLAIN);
			actionItem.setFont(font);
			popup.add(actionItem);
			defaultAction = false;
		}
		return popup;
	}
}
