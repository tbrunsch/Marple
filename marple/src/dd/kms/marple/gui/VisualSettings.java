package dd.kms.marple.gui;

import dd.kms.marple.InspectionContext;

import java.util.List;

/**
 *
 * @param <C>	GUI component class
 */
public interface VisualSettings<C>
{
	String getDisplayText(Object object);
	List<ObjectView<C>> getInspectionViews(Object object, InspectionContext<C> inspectionContext);
}
