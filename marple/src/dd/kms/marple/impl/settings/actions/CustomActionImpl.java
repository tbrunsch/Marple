package dd.kms.marple.impl.settings.actions;

import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.keys.KeyRepresentation;

import javax.annotation.Nullable;

public class CustomActionImpl implements CustomAction
{
	private final String			name;
	private final String			actionExpression;
	private final Class<?>			thisClass;
	@Nullable
	private final KeyRepresentation	key;

	public CustomActionImpl(String name, String actionExpression, Class<?> thisClass, @Nullable KeyRepresentation key) {
		this.name = name;
		this.actionExpression = actionExpression;
		this.thisClass = thisClass;
		this.key = key;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getActionExpression() {
		return actionExpression;
	}

	@Override
	public Class<?> getThisClass() {
		return thisClass;
	}

	@Override
	@Nullable
	public KeyRepresentation getKey() {
		return key;
	}
}
