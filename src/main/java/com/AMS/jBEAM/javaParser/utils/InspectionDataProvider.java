package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.AccessLevel;
import com.AMS.jBEAM.javaParser.JavaParserSettings;
import com.AMS.jBEAM.common.ReflectionUtils;
import com.sun.org.apache.xpath.internal.operations.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;

public class InspectionDataProvider
{
	private static final Predicate<Integer>	STATIC_FILTER = modifiers -> (modifiers & Modifier.STATIC) != 0;

	private final Predicate<Integer> accessLevelFilter;

	public InspectionDataProvider(JavaParserSettings parserSettings) {
		accessLevelFilter = createAccessLevelFilter(parserSettings.getMinimumAccessLevel());
	}

	private static Predicate<Integer> createAccessLevelFilter(final AccessLevel minimumAccessLevel) {
		switch (minimumAccessLevel) {
			case PUBLIC:
				return modifiers -> (modifiers & Modifier.PUBLIC) != 0;
			case PROTECTED:
				return modifiers -> (modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0;
			case PACKAGE_PRIVATE:
				return modifiers -> (modifiers & Modifier.PRIVATE) == 0;
			case PRIVATE:
				return modifiers -> true;
			default:
				throw new IllegalArgumentException("Unsupported access level " + minimumAccessLevel);
		}
	}

	public List<Field> getFields(Class<?> clazz, boolean staticOnly) {
		Predicate<Integer> modifierFilter = staticOnly ? accessLevelFilter.and(STATIC_FILTER) : accessLevelFilter;
		return ReflectionUtils.getFields(clazz, true, modifierFilter);
	}

	public List<Method> getMethods(Class<?> clazz, boolean staticOnly) {
		Predicate<Integer> modifierFilter = staticOnly ? accessLevelFilter.and(STATIC_FILTER) : accessLevelFilter;
		return ReflectionUtils.getMethods(clazz, modifierFilter);
	}
}
