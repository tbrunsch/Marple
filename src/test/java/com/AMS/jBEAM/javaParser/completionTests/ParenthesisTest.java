package com.AMS.jBEAM.javaParser.completionTests;

import org.junit.Test;

public class ParenthesisTest
{
	@Test
	public void testParenthesizedExpression() {
		final String getClass = "getClass()";
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("(",								"x", "y", "getFloat()", "goDoNothing()")
			.test("(g",							"getFloat()", "goDoNothing()", getClass)
			.test("(getFloat(y).toString()).le",	"length()");
	}

	private static class TestClass
	{
		private int y = 1;
		private double x = 2.0;

		void goDoNothing() {}
		Float getFloat(int i) { return i + 0.5f; }
	}
}
