package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class FieldTest
{
	@Test
	public void testField() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("i", 3)
			.test("d", 2.4)
			.test("s", "xyz")
			.test("l", (long) 1);

		new ErrorTestExecutor(testInstance)
			.test("")
			.test("xyz")
			.test("d,");
	}

	private static class TestClass
	{
		private final int i = 3;
		private final double d = 2.4;
		private final String s = "xyz";
		private final Object l = 1L;
	}
}
