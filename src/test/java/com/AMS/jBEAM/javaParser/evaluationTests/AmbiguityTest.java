package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class AmbiguityTest
{
	@Test
	public void testAmbiguity() {
		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("get(c)",	testInstance.get(testInstance.c))
			.test("get(b)",	testInstance.get(testInstance.b))
			.test("get(i)",	testInstance.get(testInstance.i))
			.test("get(l)",	testInstance.get(testInstance.l))
			.test("get(o1)",	testInstance.get(testInstance.o1))
			.test("get(o2)",	testInstance.get(testInstance.o2));

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("get(o1)", testInstance.get(testInstance.o1))
			.test("get(o2)", testInstance.get((Float) testInstance.o2));

		new ErrorTestExecutor(testInstance)
			.test("get(s)");
	}

	private static class TestClass
	{
		private final char c = 'A';
		private final byte b = 123;
		private final short s = (short) 1234;
		private final int i = 123456789;
		private final long l = 5000000000L;
		private final Object o1 = new Double(1.23);
		private final Object o2 = new Float(2.34f);

		char get(char c) { return c; }
		byte get(byte b) { return b; }
		int get(int l) { return i; }
		double get(double d) { return d; }
		Object get(Object o) { return o; }
		Object get(Float f) { return f + 1.0f; }
	}
}
