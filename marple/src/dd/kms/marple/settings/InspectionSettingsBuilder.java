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
public interface InspectionSettingsBuilder<C, V, K, P>
{
	InspectionSettingsBuilder<C, V, K, P> inspector(ObjectInspector<C, V> inspector);
	InspectionSettingsBuilder<C, V, K, P> inspectionKey(K inspectionKey);
	InspectionSettingsBuilder<C, V, K, P> evaluator(ExpressionEvaluator evaluator);
	InspectionSettingsBuilder<C, V, K, P> evaluationKey(K evaluationKey);
	InspectionSettingsBuilder<C, V, K, P> componentHierarchyModel(ComponentHierarchyModel<C, P> componentHierarchyModel);
	InspectionSettingsBuilder<C, V, K, P> visualSettings(VisualSettings<C, V> visualSettings);
	InspectionSettingsBuilder<C, V, K, P> responsibilityPredicate(Predicate<C> responsibilityPredicate);
	InspectionSettings<C, V, K, P> build();
}
