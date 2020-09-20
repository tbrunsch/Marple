package dd.kms.marple.api.settings.components;

import java.util.List;

public interface ComponentHierarchy
{
	List<Object> getComponents();
	int getSelectedIndex();
	Object getSelectedComponent();
}
