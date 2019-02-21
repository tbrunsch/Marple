package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import org.junit.Test;

public class MethodDotFieldOrMethodTest
{
	@Test
	public void testMethodDotFieldOrMethod() {
		Object testInstance = new TestClass1B();
		new TestExecutor(testInstance)
			.test("getTestClass",									"getTestClass()")
			.test("getTestClass(c).",								"d", "i", "getObject()")
			.test("getTestClass(c).get",							"getObject()")
			.test("getTestClass(c).getObject(",					"c", "s")
			.test("getTestClass(c).getObject(getTestClass(c).d).",	"clone()", "equals()");

		new ErrorTestExecutor(testInstance)
			.test("getTestClazz().",								-1, ParseException.class)
			.test("getTestClazz().i",								-1, ParseException.class)
			.test("getTestClass().i.d",							-1, ParseException.class)
			.test("getTestClass(c).getObject(getTestClass(c).d)",	-1, IllegalStateException.class);
	}

	@Test
	public void testMethodDotFieldOrMethodWithEvaluation() {
		final String getClass = "getClass()";
		Object testInstance = new TestClass2();
		new TestExecutor(testInstance)
			.test("getObject().",		"x", "xyz", "getInt()")
			.test("getObject().x",		"x", "xyz", "getInt()")
			.test("getObject().xy",	"xyz", "x", "getInt()")
			.test("getObject().xyz",	"xyz", "x", "getInt()")
			.test("getObject().get",	"getInt()", getClass, "x", "xyz");

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("getObject().",		"xy", "x", "xyz", "getDouble()", "getInt()")
			.test("getObject().x",		"x", "xy", "xyz", "getDouble()", "getInt()")
			.test("getObject().xy",	"xy", "xyz", "x", "getDouble()", "getInt()")
			.test("getObject().xyz",	"xyz", "xy", "x", "getDouble()", "getInt()")
			.test("getObject().get",	"getDouble()", "getInt()", getClass, "xy", "x", "xyz");
	}

	private static class TestClass1A
	{
		private int 	i	= 1;
		private double	d	= 1.0;

		private Object getObject(double d) { return null; }
	}

	private static class TestClass1B
	{
		private short	s	= 1;
		private char	c	= 'A';

		private TestClass1A getTestClass(char c) { return null; }
	}

	private static class BaseClass
	{
		private int x;
		private int xyz;

		public int getInt() { return 1; }
	}

	private static class DescendantClass extends BaseClass
	{
		private int	xy;

		public double getDouble() { return 1.0; }
	}

	private static class TestClass2
	{
		private BaseClass getObject() { return new DescendantClass(); }
	}
}
