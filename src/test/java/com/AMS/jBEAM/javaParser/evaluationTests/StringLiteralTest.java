package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class StringLiteralTest
{
	@Test
	public void testStringLiteral() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("\"xyz\"", "xyz")
			.test("getString(\"xyz\")",	"xyz")
			.test("getString(\"\\\"\")",	"\"")
			.test("getString(\"\\n\")",	"\n")
			.test("getString(\"\\r\")",	"\r")
			.test("getString(\"\\t\")",	"\t");

		new ErrorTestExecutor(testInstance)
			.test("getString(xyz)")
			.test("getString(\"xyz")
			.test("getString(\"xyz)")
			.test("getString(xyz\")")
			.test("getString(\"\\\")");
	}

	private static class TestClass
	{
		String getString(String s) { return s; }
	}
}
