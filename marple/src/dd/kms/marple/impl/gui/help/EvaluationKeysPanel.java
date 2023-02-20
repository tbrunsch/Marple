package dd.kms.marple.impl.gui.help;

import dd.kms.marple.api.settings.keys.KeyFunction;
import dd.kms.marple.api.settings.keys.KeySettings;

class EvaluationKeysPanel extends AbstractKeysPanel
{
	private static final String	CODE_COMPLETION_DESCRIPTION			= "Suggest code completions";
	private static final String	SHOW_METHOD_ARGUMENTS_DESCRIPTION	= "Show method argument types";

	EvaluationKeysPanel(KeySettings keySettings) {
		super("Keys for expression evaluation");

		addKeyDescription(CODE_COMPLETION_DESCRIPTION,			keySettings.getKey(KeyFunction.CODE_COMPLETION));
		addKeyDescription(SHOW_METHOD_ARGUMENTS_DESCRIPTION,	keySettings.getKey(KeyFunction.SHOW_METHOD_ARGUMENTS));
	}
}
