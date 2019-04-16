package dd.kms.marple;

import java.util.List;

/**
 *
 * @param <C>	GUI component class
 * @param <V>	View class (GUI component class plus name)
 */
public interface ObjectInspector<C, V>
{
	void setInspectionContext(InspectionContext<C, V> inspectionContext);
	void inspectComponent(List<C> componentHierarchy, List<?> subcomponentHierarchy);
	void inspectObject(Object object);
}
