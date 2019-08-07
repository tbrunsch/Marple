package dd.kms.marple.gui.common;

import javax.swing.*;
import java.awt.*;

public class GuiCommons
{
	public static final Insets	DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	public static void setFontStyle(JComponent component, int fontStyle) {
		component.setFont(component.getFont().deriveFont(fontStyle));
	}
}
