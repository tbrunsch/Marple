package com.AMS.jBEAM.javaParser.tokenizer;

public class JavaToken
{
	private final String	value;
	private final boolean   containsCaret;

	JavaToken(String value, boolean containsCaret) {
		this.value = value;
		this.containsCaret = containsCaret;
	}

	public String getValue() {
		return value;
	}

	public boolean isContainsCaret() {
		return containsCaret;
	}
}
