package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MethodArgumentsTest
{
	@Test
	public void testMethodArguments() {
		Object testInstance = new TestClass1();
		new TestExecutor(testInstance)
			.test("doubleAdd(i,d)", 5.5);

		new ErrorTestExecutor(testInstance)
			.test("doubleAdd(d,i)");
	}

	@Test
	public void testMethodArgumentsDuckTyping() {
		Object testInstance = new TestClass1();
		new ErrorTestExecutor(testInstance)
			.test("objectAdd(i,objectAdd(i,d))");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("objectAdd(i,objectAdd(i,d))", 8.5);
	}

	@Test
	public void testMethodParseErrorAndSideEffect() {
		TestClass2 testInstance = new TestClass2();
		new ErrorTestExecutor(testInstance)
				.test("f(g(), s)");

		assertEquals("Triggered side effect despite parse error", testInstance.sideEffectCounter, 0);
	}

	private static class TestClass1
	{
		private final int i = 3;
		private final double d = 2.5;

		double doubleAdd(int a, double b) { return a + b; }
		Object objectAdd(int a, double b) { return a + b; }
	}

	private static class TestClass2
	{
		private int sideEffectCounter = 0;

		double f(int i, String s) {
			return 1.0;
		}
		int g() { return sideEffectCounter++; }
	}
}
