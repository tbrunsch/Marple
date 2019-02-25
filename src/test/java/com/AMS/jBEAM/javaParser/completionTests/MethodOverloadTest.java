package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import org.junit.Test;

public class MethodOverloadTest
{
	@Test
	public void testMethodOverload() {
		final String hashCode = "hashCode()";
		final String toString = "toString()";
		Object testInstance = new TestClass1C();
		new TestExecutor(testInstance)
			.test("getTestClass(",							"intValue", "stringValue")
			.test("getTestClass(i",						"intValue", "int")
			.test("getTestClass(s",						"stringValue", "short")
			.test("getTestClass(intValue,",				"stringValue", toString, "intValue")
			.test("getTestClass(stringValue,",				"intValue", hashCode, "stringValue")
			.test("getTestClass(intValue,stringValue).",	"myInt")
			.test("getTestClass(stringValue,intValue).",	"myString");
	}

	@Test
	public void testMethodOverloadWithEvaluation() {
		Object testInstance = new TestClass2C();
		new ErrorTestExecutor(testInstance)
			.test("getTestClass(getTestClass(i)).", ParseException.class);

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("getTestClass(getTestClass(i)).", "myInt")
			.test("getTestClass(getTestClass(j)).", "myString");
	}

	private static class TestClass1A
	{
		private int myInt;
	}

	private static class TestClass1B
	{
		private String myString;
	}

	private static class TestClass1C
	{
		private int intValue;
		private String stringValue;

		TestClass1A getTestClass(int i, String s) { return null; }
		TestClass1B getTestClass(String s, int i) { return null; }
	}

	private static class TestClass2A
	{
		private int myInt;
	}

	private static class TestClass2B
	{
		private String myString;
	}

	private static class TestClass2C
	{
		private int i = 0;
		private int j = 1;

		Object getTestClass(int i) { return i == 0 ? new TestClass2A() : new TestClass2B(); }

		TestClass2A getTestClass(TestClass2A testClass) { return testClass; }
		TestClass2B getTestClass(TestClass2B testClass) { return testClass; }
	}
}
