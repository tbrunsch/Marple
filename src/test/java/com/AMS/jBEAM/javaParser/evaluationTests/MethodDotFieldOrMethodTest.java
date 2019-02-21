package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class MethodDotFieldOrMethodTest
{
	@Test
	public void testMethodDotFieldOrMethod() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("getTestClass().i",				7)
			.test("getTestClass().d",				1.2)
			.test("getTestClass().getString()",	"xyz");
	}

	@Test
	public void testMethodDotFieldOrMethodWithEvaluation() {
		Object testInstance = new TestClass();
		new ErrorTestExecutor(testInstance)
			.test("getTestClassAsObject().i")
			.test("getTestClassAsObject().d")
			.test("getTestClassAsObject().getString()");

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("getTestClassAsObject().i",				7)
			.test("getTestClassAsObject().d",				1.2)
			.test("getTestClassAsObject().getString()",	"xyz");
	}

	private static class TestClass
	{
		private final int i = 7;
		private final double d = 1.2;

		TestClass getTestClass() { return new TestClass(); }
		Object getTestClassAsObject() { return new TestClass(); }
		String getString() { return "xyz"; }
	}
}
