package com.AMS.jBEAM.javaParser.utils;

public class ObjectInfo
{
	private final Object			object;
	private final Class<?>			declaredClass;
	private final ValueSetterIF		valueSetter;

	public ObjectInfo(Object object) {
		this(object, object == null ? null : object.getClass());
	}

	public ObjectInfo(Object object, Class<?> declaredClass) {
		this(object, declaredClass, null);
	}

	public ObjectInfo(Object object, Class<?> declaredClass, ValueSetterIF valueSetter) {
		this.object = object;
		this.declaredClass = declaredClass;
		this.valueSetter = valueSetter;
	}

	public Object getObject() {
		return object;
	}

	public Class<?> getDeclaredClass() {
		return declaredClass;
	}

	public ValueSetterIF getValueSetter() {
		return valueSetter;
	}

	@FunctionalInterface
	public interface ValueSetterIF
	{
		void setObject(Object object) throws IllegalArgumentException;
	}
}
