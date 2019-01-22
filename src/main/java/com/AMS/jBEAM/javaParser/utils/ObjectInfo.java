package com.AMS.jBEAM.javaParser.utils;

import com.google.common.reflect.TypeToken;

public class ObjectInfo
{
	private final Object			object;
	private final TypeToken<?>		declaredType;
	private final ValueSetterIF		valueSetter;

	public ObjectInfo(Object object) {
		this(object, TypeToken.of(Object.class));
	}

	public ObjectInfo(Object object, TypeToken<?> declaredType) {
		this(object, declaredType, null);
	}

	public ObjectInfo(Object object, TypeToken<?> declaredType, ValueSetterIF valueSetter) {
		this.object = object;
		this.declaredType = declaredType;
		this.valueSetter = valueSetter;
	}

	public Object getObject() {
		return object;
	}

	public TypeToken<?> getDeclaredType() {
		return declaredType;
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
