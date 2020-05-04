package dd.kms.marple;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ComponentHierarchy
{
	private static final int	LAST_INDEX	= -1;

	private final List<Object>	components;
	private final int			selectedIndex;

	public ComponentHierarchy(List<Object> components) {
		this(components, LAST_INDEX);
	}

	public ComponentHierarchy(List<Object> components, int selectedIndex) {
		this.components = ImmutableList.copyOf(components);
		this.selectedIndex = selectedIndex;
	}

	public ComponentHierarchy(ComponentHierarchy componentHierarchy) {
		this(componentHierarchy.getComponents(), componentHierarchy.getSelectedIndex());
	}

	public List<Object> getComponents() {
		return components;
	}

	public int getSelectedIndex() {
		return selectedIndex == LAST_INDEX ? components.size() - 1 : selectedIndex;
	}

	public Object getSelectedComponent() {
		return components.get(getSelectedIndex());
	}
}
