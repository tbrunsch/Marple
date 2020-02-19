package dd.kms.marple.gui.evaluator.completion;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dd.kms.marple.gui.common.GuiCommons;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestionType;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionClass;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionField;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionMethod;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.FieldInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

class IconFactory
{
	private static final Color	COLOR_VARIABLE			= Color.BLUE.brighter();
	private static final Color	COLOR_OBJECT_TREE_NODE	= COLOR_VARIABLE;
	private static final Color	COLOR_PACKAGE			= Color.ORANGE;
	private static final Color	COLOR_KEYWORD			= Color.BLUE;
	private static final Color	COLOR_CLASS				= Color.LIGHT_GRAY;

	private static final Table<CompletionSuggestionType, Color, Icon> CACHED_ICONS	= HashBasedTable.create();

	static synchronized Icon getIcon(CompletionSuggestion completionSuggestion) {
		CompletionSuggestionType type = completionSuggestion.getType();
		Color color = determineColor(completionSuggestion);

		Icon icon = CACHED_ICONS.get(type, color);
		if (icon == null) {
			icon = new TypeIcon(type, color);
			CACHED_ICONS.put(type, color, icon);
		}
		return icon;
	}

	private static Color determineColor(CompletionSuggestion completionSuggestion) {
		switch (completionSuggestion.getType()) {
			case VARIABLE:
				return COLOR_VARIABLE;
			case OBJECT_TREE_NODE:
				return COLOR_OBJECT_TREE_NODE;
			case FIELD: {
				CompletionSuggestionField fieldSuggestion = (CompletionSuggestionField) completionSuggestion;
				FieldInfo fieldInfo = fieldSuggestion.getFieldInfo();
				AccessModifier accessModifier = fieldInfo.getAccessModifier();
				return getColor(accessModifier);
			}
			case METHOD: {
				CompletionSuggestionMethod methodSuggestion = (CompletionSuggestionMethod) completionSuggestion;
				ExecutableInfo methodInfo = methodSuggestion.getMethodInfo();
				AccessModifier accessModifier = methodInfo.getAccessModifier();
				return getColor(accessModifier);
			}
			case CLASS:
				return COLOR_CLASS;
			case PACKAGE:
				return COLOR_PACKAGE;
			case KEYWORD:
				return COLOR_KEYWORD;
			default:
				return Color.WHITE;
		}
	}

	private static Color getColor(AccessModifier accessModifier) {
		return GuiCommons.getAccessModifierColor(accessModifier);
	}

	private static void drawCenteredText(String text, Rectangle2D bounds, Graphics2D g2d) {
		FontMetrics fm = g2d.getFontMetrics();
		Rectangle2D textBounds = fm.getStringBounds(text, g2d);
		double x = bounds.getX() + 0.5 * (bounds.getWidth() - textBounds.getWidth());
		double y = bounds.getY() + 0.5* (bounds.getHeight() - textBounds.getHeight()) + fm.getAscent();
		g2d.drawString(text, (int) x, (int) y);
	}

	private static class TypeIcon implements Icon
	{
		private static final int WIDTH	= 16;
		private static final int HEIGHT	= 16;

		private final CompletionSuggestionType	type;
		private final Color						color;

		private TypeIcon(CompletionSuggestionType type, Color color) {
			this.type = type;
			this.color = color;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			String firstChar = type.toString().substring(0, 1);

			Graphics2D g2d = (Graphics2D) g.create();
			try {
				Rectangle bounds = new Rectangle(x, y, x + WIDTH, y + HEIGHT);

				g2d.setColor(color);
				g2d.fillRect(x+1, y+1, WIDTH-2, HEIGHT-2);

				g2d.setColor(Color.BLACK);
				drawCenteredText(firstChar, bounds, g2d);
			} finally {
				g2d.dispose();
			}
		}

		@Override
		public int getIconWidth() {
			return WIDTH;
		}

		@Override
		public int getIconHeight() {
			return HEIGHT;
		}
	}
}
