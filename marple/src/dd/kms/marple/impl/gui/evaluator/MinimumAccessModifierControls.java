package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.evaluator.ExpressionEvaluators;
import dd.kms.marple.impl.gui.common.AccessModifierInput;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

class MinimumAccessModifierControls
{
	private final AccessModifierInput	minimumFieldAccessModifierInput		= new AccessModifierInput();
	private final AccessModifierInput	minimumMethodAccessModifierInput	= new AccessModifierInput();

	private final InspectionContext		context;

	MinimumAccessModifierControls(InspectionContext context) {
		this.context = context;

		updateControls();

		minimumFieldAccessModifierInput.addChangeListener(e -> onMinimumFieldAccessModifierChanged());
		minimumMethodAccessModifierInput.addChangeListener(e -> onMinimumMethodAccessModifierChanged());
	}

	void updateControls() {
		minimumFieldAccessModifierInput.setAccessModifier(getMinimumFieldAccessModifier());
		minimumMethodAccessModifierInput.setAccessModifier(getMinimumMethodAccessModifier());
	}

	AccessModifierInput getMinimumFieldAccessModifierInput() {
		return minimumFieldAccessModifierInput;
	}

	AccessModifierInput getMinimumMethodAccessModifierInput() {
		return minimumMethodAccessModifierInput;
	}

	private AccessModifier getMinimumFieldAccessModifier() {
		return ExpressionEvaluators.getValue(ParserSettings::getMinimumFieldAccessModifier, context);
	}

	private AccessModifier getMinimumMethodAccessModifier() {
		return ExpressionEvaluators.getValue(ParserSettings::getMinimumMethodAccessModifier, context);
	}

	private void setMinimumFieldAccessModifier(AccessModifier minimumFieldAccessModifier) {
		ExpressionEvaluators.setValue(minimumFieldAccessModifier, ParserSettingsBuilder::minimumFieldAccessModifier, context);
	}

	private void setMinimumMethodAccessModifier(AccessModifier minimumMethodAccessModifier) {
		ExpressionEvaluators.setValue(minimumMethodAccessModifier, ParserSettingsBuilder::minimumMethodAccessModifier, context);
	}

	private void onMinimumFieldAccessModifierChanged() {
		setMinimumFieldAccessModifier(minimumFieldAccessModifierInput.getAccessModifier());
	}

	private void onMinimumMethodAccessModifierChanged() {
		setMinimumMethodAccessModifier(minimumMethodAccessModifierInput.getAccessModifier());
	}
}
