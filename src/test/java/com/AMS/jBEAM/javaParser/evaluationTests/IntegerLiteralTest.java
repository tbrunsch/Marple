package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class IntegerLiteralTest
{
	@Test
	public void testIntegerLiteral() {
		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("120",						120)
			.test("getByte((byte) 120)",		testInstance.getByte((byte) 120))
			.test("1234",						1234)
			.test("getShort((short) 1234)",	testInstance.getShort((short) 1234))
			.test("100000",					100000)
			.test("getInt(100000)",			testInstance.getInt(100000))
			.test("5000000000L",				5000000000L)
			.test("getLong(5000000000l)",		testInstance.getLong(5000000000l));

		new ErrorTestExecutor(testInstance)
			.test("getByte(123)")
			.test("getShort(1000)")
			.test("getInt(5000000000)");
	}

	private static class TestClass
	{
		byte getByte(byte b) { return b; }
		short getShort(short s) { return s; }
		int getInt(int i) { return i; }
		long getLong(long l) { return l; }
	}
}
