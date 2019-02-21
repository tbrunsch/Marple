package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.settings.Variable;
import org.junit.Test;

public class VariableTest
{
	@Test
	public void testVariable() {
		Variable variable1 = new Variable("xyz", 15.0, true);
		Variable variable2 = new Variable("abc", "Test", true);

		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.addVariable(variable1)
			.addVariable(variable2)
			.test("b + xyz",		18.0)
			.test("xyz * i",		-15000.0)
			.test("(int) xyz / f",	6.0f)
			.test("b + abc",		"3Test")
			.test("abc + i",		"Test-1000")
			.test("abc + f",		"Test2.5")
			.test("xyz + xyz",		30.0)
			.test("abc + abc",		"TestTest")
			.test("test(xyz)",		"15.0")
			.test("test(abc)",		"Test");
	}

	private static class TestClass
	{
		private final byte b = 3;
		private final int i = -1000;
		private final float f = 2.5f;

		String test(Object o) { return o.toString(); }
	}
}
