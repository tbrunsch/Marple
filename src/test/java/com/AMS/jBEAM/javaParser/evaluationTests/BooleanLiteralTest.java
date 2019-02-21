package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class BooleanLiteralTest
{
	@Test
	public void testBooleanLiteral() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("true",				true)
			.test("false",				false)
			.test("getBoolean(true)",	true)
			.test("getBoolean(false)",	false);

		new ErrorTestExecutor(testInstance)
			.test("getBoolean(tru)")
			.test("getBoolean(fals)")
			.test("getBoolean(TRUE)")
			.test("getBoolean(FALSE)");
	}

	private static class TestClass
	{
		boolean getBoolean(boolean b) { return b; }
	}
}
