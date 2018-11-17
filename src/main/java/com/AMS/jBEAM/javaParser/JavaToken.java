package com.AMS.jBEAM.javaParser;

class JavaToken
{
	private final String	value;
	private final boolean   containsCaret;

	JavaToken(String value, boolean containsCaret) {
		this.value = value;
		this.containsCaret = containsCaret;
	}

	String getValue() {
		return value;
	}

	boolean isContainsCaret() {
		return containsCaret;
	}
}
