package com.AMS.jBEAM.javaParser;

public class Variable
{
	private final String	name;
	private final Object	value;
	private final boolean	useHardReferenceInPool;

	public Variable(String name, Object value, boolean useHardReferenceInPool) {
		this.name = name;
		this.value = value;
		this.useHardReferenceInPool = useHardReferenceInPool;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public boolean isUseHardReferenceInPool() {
		return useHardReferenceInPool;
	}
}
