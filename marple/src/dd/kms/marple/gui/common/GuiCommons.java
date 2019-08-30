package dd.kms.marple.gui.common;

import javax.swing.*;
import java.awt.*;

public class GuiCommons
{
	public static final int		DEFAULT_DISTANCE	= 5;
	public static final Insets	DEFAULT_INSETS		= new Insets(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE);

	public static void setFontStyle(JComponent component, int fontStyle) {
		component.setFont(component.getFont().deriveFont(fontStyle));
	}
}
