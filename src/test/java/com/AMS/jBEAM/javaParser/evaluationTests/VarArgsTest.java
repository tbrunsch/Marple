package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

import java.util.Arrays;

public class VarArgsTest
{
	@Test
	public void testVarArgs() {
		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("sum(d)",				3.5)
			.test("sum(d, i1)",			6.5)
			.test("sum(d, i1, i2)",		1.5)
			.test("sum(d, i1, i2, i3)",	12.5)
			.test("sum(d, i123)",			10.5)
			.test("sum(i1, i123)",			10.0);

		new ErrorTestExecutor(testInstance)
			.test("sum()")
			.test("sum(i1, d)")
			.test("sum(d, i123, i1)")
			.test("sum(d, i1, i123)")
			.test("sum(d, i123, i123)");
	}

	private static class TestClass
	{
		private double d = 3.5;
		private final int i1 = 3;
		private final int i2 = -5;
		private final int i3 = 11;
		private final int[] i123 = { 1, 2, 4 };

		double sum(double offset, int... ints) {
			return offset + Arrays.stream(ints).sum();
		}
	}
}
