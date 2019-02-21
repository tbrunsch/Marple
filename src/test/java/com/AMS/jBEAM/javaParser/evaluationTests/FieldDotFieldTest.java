package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import org.junit.Test;

public class FieldDotFieldTest
{
	@Test
	public void testFieldDotField() {
		Object testInstance = new TestClass2();
		new TestExecutor(testInstance)
			.test("tc.i", 2)
			.test("tc.f", 1.3f);
	}

	@Test
	public void testFieldDotFieldWithDuckTyping() {
		Object testInstance = new TestClass2();
		new ErrorTestExecutor(testInstance)
			.test("o.i")
			.test("o.f");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("o.i", 2)
			.test("o.f", 1.3f);
	}

	private static class TestClass1
	{
		private final int i = 2;
		private final float f = 1.3f;
	}

	private static class TestClass2
	{
		private final TestClass1 	tc = new TestClass1();
		private final Object		o = new TestClass1();
	}
}
