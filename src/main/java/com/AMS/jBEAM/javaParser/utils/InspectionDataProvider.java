package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.AccessLevel;
import com.AMS.jBEAM.javaParser.ParserSettings;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InspectionDataProvider
{
	private static final Predicate<Integer>	STATIC_FILTER = modifiers -> (modifiers & Modifier.STATIC) != 0;

	private final Predicate<Integer> accessLevelFilter;

	public InspectionDataProvider(ParserSettings parserSettings) {
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

	public List<FieldInfo> getFieldInfos(TypeToken<?> type, boolean staticOnly) {
		Predicate<Integer> modifierFilter = staticOnly ? accessLevelFilter.and(STATIC_FILTER) : accessLevelFilter;
		List<Field> fields = ReflectionUtils.getFields(type.getRawType(), true, modifierFilter);
		return fields.stream()
				.map(field -> new FieldInfo(field, type))
				.collect(Collectors.toList());
	}

	public List<ExecutableInfo> getMethodInfos(TypeToken<?> type, boolean staticOnly) {
		Predicate<Integer> modifierFilter = staticOnly ? accessLevelFilter.and(STATIC_FILTER) : accessLevelFilter;
		List<Method> methods = ReflectionUtils.getMethods(type.getRawType(), modifierFilter);
		List<ExecutableInfo> executableInfos = new ArrayList<>(methods.size());
		for (Method method : methods) {
			executableInfos.addAll(ExecutableInfo.getAvailableExecutableInfos(method, type));
		}
		return executableInfos;
	}

	public List<ExecutableInfo> getConstructorInfos(TypeToken<?> type) {
		List<Constructor<?>> constructors = ReflectionUtils.getConstructors(type.getRawType(), accessLevelFilter);
		List<ExecutableInfo> executableInfos = new ArrayList<>(constructors.size());
		for (Constructor<?> constructor : constructors) {
			executableInfos.addAll(ExecutableInfo.getAvailableExecutableInfos(constructor, type));
		}
		return executableInfos;
	}
}
