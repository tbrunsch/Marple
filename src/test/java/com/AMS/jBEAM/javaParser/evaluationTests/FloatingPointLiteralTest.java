package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class FloatingPointLiteralTest
{
	@Test
	public void testFloatingPointLiteral() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("123f",						123f)
			.test("getFloat(123f)",			123f)
			.test("123e0",						123e0)
			.test("getDouble(123e0)",			123e0)
			.test("-123e+07",					-123e+07)
			.test("getDouble(-123e+07)",		-123e+07)
			.test("+123e-13F",					+123e-13F)
			.test("getFloat(+123e-13F)",		+123e-13F)
			.test("-123.456E1d",				-123.456E1d)
			.test("getDouble(-123.456E1d)",	-123.456E1d)
			.test("123.d",						123.d)
			.test("getDouble(123.d)",			123.d)
			.test("123.e2D",					123.e2D)
			.test("getDouble(123.e2D)",		123.e2D)
			.test("123.456f",					123.456f)
			.test("getFloat(123.456f)",		123.456f)
			.test("+.1e-1d",					+.1e-1d)
			.test("getDouble(+.1e-1d)",		+.1e-1d)
			.test("-.2e3f",					-.2e3f)
			.test("getFloat(-.2e3f)",			-.2e3f);
	}

	private static class TestClass
	{
		float getFloat(float f) { return f; }
		double getDouble(double d) { return d; }
	}
}
