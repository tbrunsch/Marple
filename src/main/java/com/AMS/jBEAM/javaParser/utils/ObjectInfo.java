package com.AMS.jBEAM.javaParser.utils;

public class ObjectInfo
{
	private final Object	object;
	private final Class<?>	declaredClass;

	public ObjectInfo(Object object) {
		this(object, object == null ? null : object.getClass());
	}

	public ObjectInfo(Object object, Class<?> declaredClass) {
		this.object = object;
		this.declaredClass = declaredClass;
	}

	public Object getObject() {
		return object;
	}

	public Class<?> getDeclaredClass() {
		return declaredClass;
	}
}
