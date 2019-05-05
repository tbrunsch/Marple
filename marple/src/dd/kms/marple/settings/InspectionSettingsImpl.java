package dd.kms.marple.settings;

import dd.kms.marple.evaluator.ExpressionEvaluator;
import dd.kms.marple.inspector.ObjectInspector;
import dd.kms.marple.components.ComponentHierarchyModel;
import dd.kms.marple.gui.VisualSettings;

import java.awt.*;
import java.util.Optional;
import java.util.function.Predicate;

class InspectionSettingsImpl implements InspectionSettings
{
	private final ObjectInspector				inspector;
	private final ExpressionEvaluator			evaluator;
	private final ComponentHierarchyModel		componentHierarchyModel;
	private final VisualSettings				visualSettings;
	private final Predicate<Component>			responsibilityPredicate;
	private final Optional<SecuritySettings>	securitySettings;
	private final KeyRepresentation				inspectionKey;
	private final KeyRepresentation				evaluationKey;
	private final KeyRepresentation				codeCompletionKey;
	private final KeyRepresentation				showMethodArgumentsKey;

	InspectionSettingsImpl(ObjectInspector inspector, ExpressionEvaluator evaluator, ComponentHierarchyModel componentHierarchyModel, VisualSettings visualSettings, Predicate<Component> responsibilityPredicate, Optional<SecuritySettings> securitySettings, KeyRepresentation inspectionKey, KeyRepresentation evaluationKey, KeyRepresentation codeCompletionKey, KeyRepresentation showMethodArgumentsKey) {
		this.inspector = inspector;
		this.evaluator = evaluator;
		this.componentHierarchyModel = componentHierarchyModel;
		this.visualSettings = visualSettings;
		this.responsibilityPredicate = responsibilityPredicate;
		this.securitySettings = securitySettings;
		this.inspectionKey = inspectionKey;
		this.evaluationKey = evaluationKey;
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
	public Optional<SecuritySettings> getSecuritySettings() {
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
	public KeyRepresentation getCodeCompletionKey() {
		return codeCompletionKey;
	}

	@Override
	public KeyRepresentation getShowMethodArgumentsKey() {
		return showMethodArgumentsKey;
	}
}
