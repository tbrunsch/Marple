package dd.kms.marple.impl.actions;

import dd.kms.marple.api.actions.InspectionAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class Actions
{
	private static final int	MAX_DISPLAY_TEXT_LENGTH	= 60;

	public static String trimDisplayText(String name) {
		return name.length() > MAX_DISPLAY_TEXT_LENGTH
			? name.substring(0, MAX_DISPLAY_TEXT_LENGTH) + "..."
			: name;
	}

	public static boolean hasDefaultAction(ActionProvider actionProvider) {
		return actionProvider != null && actionProvider.getDefaultAction().isPresent();
	}

	public static void performDefaultAction(ActionProvider actionProvider) {
		Optional<InspectionAction> defaultAction = actionProvider.getDefaultAction();
		if (defaultAction.isPresent()) {
			defaultAction.get().perform();
		}
	}

	public static void performImmediateActions(ActionProvider actionProvider) {
		if (actionProvider == null) {
			return;
		}
		actionProvider.getActions().stream()
			.filter(ImmediateInspectionAction.class::isInstance)
			.map(ImmediateInspectionAction.class::cast)
			.forEach(ImmediateInspectionAction::performImmediately);
	}

	public static void showActionPopup(ActionProvider actionProvider, MouseEvent e) {
		JPopupMenu popup = createActionPopup(actionProvider);
		popup.show(e.getComponent(), e.getX(), e.getY());
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
