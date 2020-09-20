package dd.kms.marple.impl.settings.components;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.settings.components.ComponentHierarchy;

import java.util.List;

public class ComponentHierarchyImpl implements ComponentHierarchy
{
	private static final int	LAST_INDEX	= -1;

	private final List<Object>	components;
	private final int			selectedIndex;

	public ComponentHierarchyImpl(List<Object> components) {
		this(components, LAST_INDEX);
	}

	public ComponentHierarchyImpl(List<Object> components, int selectedIndex) {
		this.components = ImmutableList.copyOf(components);
		this.selectedIndex = selectedIndex;
	}

	public ComponentHierarchyImpl(ComponentHierarchy componentHierarchy) {
		this(componentHierarchy.getComponents(), componentHierarchy.getSelectedIndex());
	}

	@Override
	public List<Object> getComponents() {
		return components;
	}

	@Override
	public int getSelectedIndex() {
		return selectedIndex == LAST_INDEX ? components.size() - 1 : selectedIndex;
	}

	@Override
	public Object getSelectedComponent() {
		return components.get(getSelectedIndex());
	}
}
