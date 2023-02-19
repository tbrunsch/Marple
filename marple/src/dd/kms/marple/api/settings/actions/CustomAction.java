package dd.kms.marple.api.settings.actions;

import dd.kms.marple.api.settings.keys.KeyRepresentation;

import javax.annotation.Nullable;

public interface CustomAction
{
	static CustomAction create(String name, String actionExpression) {
		return create(name, actionExpression, Object.class);
	}

	static CustomAction create(String name, String actionExpression, Class<?> thisClass) {
		return create(name, actionExpression, thisClass, null);
	}

	static CustomAction create(String name, String actionExpression, Class<?> thisClass, @Nullable KeyRepresentation key) {
		return new dd.kms.marple.impl.settings.actions.CustomActionImpl(name, actionExpression, thisClass, key);
	}

	String getName();
	String getActionExpression();
	Class<?> getThisClass();
	@Nullable
	KeyRepresentation getKey();
}
