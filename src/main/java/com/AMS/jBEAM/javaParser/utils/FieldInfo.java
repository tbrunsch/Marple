package com.AMS.jBEAM.javaParser.utils;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class FieldInfo
{
	private final Field			field;
	private final TypeToken<?>	declaringType;

	public FieldInfo(Field field, TypeToken<?> declaringType) {
		this.field = field;
		this.declaringType = declaringType;
	}

	public String getName() {
		return field.getName();
	}

	public TypeToken<?> getType() {
		return declaringType.resolveType(field.getGenericType());
	}

	public boolean isFinal() {
		return Modifier.isFinal(field.getModifiers());
	}

	public TypeToken<?> getDeclaringType() {
		return declaringType;
	}

	public Object get(Object instance) throws IllegalAccessException {
		field.setAccessible(true);
		return field.get(instance);
	}

	public void set(Object instance, Object value) throws IllegalAccessException {
		field.set(instance, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FieldInfo fieldInfo = (FieldInfo) o;
		return Objects.equals(field, fieldInfo.field) &&
				Objects.equals(declaringType, fieldInfo.declaringType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(field, declaringType);
	}
}
