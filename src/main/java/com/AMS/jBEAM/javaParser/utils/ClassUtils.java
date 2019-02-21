package com.AMS.jBEAM.javaParser.utils;

public class ClassUtils
{
	public static Class<?> getClassUnchecked(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			return null;
		}
	}

	public static int lastIndexOfPathSeparator(String path) {
		return Math.max(path.lastIndexOf('.'), path.lastIndexOf('$'));
	}

	public static String getParentPath(String path) {
		int lastSeparatorIndex = lastIndexOfPathSeparator(path);
		return lastSeparatorIndex < 0 ? null : path.substring(0, lastSeparatorIndex);
	}

	public static String getLeafOfPath(String path) {
		int lastSeparatorIndex = lastIndexOfPathSeparator(path);
		return path.substring(lastSeparatorIndex + 1);
	}

	/**
	 * Replaces dots ('.') by dollar signs ('$') before each inner class of the given
	 * fully qualified class name.
	 *
	 * @throws ClassNotFoundException
	 */
	public static String normalizeClassName(String qualifiedClassName) throws ClassNotFoundException {
		if (ClassUtils.getClassUnchecked(qualifiedClassName) != null) {
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
			Class<?> clazz = ClassUtils.getClassUnchecked(qualifiedClassName.substring(0, nextSeparatorPos));
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

	/**
	 * Replaces all dollar signs ('$') in a class name by dots ('.').
	 */
	public static String getRegularClassName(String qualifiedClassName) {
		return qualifiedClassName.replace('$', '.');
	}
}
