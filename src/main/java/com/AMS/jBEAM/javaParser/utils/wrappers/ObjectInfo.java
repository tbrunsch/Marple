package com.AMS.jBEAM.javaParser.utils.wrappers;

import com.google.common.reflect.TypeToken;

public class ObjectInfo
{
	public static final	Object		INDETERMINATE	= new Object();

	private final Object			object;
	private final TypeToken<?>		declaredType;
	private final ValueSetterIF		valueSetter;

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(object == null ? "NULL" : object.toString());
		if (declaredType != null) {
			builder.append(" (").append(declaredType.toString()).append(")");
		}
		return builder.toString();
	}

	@FunctionalInterface
	public interface ValueSetterIF
	{
		void setObject(Object object) throws IllegalArgumentException;
	}
}
