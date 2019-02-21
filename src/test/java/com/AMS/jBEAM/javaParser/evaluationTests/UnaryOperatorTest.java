package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class UnaryOperatorTest
{
	@Test
	public void testUnaryOperator() {
		new TestExecutor(new TestClass())
			.test("++reset().b",			(byte) 14)
			.test("reset().get(++b).b",	(byte) 14)
			.test("++reset().i",			-20)
			.test("reset().get(++i).i",	-20)
			.test("--reset().b",			(byte) 12)
			.test("reset().get(--b).b",	(byte) 12)
			.test("--reset().i",			-22)
			.test("reset().get(--i).i",	-22)
			.test("+reset().b",			13)
			.test("+reset().i",			-21)
			.test("+reset().f",			2.5f)
			.test("-reset().b",			-13)
			.test("-reset().i",			21)
			.test("-reset().f",			-2.5f)
			.test("!false",				true)
			.test("!true",					false)
			.test("!(false || true)",		false)
			.test("!(true && false)",		true)
			.test("~12345", ~12345);

		new ErrorTestExecutor(new TestClass())
			.test("++f")
			.test("++j")
			.test("++s")
			.test("--f")
			.test("--j")
			.test("--s")
			.test("+s")
			.test("-s")
			.test("!1")
			.test("!null")
			.test("~f");
	}

	private static class TestClass
	{
		private byte b = 13;
		private int	i = -21;
		private float f = 2.5f;
		private final String s = "Test";
		private final int j = 123;

		TestClass reset() {
			b = 13;
			i = -21;
			f = 2.5f;
			return this;
		}

		TestClass get(int dummy) { return this; }
	}
}
