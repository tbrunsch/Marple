package dd.kms.marple.settings;

import dd.kms.marple.ExpressionEvaluator;
import dd.kms.marple.ObjectInspector;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.gui.VisualSettings;
import dd.kms.marple.gui.VisualSettingsBuilders;

import java.util.function.Predicate;

/**
 *
 * @param <C>	GUI component class
 * @param <V>	View class (GUI component class plus name)
 * @param <K>	KeyStroke class
 * @param <P>	Point class
 */
class InspectionSettingsBuilderImpl<C, V, K, P> implements InspectionSettingsBuilder<C, V, K, P>
{
	private final Class<C>					componentClass;
	private ObjectInspector<C, V>			inspector;
	private K								inspectionKey;
	private ExpressionEvaluator				evaluator;
	private K								evaluationKey;
	private ComponentHierarchyModel<C, P>	componentHierarchyModel	= ComponentHierarchyModels.<C, P>createBuilder(component -> null).build();
	private VisualSettings<C, V>			visualSettings			= VisualSettingsBuilders.<C,V>createBuilder().build();
	private Predicate<C>					responsibilityPredicate	= component -> true;

	InspectionSettingsBuilderImpl(Class<C> componentClass) {
		this.componentClass = componentClass;
	}

	@Override
	public InspectionSettingsBuilder<C, V, K, P> inspector(ObjectInspector<C, V> inspector) {
		this.inspector = inspector;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, V, K, P> inspectionKey(K inspectionKey) {
		this.inspectionKey = inspectionKey;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, V, K, P> evaluator(ExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, V, K, P> evaluationKey(K evaluationKey) {
		this.evaluationKey = evaluationKey;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, V, K, P> componentHierarchyModel(ComponentHierarchyModel<C, P> componentHierarchyModel) {
		this.componentHierarchyModel = componentHierarchyModel;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, V, K, P> visualSettings(VisualSettings<C, V> visualSettings) {
		this.visualSettings = visualSettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder<C, V, K, P> responsibilityPredicate(Predicate<C> responsibilityPredicate) {
		this.responsibilityPredicate = responsibilityPredicate;
		return this;
	}

	@Override
	public InspectionSettings<C, V, K, P> build() {
		return new InspectionSettingsImpl<>(componentClass, inspector, inspectionKey, evaluator, evaluationKey, componentHierarchyModel, visualSettings, responsibilityPredicate);
	}
}
