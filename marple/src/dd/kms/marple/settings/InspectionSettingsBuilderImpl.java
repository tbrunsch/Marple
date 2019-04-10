package dd.kms.marple.settings;

import dd.kms.marple.ExpressionEvaluator;
import dd.kms.marple.ObjectInspector;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.components.ComponentHierarchyModels;

import java.util.function.Predicate;

/**
 *
 * @param <C>	GUI component class
 * @param <K>	KeyStroke class
 * @param <P>	Point class
 */
class InspectionSettingsBuilderImpl<C, K, P> implements InspectionSettingsBuilder<C, K, P>
{
	private final Class<C>					componentClass;
	private ObjectInspector<C>				inspector;
	private K								inspectionKey;
	private ExpressionEvaluator				evaluator;
	private K								evaluationKey;
	private ComponentHierarchyModel<C, P>	componentHierarchyModel	= ComponentHierarchyModels.<C, P>createBuilder(component -> null).build();
	private Predicate<C>					responsibilityPredicate	= component -> true;

	InspectionSettingsBuilderImpl(Class<C> componentClass) {
		this.componentClass = componentClass;
	}

	@Override
	public InspectionSettingsBuilder<C, K, P> inspector(ObjectInspector<C> inspector) {
		this.inspector = inspector;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, K, P> inspectionKey(K inspectionKey) {
		this.inspectionKey = inspectionKey;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, K, P> evaluator(ExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, K, P> evaluationKey(K evaluationKey) {
		this.evaluationKey = evaluationKey;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, K, P> componentHierarchyModel(ComponentHierarchyModel<C, P> componentHierarchyModel) {
		this.componentHierarchyModel = componentHierarchyModel;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, K, P> responsibilityPredicate(Predicate<C> responsibilityPredicate) {
		this.responsibilityPredicate = responsibilityPredicate;
		return this;
	}

	@Override
	public InspectionSettings<C, K, P> build() {
		return new InspectionSettingsImpl<>(componentClass, inspector, inspectionKey, evaluator, evaluationKey, componentHierarchyModel, responsibilityPredicate);
	}
}
