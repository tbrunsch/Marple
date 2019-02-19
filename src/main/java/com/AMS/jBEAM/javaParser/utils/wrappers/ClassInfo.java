package com.AMS.jBEAM.javaParser.utils.wrappers;

import java.util.Objects;

public class ClassInfo
{
	public static ClassInfo forName(String qualifiedClassName) throws ClassNotFoundException {
		return new ClassInfo(normalizeClassName(qualifiedClassName));
	}

	public static ClassInfo forNameUnchecked(String qualifiedClassName) {
		return new ClassInfo(qualifiedClassName);
	}

	private static String normalizeClassName(String qualifiedClassName) throws ClassNotFoundException {
		if (Class.forName(qualifiedClassName) != null) {
			return qualifiedClassName;
		}

		int lastSeparatorPos = -1;
		while (true) {
			int nextSeparatorPos = Math.min(qualifiedClassName.indexOf('.', lastSeparatorPos + 1), qualifiedClassName.indexOf('$', lastSeparatorPos + 1));
			if (nextSeparatorPos < 0) {
				throw new ClassNotFoundException("Unknown class '" + qualifiedClassName + "'");
			}
			Class<?> clazz = Class.forName(qualifiedClassName.substring(0, nextSeparatorPos));
			if (clazz != null) {
				String topLevelClassName = qualifiedClassName.substring(0, nextSeparatorPos);
				String innerClassName = qualifiedClassName.substring(nextSeparatorPos + 1);
				String normalizedClassName = topLevelClassName + "$" + innerClassName;
				if (Class.forName(normalizedClassName) == null) {
					throw new ClassNotFoundException("Unknown class '" + qualifiedClassName + "'");
				}
				return normalizedClassName;
			}
			lastSeparatorPos = nextSeparatorPos;
		}
	}

	// inner classes must be separated by "$"
	private final String qualifiedClassName;

	private ClassInfo(String qualifiedClassName) {
		this.qualifiedClassName = qualifiedClassName;
	}

	public String getQualifiedName() {
		return qualifiedClassName;
	}

	public String getUnqualifiedName() {
		int lastSeparatorPos = Math.max(qualifiedClassName.lastIndexOf('.'), qualifiedClassName.lastIndexOf('$'));
		return qualifiedClassName.substring(lastSeparatorPos + 1);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassInfo that = (ClassInfo) o;
		return Objects.equals(qualifiedClassName, that.qualifiedClassName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(qualifiedClassName);
	}

	@Override
	public String toString() {
		return qualifiedClassName;
	}
}
