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
class InspectionSettingsImpl<C, K, P> implements InspectionSettings<C, K, P>
{
	private final Class<C>						componentClass;
	private final ObjectInspector<C>			inspector;
	private final K								inspectionKey;
	private final ExpressionEvaluator			evaluator;
	private final K								evaluationKey;
	private final ComponentHierarchyModel<C, P>	componentHierarchyModel;
	private final VisualSettings<C>				visualSettings;
	private final Predicate<C>					responsibilityPredicate;

	InspectionSettingsImpl(Class<C> componentClass, ObjectInspector<C> inspector, K inspectionKey, ExpressionEvaluator evaluator, K evaluationKey, ComponentHierarchyModel<C, P> componentHierarchyModel, VisualSettings<C> visualSettings, Predicate<C> responsibilityPredicate) {
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
	public ObjectInspector<C> getInspector() {
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
	public VisualSettings<C> getVisualSettings() {
		return visualSettings;
	}

	@Override
	public Predicate<C> getResponsibilityPredicate() {
		return responsibilityPredicate;
	}
}
