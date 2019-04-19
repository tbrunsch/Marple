package dd.kms.marple.settings;

import dd.kms.marple.ExpressionEvaluator;
import dd.kms.marple.ObjectInspector;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.gui.VisualSettings;

import java.util.function.Predicate;

/**
 *
 * @param <C>	GUI component class
 * @param <K>	KeyStroke class
 * @param <P>	Point class
 */
public interface InspectionSettings<C, K, P>
{
	boolean isComponent(Object object);
	ObjectInspector<C> getInspector();
	K getInspectionKey();
	ExpressionEvaluator getEvaluator();
	K getEvaluationKey();
	ComponentHierarchyModel<C, P> getComponentHierarchyModel();
	VisualSettings<C> getVisualSettings();
	Predicate<C> getResponsibilityPredicate();
}
