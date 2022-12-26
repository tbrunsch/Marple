package dd.kms.marple.impl.gui.common;

import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.zenodot.api.common.AccessModifier;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class GuiCommons
{
	public static final int		DEFAULT_DISTANCE	= 5;
	public static final Insets	DEFAULT_INSETS		= new Insets(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE);

	private static final Color	TABLE_SELECTION_FOREGROUND;
	private static final Color	TABLE_SELECTION_BACKGROUND;

	private static final Map<AccessModifier, Color>	MODIFIER_COLORS = new HashMap<>();

	static {
		Color tableSelectionForeground = UIManager.getColor("Table.selectionForeground");
		if (tableSelectionForeground == null) {
			tableSelectionForeground = UIManager.getColor("textHighlightText");
		}
		TABLE_SELECTION_FOREGROUND = tableSelectionForeground;

		Color tableSelectionBackground = UIManager.getColor("Table.selectionBackground");
		if (tableSelectionBackground == null) {
			tableSelectionBackground = UIManager.getColor("textHighlight");
		}
		TABLE_SELECTION_BACKGROUND = tableSelectionBackground;

		MODIFIER_COLORS.put(AccessModifier.PUBLIC,			Color.GREEN);
		MODIFIER_COLORS.put(AccessModifier.PROTECTED,		Color.YELLOW);
		MODIFIER_COLORS.put(AccessModifier.PACKAGE_PRIVATE,	Color.ORANGE);
		MODIFIER_COLORS.put(AccessModifier.PRIVATE,			Color.RED);
	}

	public static Color getSelectedTableForegroundColor() {
		return TABLE_SELECTION_FOREGROUND;
	}

	public static Color getSelectedTableForegroundColor(Color desiredColor) {
		return blendColors(TABLE_SELECTION_FOREGROUND, desiredColor);
	}

	public static Color getSelectedTableBackgroundColor() {
		return TABLE_SELECTION_BACKGROUND;
	}

	public static Color getSelectedTableBackgroundColor(Color desiredColor) {
		return blendColors(TABLE_SELECTION_BACKGROUND, desiredColor);
	}

	public static Color getAccessModifierColor(AccessModifier accessModifier) {
		return MODIFIER_COLORS.get(accessModifier);
	}

	private static Color blendColors(Color c1, Color c2) {
		int r1 = c1.getRed();
		int g1 = c1.getGreen();
		int b1 = c1.getBlue();
		int r2 = c2.getRed();
		int g2 = c2.getGreen();
		int b2 = c2.getBlue();
		return new Color((r1 + r2) / 2, (g1 + g2) / 2, (b1 + b2) / 2);
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

	public static boolean isCaretPositionValid(String text, int caretPosition) {
		return text != null && 0 <= caretPosition && caretPosition <= text.length();
	}

	public static void reevaluateButtonAction(AbstractButton button) {
		Action action = button.getAction();
		button.setAction(null);
		button.setAction(action);
	}
}
