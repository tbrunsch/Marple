package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class SpacesTest
{
	@Test
	public void testSpacesInExpressions() {
		Object testInstance = new TestClass("abc", (short) 13, 'X', 123456789, -13e02f, 1L, false, 2.34e-56);
		new TestExecutor(testInstance)
			.test(" s ", "abc")
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ) . sValue ",	(short) 13)
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ) .s",			"abc" + "_xyz")
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ). c",			'X')
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ).i ",			123456789 + 1)
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d) .f",				-13e02f / 2.f)
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d ). l",			1L * 3)
			.test("  getTestClass (s,sValue,  c,i  ,  f,l,b,d ).b",					!false)
			.test("  getTestClass( s, sValue, c, i, f, l, b, d  ) . d",				3 - 2.34e-56);
	}

	private static class TestClass
	{
		private final String s;
		private final short sValue;
		private final char c;
		private final int i;
		private final float f;
		private final long l;
		private final boolean b;
		private final double d;

		TestClass(String s, short sValue, char c, int i, float f, long l, boolean b, double d) {
			this.s = s;
			this.sValue = sValue;
			this.c = c;
			this.i = i;
			this.f = f;
			this.l = l;
			this.b = b;
			this.d = d;
		}

		TestClass getTestClass(String s, short sValue, char c, int i, float f, long l, boolean b, double d) {
			return new TestClass(s + "_xyz", sValue, c, i + 1, f / 2.f, l * 3, !b, 3 - d);
		}
	}
}
