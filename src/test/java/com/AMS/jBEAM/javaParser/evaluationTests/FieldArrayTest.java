package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import org.junit.Test;

public class FieldArrayTest
{
	@Test
	public void testFieldArray() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("array[i0]", 1)
			.test("array[i1]", 4)
			.test("array[i2]", 3)
			.test("array[i3]", 7);
	}

	@Test
	public void testFieldArrayWithDuckTyping() {
		Object testInstance = new TestClass();
		new ErrorTestExecutor(testInstance)
			.test("o[i0]")
			.test("o[i1]")
			.test("o[i2]")
			.test("o[i3]");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("o[i0]", 1)
			.test("o[i1]", 4)
			.test("o[i2]", 3)
			.test("o[i3]", 7);
	}

	private static class TestClass
	{
		private final int i0 = 3;
		private final int i1 = 2;
		private final int i2 = 1;
		private final int i3 = 0;
		private final int[] array = { 7, 3, 4, 1 };
		private final Object o = new int[]{ 7, 3, 4, 1 };
	}
}
