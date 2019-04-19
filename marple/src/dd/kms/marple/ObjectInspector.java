package dd.kms.marple;

import java.util.List;

/**
 *
 * @param <C>	GUI component class
 */
public interface ObjectInspector<C>
{
	void setInspectionContext(InspectionContext<C> inspectionContext);
	void inspectComponent(List<C> componentHierarchy, List<?> subcomponentHierarchy);
	void inspectObject(Object object);
	void highlightComponent(C component);
}
