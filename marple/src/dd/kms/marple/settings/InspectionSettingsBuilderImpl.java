package dd.kms.marple.settings;

import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.gui.VisualSettingsUtils;
import dd.kms.marple.inspector.ObjectInspector;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.gui.VisualSettings;

import java.awt.*;
import java.util.Optional;
import java.util.function.Predicate;

class InspectionSettingsBuilderImpl implements InspectionSettingsBuilder
{
	private ObjectInspector				inspector;
	private ExpressionEvaluator			evaluator;
	private ComponentHierarchyModel		componentHierarchyModel	= ComponentHierarchyModels.createBuilder().build();
	private VisualSettings				visualSettings			= VisualSettingsUtils.createBuilder().build();
	private Predicate<Component>		responsibilityPredicate	= component -> true;
	private Optional<SecuritySettings> 	securitySettings		= Optional.empty();

	private KeyRepresentation			inspectionKey;
	private KeyRepresentation			evaluationKey;
	private KeyRepresentation			codeCompletionKey;
	private KeyRepresentation			searchKey;
	private KeyRepresentation			showMethodArgumentsKey;

	@Override
	public InspectionSettingsBuilder inspector(ObjectInspector inspector) {
		this.inspector = inspector;
		return this;
	}

	@Override
	public InspectionSettingsBuilder evaluator(ExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
		return this;
	}

	@Override
	public InspectionSettingsBuilder componentHierarchyModel(ComponentHierarchyModel componentHierarchyModel) {
		this.componentHierarchyModel = componentHierarchyModel;
		return this;
	}

	@Override
	public InspectionSettingsBuilder visualSettings(VisualSettings visualSettings) {
		this.visualSettings = visualSettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder responsibilityPredicate(Predicate<Component> responsibilityPredicate) {
		this.responsibilityPredicate = responsibilityPredicate;
		return this;
	}

	@Override
	public InspectionSettingsBuilder securitySettings(Optional<SecuritySettings> securitySettings) {
		this.securitySettings = securitySettings;
		return this;
	}

	@Override
	public InspectionSettingsBuilder inspectionKey(KeyRepresentation inspectionKey) {
		this.inspectionKey = inspectionKey;
		return this;
	}

	@Override
	public InspectionSettingsBuilder evaluationKey(KeyRepresentation evaluationKey) {
		this.evaluationKey = evaluationKey;
		return this;
	}

	@Override
	public InspectionSettingsBuilder searchKey(KeyRepresentation searchKey) {
		this.searchKey = searchKey;
		return this;
	}

	@Override
	public InspectionSettingsBuilder codeCompletionKey(KeyRepresentation codeCompletionKey) {
		this.codeCompletionKey = codeCompletionKey;
		return this;
	}

	@Override
	public InspectionSettingsBuilder showMethodArgumentsKey(KeyRepresentation showMethodArgumentsKey) {
		this.showMethodArgumentsKey = showMethodArgumentsKey;
		return this;
	}

	@Override
	public InspectionSettings build() {
		return new InspectionSettingsImpl(inspector, evaluator, componentHierarchyModel, visualSettings, responsibilityPredicate, securitySettings, inspectionKey, evaluationKey, searchKey, codeCompletionKey, showMethodArgumentsKey);
	}
}
