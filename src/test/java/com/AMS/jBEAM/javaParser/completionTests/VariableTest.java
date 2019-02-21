package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.settings.Variable;
import org.junit.Test;

public class VariableTest
{
	@Test
	public void testVariables() {
		Variable variable1 = new Variable("xyz", 13.0, true);
		Variable variable2 = new Variable("abc", "Test", true);

		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.addVariable(variable1)
			.addVariable(variable2)
			.test("x",			"x", "xyz", "xy", "xyzw", "abc")
			.test("xy",		"xy", "xyz", "xyzw", "x", "abc")
			.test("xyz",		"xyz", "xyzw", "x", "xy", "abc")
			.test("xyzw",		"xyzw", "xyz", "x", "xy", "abc")
			.test("abc",		"abc", "xyz", "x", "xy", "xyzw")
			.test("test(",		"xyzw", "abc", "xyz", "x", "xy")
			.test("test(x",	"x", "xyzw", "xyz", "xy", "abc")
			.test("test(xy",	"xy", "xyzw", "xyz", "x", "abc")
			.test("test(xyz",	"xyz", "xyzw", "x", "xy", "abc")
			.test("test(xyzw",	"xyzw", "xyz", "x", "xy", "abc")
			.test("test(abc",	"abc", "xyzw", "xyz", "x", "xy");
	}

	private static class TestClass
	{
		private int xy;
		private byte xyzw;
		private float x;

		void test(byte b) {}
	}
}
