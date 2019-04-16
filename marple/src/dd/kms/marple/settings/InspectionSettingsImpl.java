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
class InspectionSettingsImpl<C, V, K, P> implements InspectionSettings<C, V, K, P>
{
	private final Class<C>						componentClass;
	private final ObjectInspector<C, V>			inspector;
	private final K								inspectionKey;
	private final ExpressionEvaluator			evaluator;
	private final K								evaluationKey;
	private final ComponentHierarchyModel<C, P>	componentHierarchyModel;
	private final VisualSettings<C, V>			visualSettings;
	private final Predicate<C>					responsibilityPredicate;

	InspectionSettingsImpl(Class<C> componentClass, ObjectInspector<C, V> inspector, K inspectionKey, ExpressionEvaluator evaluator, K evaluationKey, ComponentHierarchyModel<C, P> componentHierarchyModel, VisualSettings<C, V> visualSettings, Predicate<C> responsibilityPredicate) {
		this.componentClass = componentClass;
		this.inspector = inspector;
		this.inspectionKey = inspectionKey;
		this.evaluator = evaluator;
		this.evaluationKey = evaluationKey;
		this.componentHierarchyModel = componentHierarchyModel;
		this.visualSettings = visualSettings;
		this.responsibilityPredicate = responsibilityPredicate;
	}

	@Override
	public boolean isComponent(Object object) {
		return componentClass.isInstance(object);
	}

	@Override
	public ObjectInspector<C, V> getInspector() {
		return inspector;
	}

	@Override
	public K getInspectionKey() {
		return inspectionKey;
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return evaluator;
	}

	@Override
	public K getEvaluationKey() {
		return evaluationKey;
	}

	@Override
	public ComponentHierarchyModel<C, P> getComponentHierarchyModel() {
		return componentHierarchyModel;
	}

	@Override
	public VisualSettings<C, V> getVisualSettings() {
		return visualSettings;
	}

	@Override
	public Predicate<C> getResponsibilityPredicate() {
		return responsibilityPredicate;
	}
}
