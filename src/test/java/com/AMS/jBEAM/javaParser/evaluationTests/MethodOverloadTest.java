package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import org.junit.Test;

public class MethodOverloadTest
{
	@Test
	public void testMethodOverload() {
		Object testInstance = new TestClass1C();
		new TestExecutor(testInstance)
			.test("getTestClass(myInt).i",		3)
			.test("getTestClass(myString).d",	2.7);

		new ErrorTestExecutor(testInstance)
			.test("getTestClass(myInt).d")
			.test("getTestClass(myString).i");
	}

	@Test
	public void testMethodOverloadWithEvaluation() {
		Object testInstance = new TestClass2C();
		new ErrorTestExecutor(testInstance)
			.test("getTestClass(getTestClass(i)).myInt")
			.test("getTestClass(getTestClass(j)).myString");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DYNAMICALLY_TYPED)
			.test("getTestClass(getTestClass(i)).myInt",		7)
			.test("getTestClass(getTestClass(j)).myString",	"abc");

		new ErrorTestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DYNAMICALLY_TYPED)
			.test("getTestClass(getTestClass(i)).myString")
			.test("getTestClass(getTestClass(j)).myInt");
	}

	@Test
	public void testMethodOverloadSideEffects() {
		/*
		 * It is important that expression are not evaluated multiple times
		 * when searching for the right method overload. Otherwise, side effects
		 * (which is critical anyway) may also occur multiple times.
		 */
		TestClass3 testInstance = new TestClass3();
		new TestExecutor(testInstance)
			.test("f(getInt(), 1.0f)",		1)
			.test("f(getInt(), \"Test\")",	2)
			.test("f(getInt(), \"Test\")",	3)
			.test("f(getInt(), 1.0f)",		4);
	}

	private static class TestClass1A
	{
		private final int i = 3;
	}

	private static class TestClass1B
	{
		private final double d = 2.7;
	}

	private static class TestClass1C
	{
		private final int myInt = 3;
		private final String myString = "xyz";

		TestClass1A getTestClass(int i) { return new TestClass1A(); }
		TestClass1B getTestClass(String s) { return new TestClass1B(); }
	}

	private static class TestClass2A
	{
		private final int myInt = 7;
	}

	private static class TestClass2B
	{
		private final String myString = "abc";
	}

	private static class TestClass2C
	{
		private final int i = 0;
		private final int j = 1;

		Object getTestClass(int i) { return i == 0 ? new TestClass2A() : new TestClass2B(); }

		TestClass2A getTestClass(TestClass2A testClass) { return testClass; }
		TestClass2B getTestClass(TestClass2B testClass) { return testClass; }
	}

	private static class TestClass3
	{
		private int count = 0;

		int f(int i, float f) { return i; };
		int f(int i, String s) { return i; };

		int getInt() { return ++count; }
	}
}
