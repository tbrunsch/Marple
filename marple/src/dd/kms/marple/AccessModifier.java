package dd.kms.marple;

import java.lang.reflect.Modifier;

public enum AccessModifier
{
	PUBLIC			("public"),
	PROTECTED		("protected"),
	PACKAGE_PRIVATE	("package private"),
	PRIVATE			("private");

	public static AccessModifier getValue(int modifiers) {
		return	Modifier.isPublic(modifiers)	? AccessModifier.PUBLIC :
				Modifier.isProtected(modifiers)	? AccessModifier.PROTECTED :
				Modifier.isPrivate(modifiers)	? AccessModifier.PRIVATE
												: AccessModifier.PACKAGE_PRIVATE;
	}

	private final String name;

	AccessModifier(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
