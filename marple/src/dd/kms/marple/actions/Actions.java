package dd.kms.marple.actions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class Actions
{
	private static final int	MAX_NAME_LENGTH	= 60;

	public static String trimName(String name) {
		return name.length() > MAX_NAME_LENGTH
			? name.substring(0, MAX_NAME_LENGTH) + "..."
			: name;
	}

	public static boolean hasDefaultAction(ActionProvider actionProvider) {
		return actionProvider != null && actionProvider.getDefaultAction().isPresent();
	}

	public static void runDefaultAction(ActionProvider actionProvider) {
		Optional<InspectionAction> defaultAction = actionProvider.getDefaultAction();
		if (defaultAction.isPresent()) {
			defaultAction.get().perform();
		}
	}

	public static void showActionPopup(Component parent, ActionProvider actionProvider, MouseEvent e) {
		JPopupMenu popup = createActionPopup(actionProvider);
		popup.show(parent, e.getX(), e.getY());
	}

	private static JPopupMenu createActionPopup(ActionProvider actionProvider) {
		JPopupMenu popup = new JPopupMenu();
		InspectionAction defaultAction = actionProvider.getDefaultAction().orElse(null);
		for (InspectionAction action : actionProvider.getActions()) {
			JMenuItem actionItem = new JMenuItem(new ActionWrapper(action));
			Font font = actionItem.getFont().deriveFont(action == defaultAction ? Font.BOLD : Font.PLAIN);
			actionItem.setFont(font);
			popup.add(actionItem);
		}
		return popup;
	}
}
