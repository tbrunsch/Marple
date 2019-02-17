package com.AMS.jBEAM.javaParser.utils.wrappers;

import java.util.Objects;

public class ClassInfo
{
	private final String fullyQualifiedClassName;

	/**
	 * Inner classes must be separated by "$"
	 */
	public ClassInfo(String fullyQualifiedClassName) {
		this.fullyQualifiedClassName = fullyQualifiedClassName;
	}

	public String getName() {
		return fullyQualifiedClassName;
	}

	/**
	 * Simple name of inner classes might start with digits. This method returns the
	 * simple name without leading digits.
	 */
	public String getSimpleNameWithoutLeadingDigits() {
		int lastSeparatorPos = Math.max(fullyQualifiedClassName.lastIndexOf('.'), fullyQualifiedClassName.lastIndexOf('$'));
		return fullyQualifiedClassName.substring(lastSeparatorPos + 1).replaceFirst("^\\d*(.*)$", "$1");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassInfo that = (ClassInfo) o;
		return Objects.equals(fullyQualifiedClassName, that.fullyQualifiedClassName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fullyQualifiedClassName);
	}

	@Override
	public String toString() {
		return fullyQualifiedClassName;
	}
}
