package com.AMS.jBEAM.javaParser.completionTests;

import org.junit.Test;

public class ConstructorTest
{
	@Test
	public void testConstructor() {
		Object testInstance = new ConstructorTestClass(5, -2.0f);
		new TestExecutor(testInstance)
			.test("new ConstructorT",							"ConstructorTestClass")
			.test("new ConstructorTestClass(",					"i", "o")
			.test("new ConstructorTestClass(i, ",				"i")
			.test("new ConstructorTestClass(o, ",				"i")
			.test("new ConstructorTestClass(\"bla\", ",		"d", "i")
			.test("new ConstructorTestClass(\"bla\", i, ",		"i")
			.test("new ConstructorTestClass(\"bla\", d, i).",	"d", "i", "o");
	}

	@Test
	public void testArrayCreation() {
		Object testInstance = new ArrayCreationTestClass();
		new TestExecutor(testInstance)
			.test("new int[", 			"i")
			.test("new int[]{ ",		"i")
			.test("new String[",		"i")
			.test("new String[]{ ",	"s");
	}

	private static class ConstructorTestClass
	{
		private final int i;
		private final double d;
		private final Object o;

		ConstructorTestClass(int i, float f) {
			this.i = i;
			this.d = f;
			o = this;
		}

		ConstructorTestClass(String s, double d, int i) {
			this.i = i;
			this.d = d;
			this.o = s;
		}

		ConstructorTestClass(Object o, int i) {
			this.i = i;
			this.d = 0.0;
			this.o = o;
		}
	}

	private static class ArrayCreationTestClass
	{
		private final int i = 3;
		private final String s = "X";
	}
}
