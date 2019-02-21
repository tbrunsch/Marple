package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class ThisLiteralTest
{
	@Test
	public void testThisLiteral() {
		Object testInstance = new TestClass(23);
		new TestExecutor(testInstance)
			.test("this.value",		23)
			.test("getValue(this)",	23);
	}

	private static class TestClass
	{
		private final int value;

		TestClass(int value) { this.value = value; }
		int getValue(TestClass testInstance) { return testInstance.value; }
	}
}
