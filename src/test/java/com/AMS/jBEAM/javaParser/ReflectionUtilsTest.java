package com.AMS.jBEAM.javaParser;

import org.junit.Test;

import java.lang.reflect.Field;
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

			Stream<Field> get(String[] fieldNames) {
				return Stream.of(fieldNames)
						.map(fieldName -> Arrays.stream(clazz.getDeclaredFields())
												.filter(field -> field.getName().equals(fieldName))
												.findFirst().get()
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
}
