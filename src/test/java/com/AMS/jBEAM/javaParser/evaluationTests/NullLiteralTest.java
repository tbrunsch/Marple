package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class NullLiteralTest
{
	@Test
	public void testNullLiteral() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("null",				null)
			.test("getObject(null)",	null);

		new ErrorTestExecutor(testInstance)
			.test("nul")
			.test("getObject(nul)")
			.test("getObject(null");
	}

	private static class TestClass
	{
		Object getObject(Object o) { return o; }
	}
}
