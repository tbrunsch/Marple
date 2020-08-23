package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.marple.gui.common.AccessModifierInput;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

class MinimumAccessModifierControls
{
	private final AccessModifierInput	minimumAccessModifierInput	= new AccessModifierInput();

	private final InspectionContext		inspectionContext;

	MinimumAccessModifierControls(InspectionContext inspectionContext) {
		this.inspectionContext = inspectionContext;

		updateControls();

		minimumAccessModifierInput.addChangeListener(e -> onMinimumAccessModifierChanged());
	}

	void updateControls() {
		minimumAccessModifierInput.setAccessModifier(getMinimumAccessModifier());
	}

	AccessModifierInput getInput() {
		return minimumAccessModifierInput;
	}

	private AccessModifier getMinimumAccessModifier() {
		return ExpressionEvaluators.getValue(ParserSettings::getMinimumAccessModifier, inspectionContext);
	}

	private void setMinimumAccessModifier(AccessModifier minimumAccessModifier) {
		ExpressionEvaluators.setValue(minimumAccessModifier, ParserSettingsBuilder::minimumAccessModifier, inspectionContext);
	}

	private void onMinimumAccessModifierChanged() {
		setMinimumAccessModifier(minimumAccessModifierInput.getAccessModifier());
	}
}
