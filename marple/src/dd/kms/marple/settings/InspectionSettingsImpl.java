package dd.kms.marple.settings;

import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.marple.gui.VisualSettings;
import dd.kms.marple.inspector.ObjectInspector;
import dd.kms.marple.inspector.ObjectInspectors;

import java.awt.*;
import java.util.function.Predicate;

class InspectionSettingsImpl implements InspectionSettings
{
	private final ObjectInspector			inspector;
	private final ExpressionEvaluator		evaluator;
	private final ComponentHierarchyModel	componentHierarchyModel;
	private final VisualSettings			visualSettings;
	private final Predicate<Component>		responsibilityPredicate;
	private final SecuritySettings			securitySettings;
	private final KeyRepresentation			inspectionKey;
	private final KeyRepresentation			evaluationKey;
	private final KeyRepresentation			searchKey;
	private final KeyRepresentation			codeCompletionKey;
	private final KeyRepresentation			showMethodArgumentsKey;

	InspectionSettingsImpl(ComponentHierarchyModel componentHierarchyModel, VisualSettings visualSettings, Predicate<Component> responsibilityPredicate, SecuritySettings securitySettings, KeyRepresentation inspectionKey, KeyRepresentation evaluationKey, KeyRepresentation searchKey, KeyRepresentation codeCompletionKey, KeyRepresentation showMethodArgumentsKey) {
		this.inspector = ObjectInspectors.create();
		this.evaluator = ExpressionEvaluators.create();
		this.componentHierarchyModel = componentHierarchyModel;
		this.visualSettings = visualSettings;
		this.responsibilityPredicate = responsibilityPredicate;
		this.securitySettings = securitySettings;
		this.inspectionKey = inspectionKey;
		this.evaluationKey = evaluationKey;
		this.searchKey = searchKey;
		this.codeCompletionKey = codeCompletionKey;
		this.showMethodArgumentsKey = showMethodArgumentsKey;
	}

	@Override
	public ObjectInspector getInspector() {
		return inspector;
	}

	@Override
	public ExpressionEvaluator getEvaluator() {
		return evaluator;
	}

	@Override
	public ComponentHierarchyModel getComponentHierarchyModel() {
		return componentHierarchyModel;
	}

	@Override
	public VisualSettings getVisualSettings() {
		return visualSettings;
	}

	@Override
	public Predicate<Component> getResponsibilityPredicate() {
		return responsibilityPredicate;
	}

	@Override
	public SecuritySettings getSecuritySettings() {
		return securitySettings;
	}

	@Override
	public KeyRepresentation getInspectionKey() {
		return inspectionKey;
	}

	@Override
	public KeyRepresentation getEvaluationKey() {
		return evaluationKey;
	}

	@Override
	public KeyRepresentation getSearchKey() {
		return searchKey;
	}

	@Override
	public KeyRepresentation getCodeCompletionKey() {
		return codeCompletionKey;
	}

	@Override
	public KeyRepresentation getShowMethodArgumentsKey() {
		return showMethodArgumentsKey;
	}
}
