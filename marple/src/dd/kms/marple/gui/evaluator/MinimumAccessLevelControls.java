package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.evaluator.ExpressionEvaluators;
import dd.kms.marple.gui.common.AccessModifierInput;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsBuilder;

class MinimumAccessLevelControls
{
	private final AccessModifierInput minimumAccessLevelInput	= new AccessModifierInput();

	private final InspectionContext	inspectionContext;

	MinimumAccessLevelControls(InspectionContext inspectionContext) {
		this.inspectionContext = inspectionContext;

		updateControls();

		minimumAccessLevelInput.addChangeListener(e -> onMinimumAccessLevelChanged());
	}

	void updateControls() {
		minimumAccessLevelInput.setAccessModifier(getMinimumAccessModifier());
	}

	AccessModifierInput getInput() {
		return minimumAccessLevelInput;
	}

	private AccessModifier getMinimumAccessModifier() {
		return ExpressionEvaluators.getValue(ParserSettings::getMinimumAccessLevel, inspectionContext);
	}

	private void setMinimumAccessLevel(AccessModifier minimumAccessLevel) {
		ExpressionEvaluators.setValue(minimumAccessLevel, ParserSettingsBuilder::minimumAccessLevel, inspectionContext);
	}

	private void onMinimumAccessLevelChanged() {
		setMinimumAccessLevel(minimumAccessLevelInput.getAccessModifier());
	}
}
