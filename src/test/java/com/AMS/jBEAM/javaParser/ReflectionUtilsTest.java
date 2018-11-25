package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.common.ReflectionUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class ReflectionUtilsTest
{
	@Test
	public void getFieldsShadowingTest() {
		class BaseClass
		{
			public int y;
			int z;
			private int x;
			protected int w;
		}

		class DerivedClass extends BaseClass
		{
			int h;
			private int e;
			public int y;
		}

		class FieldGenerator
		{
			Class<?> clazz;

			FieldGenerator(Class<?> clazz) {
				this.clazz  = clazz;
			}

			Stream<Field> get(String... fieldNames) {
				return Stream.of(fieldNames)
						.flatMap(fieldName -> Arrays.stream(clazz.getDeclaredFields())
												.filter(field -> field.getName().equals(fieldName))
						);
			}
		}

		for (boolean filterShadowedFields : Arrays.asList(true, false)) {
			List<Field> actualFields = ReflectionUtils.getFields(DerivedClass.class, filterShadowedFields);
			String[] expectedFieldNamesDerived = { "e", "h", "y" };
			String[] expectedFieldNamesBase = filterShadowedFields ? new String[]{ "w", "x", "z" } : new String[] { "w", "x", "y", "z" };
			List<Field> expectedFields = Stream.concat(
				new FieldGenerator(DerivedClass.class).get(expectedFieldNamesDerived),
				new FieldGenerator(BaseClass.class).get(expectedFieldNamesBase)
			).collect(Collectors.toList());

			assertEquals("Unexpected number of fields", expectedFields.size(), actualFields.size());
			for (int i = 0; i < expectedFields.size(); i++) {
				Field expectedField = expectedFields.get(i);
				Field actualField = actualFields.get(i);
				assertEquals("Unexpected field", expectedField, actualField);
			}
		}
	}

	@Test
	public void getMethodsOverriddenMethodsTest() {
		class MyBaseClass
		{
			public int f3() { return 3; }
			int f4() { return 4; }
			private int f2() { return 2; }
			protected int f1() { return 1; }
			int f5(String s) { return 5; }
		}

		class MyDerivedClass extends MyBaseClass
		{
			int f6() { return 6; }
			double f2() { return 2.0; }
			private int f5() { return 5; }
			public int f3() { return 3; }
		}

		class MethodGenerator
		{
			Class<?> clazz;

			MethodGenerator(Class<?> clazz) {
				this.clazz  = clazz;
			}

			Stream<Method> get(String... methodNames) {
				return Stream.of(methodNames)
						.flatMap(methodName -> Arrays.stream(clazz.getDeclaredMethods())
								.filter(method -> method.getName().equals(methodName))
						);
			}
		}

		// We do not want to consider the methods MyBaseClass inherits from Object. This may differ
		// between different Java versions and makes the test less understandable.
		List<Method> actualMethods = ReflectionUtils.getMethods(MyDerivedClass.class).stream()
										.filter(method -> method.getDeclaringClass().getSimpleName().startsWith("My"))
										.collect(Collectors.toList());
		List<Method> expectedMethods = Stream.concat(
				new MethodGenerator(MyDerivedClass.class).get("f2", "f3", "f5", "f6"),
				new MethodGenerator(MyBaseClass.class).get("f1", "f4", "f5", "f6")
		).collect(Collectors.toList());

		assertEquals("Unexpected number of methods", expectedMethods.size(), actualMethods.size());
		for (int i = 0; i < expectedMethods.size(); i++) {
			Method expectedMethod = expectedMethods.get(i);
			Method actualMethod = actualMethods.get(i);
			assertEquals("Unexpected method", expectedMethod, actualMethod);
		}
	}
}
