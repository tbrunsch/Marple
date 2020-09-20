package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.evaluator.ExpressionEvaluators;
import dd.kms.marple.impl.gui.common.AccessModifierInput;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

class MinimumAccessModifierControls
{
	private final AccessModifierInput	minimumAccessModifierInput	= new AccessModifierInput();

	private final InspectionContext		context;

	MinimumAccessModifierControls(InspectionContext context) {
		this.context = context;

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
		return ExpressionEvaluators.getValue(ParserSettings::getMinimumAccessModifier, context);
	}

	private void setMinimumAccessModifier(AccessModifier minimumAccessModifier) {
		ExpressionEvaluators.setValue(minimumAccessModifier, ParserSettingsBuilder::minimumAccessModifier, context);
	}

	private void onMinimumAccessModifierChanged() {
		setMinimumAccessModifier(minimumAccessModifierInput.getAccessModifier());
	}
}
