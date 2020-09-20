package dd.kms.marple.api.settings.visual;

import java.awt.*;

public interface ObjectView
{
	String getViewName();
	Component getViewComponent();
	Object getViewSettings();
	void applyViewSettings(Object settings);
}
