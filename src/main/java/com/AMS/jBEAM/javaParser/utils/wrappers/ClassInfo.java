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

	private static Class<?> getClassUnchecked(String qualifiedClassName) {
		try {
			return Class.forName(qualifiedClassName);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private static String normalizeClassName(String qualifiedClassName) throws ClassNotFoundException {
		if (getClassUnchecked(qualifiedClassName) != null) {
			return qualifiedClassName;
		}

		int lastSeparatorPos = -1;
		while (true) {
			int nextDotPos = qualifiedClassName.indexOf('.', lastSeparatorPos + 1);
			int nextDollarPos = qualifiedClassName.indexOf('$', lastSeparatorPos + 1);
			int nextSeparatorPos =	nextDotPos < 0		? nextDollarPos :
									nextDollarPos < 0	? nextDotPos
														: Math.min(nextDotPos, nextDollarPos);
			if (nextSeparatorPos < 0) {
				throw new ClassNotFoundException("Unknown class '" + qualifiedClassName + "'");
			}
			Class<?> clazz = getClassUnchecked(qualifiedClassName.substring(0, nextSeparatorPos));
			if (clazz != null) {
				String topLevelClassName = qualifiedClassName.substring(0, nextSeparatorPos);
				String remainderClassName = qualifiedClassName.substring(nextSeparatorPos).replace('.', '$');
				String normalizedClassName = topLevelClassName + remainderClassName;
				if (Class.forName(normalizedClassName) == null) {
					throw new ClassNotFoundException("Unknown class '" + qualifiedClassName + "'");
				}
				return normalizedClassName;
			}
			lastSeparatorPos = nextSeparatorPos;
		}
	}

	// inner classes must be separated by "$" from their declaring classes
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
