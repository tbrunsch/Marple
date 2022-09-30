package dd.kms.marple.impl.gui.common;

import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.zenodot.api.common.AccessModifier;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public class GuiCommons
{
	public static final int		DEFAULT_DISTANCE	= 5;
	public static final Insets	DEFAULT_INSETS		= new Insets(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE);

	private static final Map<AccessModifier, Color>	MODIFIER_COLORS = new HashMap<>();

	static {
		MODIFIER_COLORS.put(AccessModifier.PUBLIC,			Color.GREEN);
		MODIFIER_COLORS.put(AccessModifier.PROTECTED,		Color.YELLOW);
		MODIFIER_COLORS.put(AccessModifier.PACKAGE_PRIVATE,	Color.ORANGE);
		MODIFIER_COLORS.put(AccessModifier.PRIVATE,			Color.RED);
	}

	public static Color getAccessModifierColor(AccessModifier accessModifier) {
		return MODIFIER_COLORS.get(accessModifier);
	}

	public static String formatClass(Class<?> clazz) {
		Class<?> componentType = clazz.getComponentType();
		if (componentType != null) {
			return formatClass(componentType) + "[]";
		}
		String qualifiedName = clazz.getName();
		return qualifiedName.startsWith("java.lang.") ? clazz.getSimpleName() : qualifiedName;
	}

	public static void setFontStyle(JComponent component, int fontStyle) {
		component.setFont(component.getFont().deriveFont(fontStyle));
	}

	public static <T extends JComponent> void installKeyHandler(T component, KeyRepresentation key, String actionName, @Nullable Runnable keyHandler) {
		Action action = keyHandler == null ? null : new AbstractAction(actionName) {
			@Override
			public void actionPerformed(ActionEvent e) {
				keyHandler.run();
			}
		};
		InputMap inputMap = component.getInputMap();
		inputMap.put(key.asKeyStroke(), actionName);
		ActionMap actionMap = component.getActionMap();
		actionMap.put(actionName, action);
	}

	public static Point getMousePositionOnScreen() {
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		return pointerInfo != null ? pointerInfo.getLocation() : null;
	}
}
