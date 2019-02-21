package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class MethodArrayTest
{
	@Test
	public void testMethodArray() {
		Object testInstance = new TestClass(0, 1);
		new TestExecutor(testInstance)
			.test("getTestClasses()[i0].i0", 13)
			.test("getTestClasses()[i0].i1", 7)
			.test("getTestClasses()[i1].i0", 4)
			.test("getTestClasses()[i1].i1", 9);
	}

	@Test
	public void testMethodArrayWithEvaluation() {
		Object testInstance = new TestClass(0, 1);
		new ErrorTestExecutor(testInstance)
			.test("getTestClasses()[o].o")
			.test("getTestClasses()[o].getI1()")
			.test("getTestClasses()[getI1()].o")
			.test("getTestClasses()[getI1()].getI1()");

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("getTestClasses()[o].o",				13)
			.test("getTestClasses()[o].getI1()",		7)
			.test("getTestClasses()[getI1()].o",		4)
			.test("getTestClasses()[getI1()].getI1()",	9);
	}

	private static class TestClass
	{
		private final int i0;
		private final int i1;
		private final Object o;

		TestClass(int i0, int i1) {
			this.i0 = i0;
			this.i1 = i1;
			this.o = i0;
		}

		Object getI1() {
			return i1;
		}

		TestClass[] getTestClasses() {
			return new TestClass[] { new TestClass(13, 7), new TestClass(4, 9) };
		}
	}
}
