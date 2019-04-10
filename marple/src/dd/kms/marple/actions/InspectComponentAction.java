package dd.kms.marple.actions;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.ObjectInspector;

import java.util.List;

/**
 *
 * @param <C>	GUI component class
 */
public class InspectComponentAction<C> implements InspectionAction
{
	private final ObjectInspector<C>	inspector;
	private final List<C>				componentHierarchy;
	private final List<?>				subcomponentHierarchy;
	private final String				leafDisplayText;

	public InspectComponentAction(ObjectInspector<C> inspector, List<C> componentHierarchy, List<?> subcomponentHierarchy, String leafDisplayText) {
		this.inspector = inspector;
		this.componentHierarchy = ImmutableList.copyOf(componentHierarchy);
		this.subcomponentHierarchy = ImmutableList.copyOf(subcomponentHierarchy);
		this.leafDisplayText = leafDisplayText;
	}

	@Override
	public String getName() {
		return "Inspect "
			+ (subcomponentHierarchy.isEmpty() ? "component" : "subcomponent")
			+ " '"
			+ leafDisplayText
			+ "'";
	}

	@Override
	public String getDescription() {
		return "Inspect the (sub)component hierarchy of '" + leafDisplayText + "' in the object inspector";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void perform() {
		inspector.inspectComponent(componentHierarchy, subcomponentHierarchy);
	}
}
