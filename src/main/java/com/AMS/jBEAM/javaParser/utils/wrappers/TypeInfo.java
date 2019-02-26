package com.AMS.jBEAM.javaParser.utils.wrappers;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;

public class TypeInfo
{
	public static final TypeInfo	NONE	= new TypeInfo(null);
	public static final TypeInfo	UNKNOWN	= new TypeInfo(null);

	public static TypeInfo of(Type type) {
		return type == null ? TypeInfo.NONE : new TypeInfo(TypeToken.of(type));
	}

	private final TypeToken<?> typeToken;

	private TypeInfo(@Nullable TypeToken<?> typeToken) {
		this.typeToken = typeToken;
	}

	public Class<?> getRawType() {
		return typeToken == null ? null : typeToken.getRawType();
	}

	public Type getType() {
		return typeToken == null ? null : typeToken.getType();
	}

	public TypeInfo getComponentType() {
		if (typeToken == null) {
			return TypeInfo.NONE;
		}
		TypeToken<?> componentType = typeToken.getComponentType();
		return componentType == null ? TypeInfo.NONE : new TypeInfo(componentType);
	}

	public boolean isPrimitive() {
		return typeToken == null ? false : typeToken.isPrimitive();
	}

	public boolean isSupertypeOf(TypeInfo that) {
		return typeToken != null && that.typeToken != null && this.typeToken.isSupertypeOf(that.typeToken);
	}

	public TypeInfo getSubtype(Class<?> subclass) {
		return typeToken == null ? TypeInfo.NONE : new TypeInfo(typeToken.getSubtype(subclass));
	}

	public TypeInfo resolveType(Type type) {
		return typeToken == null ? TypeInfo.NONE : new TypeInfo(typeToken.resolveType(type));
	}

	@Override
	public String toString() {
		if (typeToken == null) {
			return	this == UNKNOWN	? "unknown" :
					this == NONE	? "none"
									: null;
		}
		return typeToken.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TypeInfo typeInfo = (TypeInfo) o;
		return Objects.equals(typeToken, typeInfo.typeToken);
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeToken);
	}
}
