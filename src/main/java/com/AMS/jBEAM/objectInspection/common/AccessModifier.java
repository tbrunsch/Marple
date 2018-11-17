package com.AMS.jBEAM.objectInspection.common;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public enum AccessModifier
{
	PUBLIC			("public"),
	PROTECTED		("protected"),
	PACKAGE_PRIVATE	("package private"),
	PRIVATE			("private");

	private static final Map<Integer, AccessModifier> MODIFIER_TO_ACCESS_MODIFIER = new HashMap<>();

	static {
		MODIFIER_TO_ACCESS_MODIFIER.put(Modifier.PUBLIC,	PUBLIC);
		MODIFIER_TO_ACCESS_MODIFIER.put(Modifier.PROTECTED,	PROTECTED);
		MODIFIER_TO_ACCESS_MODIFIER.put(Modifier.PRIVATE,	PRIVATE);
	}

	public static AccessModifier getValue(int modifiers) {
		for (int modifier : MODIFIER_TO_ACCESS_MODIFIER.keySet()) {
			if ((modifiers & modifier) != 0) {
				return MODIFIER_TO_ACCESS_MODIFIER.get(modifier);
			}
		}
		return AccessModifier.PACKAGE_PRIVATE;
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
