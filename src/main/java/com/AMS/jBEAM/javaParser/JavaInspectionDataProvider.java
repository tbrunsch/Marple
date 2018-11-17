package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

class JavaInspectionDataProvider
{
	List<Field> getFields(Class<?> clazz, boolean staticFieldsOnly) {
		if (staticFieldsOnly) {
			throw new IllegalArgumentException("Flag 'staticFieldsOnly' is currently not supported");
		}
		// TODO: Consider settings
		return ReflectionUtils.getFields(clazz);
	}

	List<Method> getMethods(Class<?> clazz, boolean staticMethodsOnly) {
		if (staticMethodsOnly) {
			throw new IllegalArgumentException("Flag 'staticMethodsOnly' is currently not supported");
		}
		// TODO: Consider settings
		return ReflectionUtils.getMethods(clazz);
	}
}
