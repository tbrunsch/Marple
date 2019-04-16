package dd.kms.marple.settings;

import dd.kms.marple.ExpressionEvaluator;
import dd.kms.marple.ObjectInspector;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.gui.VisualSettings;

import java.util.function.Predicate;

/**
 *
 * @param <C>	GUI component class
 * @param <V>	View class (GUI component class plus name)
 * @param <K>	KeyStroke class
 * @param <P>	Point class
 */
public interface InspectionSettings<C, V, K, P>
{
	boolean isComponent(Object object);
	ObjectInspector<C, V> getInspector();
	K getInspectionKey();
	ExpressionEvaluator getEvaluator();
	K getEvaluationKey();
	ComponentHierarchyModel<C, P> getComponentHierarchyModel();
	VisualSettings<C, V> getVisualSettings();
	Predicate<C> getResponsibilityPredicate();
}
