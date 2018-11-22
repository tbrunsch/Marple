package com.AMS.jBEAM.javaParser;

import java.util.Objects;

class JavaClassInfo
{
	private final String fullyQualifiedClassName;

	/**
	 * Inner classes must be separated by "$"
	 */
	JavaClassInfo(String fullyQualifiedClassName) {
		this.fullyQualifiedClassName = fullyQualifiedClassName;
	}

	String getName() {
		return fullyQualifiedClassName;
	}

	/**
	 * Simple name of inner classes might start with digits. This method returns the
	 * simple name without leading digits.
	 */
	String getSimpleNameWithoutLeadingDigits() {
		int lastSeparatorPos = Math.max(fullyQualifiedClassName.lastIndexOf('.'), fullyQualifiedClassName.lastIndexOf('$'));
		return fullyQualifiedClassName.substring(lastSeparatorPos + 1).replaceFirst("^\\d*(.*)$", "$1");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JavaClassInfo that = (JavaClassInfo) o;
		return Objects.equals(fullyQualifiedClassName, that.fullyQualifiedClassName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fullyQualifiedClassName);
	}
}
