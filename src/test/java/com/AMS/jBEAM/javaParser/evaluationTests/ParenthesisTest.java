package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class ParenthesisTest
{
	@Test
	public void testParenthesizedExpression() {
		final String getClass = "getClass()";
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("(getFloat(y).toString())",			"1.5")
			.test("(getFloat(y)).toString()",			"1.5")
			.test("(getFloat(y).toString()).length()",	3)
			.test("((x))",								2.0)
			.test("(((1.3e-7)))",						1.3e-7);
	}

	private static class TestClass
	{
		private final int y = 1;
		private final double x = 2.0;

		void goDoNothing() {}
		Float getFloat(int i) { return i + 0.5f; }
	}
}
