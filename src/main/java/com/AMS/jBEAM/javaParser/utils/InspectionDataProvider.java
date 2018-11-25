package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.JavaParserSettings;
import com.AMS.jBEAM.common.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class InspectionDataProvider
{
	private final JavaParserSettings parserSettings;

	public InspectionDataProvider(JavaParserSettings parserSettings) {
		this.parserSettings = parserSettings;
	}

	public List<Field> getFields(Class<?> clazz, boolean staticFieldsOnly) {
		if (staticFieldsOnly) {
			throw new IllegalArgumentException("Flag 'staticFieldsOnly' is currently not supported");
		}
		// TODO: Consider settings
		return ReflectionUtils.getFields(clazz, true);
	}

	public List<Method> getMethods(Class<?> clazz, boolean staticMethodsOnly) {
		if (staticMethodsOnly) {
			throw new IllegalArgumentException("Flag 'staticMethodsOnly' is currently not supported");
		}
		// TODO: Consider settings
		return ReflectionUtils.getMethods(clazz);
	}
}
