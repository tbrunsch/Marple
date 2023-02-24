package dd.kms.marple.impl.settings;

import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.inspector.ObjectInspector;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.SecuritySettings;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.components.ComponentHierarchyModel;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.keys.KeySettings;
import dd.kms.marple.api.settings.visual.VisualSettings;

import javax.annotation.Nullable;
import java.nio.file.Path;

class InspectionSettingsImpl implements InspectionSettings
{
	private final ObjectInspector			inspector;
	private final ExpressionEvaluator		evaluator;
	private final ComponentHierarchyModel	componentHierarchyModel;
	private final EvaluationSettings		evaluationSettings;
	private final VisualSettings			visualSettings;
	private final CustomActionSettings		customActionSettings;
	private final SecuritySettings			securitySettings;
	private final KeySettings				keySettings;
	@Nullable
	private final Path						preferencesFile;

	InspectionSettingsImpl(ComponentHierarchyModel componentHierarchyModel, EvaluationSettings evaluationSettings, VisualSettings visualSettings,
						   CustomActionSettings customActionSettings, SecuritySettings securitySettings, KeySettings keySettings, @Nullable Path preferencesFile) {
		this.evaluationSettings = evaluationSettings;
		this.preferencesFile = preferencesFile;
		this.inspector = ObjectInspector.create();
		this.evaluator = ExpressionEvaluator.create();
		this.componentHierarchyModel = componentHierarchyModel;
		this.visualSettings = visualSettings;
		this.customActionSettings = customActionSettings;
		this.securitySettings = securitySettings;
		this.keySettings = keySettings;
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
	public EvaluationSettings getEvaluationSettings() {
		return evaluationSettings;
	}

	@Override
	public VisualSettings getVisualSettings() {
		return visualSettings;
	}

	@Override
	public CustomActionSettings getCustomActionSettings() {
		return customActionSettings;
	}

	@Override
	public SecuritySettings getSecuritySettings() {
		return securitySettings;
	}

	@Override
	public KeySettings getKeySettings() {
		return keySettings;
	}

	@Override
	@Nullable
	public Path getPreferencesFile() {
		return preferencesFile;
	}
}
