package dd.kms.marple.impl.gui.evaluator.completion;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dd.kms.marple.impl.gui.common.GuiCommons;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.common.GeneralizedField;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionField;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionMethod;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;

class IconFactory
{
	private static final Color	COLOR_VARIABLE			= Color.BLUE.brighter();
	private static final Color	COLOR_PACKAGE			= Color.ORANGE;
	private static final Color	COLOR_KEYWORD			= Color.BLUE;
	private static final Color	COLOR_CLASS				= Color.LIGHT_GRAY;

	private static final Table<CodeCompletionType, Color, Icon> CACHED_ICONS	= HashBasedTable.create();

	static synchronized Icon getIcon(CodeCompletion completion) {
		CodeCompletionType type = completion.getType();
		Color color = determineColor(completion);

		Icon icon = CACHED_ICONS.get(type, color);
		if (icon == null) {
			icon = new TypeIcon(type, color);
			CACHED_ICONS.put(type, color, icon);
		}
		return icon;
	}

	private static Color determineColor(CodeCompletion completion) {
		CodeCompletionType type = completion.getType();
		if (type == CodeCompletionType.VARIABLE) {
			return COLOR_VARIABLE;
		} else if (type == CodeCompletionType.FIELD) {
			CodeCompletionField fieldCompletion = (CodeCompletionField) completion;
			GeneralizedField field = fieldCompletion.getField();
			AccessModifier accessModifier = AccessModifier.getValue(field.getModifiers());
			return getColor(accessModifier);
		} else if (type == CodeCompletionType.METHOD) {
			CodeCompletionMethod methodCompletion = (CodeCompletionMethod) completion;
			Method method = methodCompletion.getMethod();
			AccessModifier accessModifier = AccessModifier.getValue(method.getModifiers());
			return getColor(accessModifier);
		} else if (type == CodeCompletionType.CLASS) {
			return COLOR_CLASS;
		} else if (type == CodeCompletionType.PACKAGE) {
			return COLOR_PACKAGE;
		} else if (type == CodeCompletionType.KEYWORD) {
			return COLOR_KEYWORD;
		}
		return COLOR_VARIABLE;
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

		private final CodeCompletionType	type;
		private final Color					color;

		private TypeIcon(CodeCompletionType type, Color color) {
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
