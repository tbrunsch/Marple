package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class BinaryOperatorTest
{
	@Test
	public void testBinaryOperator() {
		new TestExecutor(null)
			.test("5 *7- 8 / 3*2 + 4 * 2",	5*7 - 8/3*2 + 4*2)
			.test("5 + 7 * 8",				5 + 7 * 8)
			.test("(5 + 7) * 8",			(5 + 7) * 8)
			.test(" 5%3 -7 / 2.0",			5 % 3 - 7/2.0)
			.test("5 + 4 + \"Test\"",		5 + 4 + "Test")
			.test("-27 >> 2 << 2",			-27 >> 2 << 2)
			.test("-23456 >>> 3 << 1",		-23456 >>> 3 << 1)
			.test("(byte) 23 << 2",		(byte) 23 << 2)
			.test("9*3 < 4*7",				9*3 < 4*7)
			.test("9*4 < 3.0*12",			9*4 < 3.0*12)
			.test("9*3 <= 4*7",			9*3 <= 4*7)
			.test("9*4 <= 3.0*12",			9*4 <= 3.0*12)
			.test("5*5 <= 4*6",			5*5 <= 4*6)
			.test("4*7 > 9*3",				4*7 > 9*3)
			.test("3.0*12 > 9*4",			3.0*12 > 9*4)
			.test("4*7 >= 9*3",			4*7 >= 9*3)
			.test("3.0*12 >= 9*4",			3.0*12 >= 9*4)
			.test("4*6 >= 5*5",			4*6 >= 5*5)
			.test("9*3 == 4*7",			9*3 == 4*7)
			.test("9*4 == 3.0*12",			9*4 == 3.0*12)
			.test("5*5 == 4*6",			5*5 == 4*6)
			.test("9*3 != 4*7",			9*3 != 4*7)
			.test("9*4 != 3.0*12",			9*4 != 3.0*12)
			.test("5*5 != 4*6",			5*5 != 4*6)
			.test("123 & 234",				123 & 234)
			.test("123 ^ 234",				123 ^ 234)
			.test("123 | 234",				123 | 234)
			.test("false && false",		false && false)
			.test("false && true",			false && true)
			.test("true && false",			true && false)
			.test("true && true",			true && true)
			.test("false || false",		false || false)
			.test("false || true",			false || true)
			.test("true || false",			true || false)
			.test("true || true",			true || true);
	}

	@Test
	public void testBinaryOperatorShortCircuitEvaluation() {
		Object testInstance = new TestClass1();
		new TestExecutor(testInstance)
			.test("reset().getCounter(FALSE())",					1)
			.test("reset().getCounter(FALSE() && FALSE())",		1)
			.test("reset().getCounter(FALSE() && TRUE())",			1)
			.test("reset().getCounter(TRUE() && FALSE())",			2)
			.test("reset().getCounter(TRUE() && TRUE())",			2)
			.test("reset().getCounter(FALSE() || FALSE())",		2)
			.test("reset().getCounter(FALSE() || TRUE())",			2)
			.test("reset().getCounter(TRUE() || FALSE())",			1)
			.test("reset().getCounter(TRUE() || TRUE())",			1)
			.test("npeTrigger != null && npeTrigger.counter > 0",	false);

		new ErrorTestExecutor(testInstance)
			.test("reset().getCounter(FALSE() && 5")
			.test("reset().getCounter(TRUE() || 'X'");
	}

	@Test
	public void testAssignment() {
		Object testInstance = new TestClass2();
		new TestExecutor(testInstance)
			.test("reset().get(d = 7.0).d",		7.0)
			.test("reset().get(f = -1).f",			-1.f)
			.test("reset().get(i = 13).i",			13)
			.test("reset().get(d = f = i = -3).d",	-3.0)
			.test("reset().get(d = f = i = -3).f",	-3.f)
			.test("reset().get(d = f = i = -3).i",	-3);
	}

	private static class TestClass1
	{
		private int counter 				= 0;
		private final TestClass1 npeTrigger	= null;

		TestClass1 reset() {
			counter = 0;
			return this;
		}

		boolean FALSE() {
			counter++;
			return false;
		}

		boolean TRUE() {
			counter++;
			return true;
		}

		int getCounter(boolean dummy) {
			return counter;
		}
	}

	private static class TestClass2
	{
		private double 	d = 3.0;
		private float 	f = 2.f;
		private int		i = 5;

		TestClass2 reset() {
			d = 3.0;
			f = 2.f;
			i = 5;
			return this;
		}

		TestClass2 get(double dummy) {
			return this;
		}
	}
}
