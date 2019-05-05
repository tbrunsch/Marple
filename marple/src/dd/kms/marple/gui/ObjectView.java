package dd.kms.marple.gui;

import java.awt.*;

public interface ObjectView
{
	String getViewName();
	Component getViewComponent();
	Object getViewSettings();
	void applyViewSettings(Object settings);
}
