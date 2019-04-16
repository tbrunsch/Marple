package dd.kms.marple.gui;

import dd.kms.marple.InspectionContext;

import java.util.List;

/**
 *
 * @param <C>	GUI component class
 * @param <V>	View class (GUI component class plus name)
 */
public interface VisualSettings<C, V>
{
	String getDisplayText(Object object);
	List<V> getInspectionViews(Object object, InspectionContext<C, V> inspectionContext);
}
