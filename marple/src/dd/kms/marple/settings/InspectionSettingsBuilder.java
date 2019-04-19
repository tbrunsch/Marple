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
public interface InspectionSettingsBuilder<C, K, P>
{
	InspectionSettingsBuilder<C, K, P> inspector(ObjectInspector<C> inspector);
	InspectionSettingsBuilder<C, K, P> inspectionKey(K inspectionKey);
	InspectionSettingsBuilder<C, K, P> evaluator(ExpressionEvaluator evaluator);
	InspectionSettingsBuilder<C, K, P> evaluationKey(K evaluationKey);
	InspectionSettingsBuilder<C, K, P> componentHierarchyModel(ComponentHierarchyModel<C, P> componentHierarchyModel);
	InspectionSettingsBuilder<C, K, P> visualSettings(VisualSettings<C> visualSettings);
	InspectionSettingsBuilder<C, K, P> responsibilityPredicate(Predicate<C> responsibilityPredicate);
	InspectionSettings<C, K, P> build();
}
