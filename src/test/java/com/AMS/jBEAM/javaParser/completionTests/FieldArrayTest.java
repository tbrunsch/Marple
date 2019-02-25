package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import org.junit.Test;

public class FieldArrayTest
{
	@Test
	public void testFieldArray() {
		final String hashCode = "hashCode()";
		TestClass1 testInstance = new TestClass1();
		new TestExecutor(testInstance)
			.test("member[",			"xyz", hashCode, "xyzw", "member", "xy")
			.test("member[x",			"xyz", "xyzw", "xy", hashCode, "member")
			.test("member[xy",			"xy", "xyz", "xyzw", hashCode, "member")
			.test("member[xyz",		"xyz", "xyzw", "xy", hashCode, "member")
			.test("member[xyzw",		"xyzw", "xyz", "xy", hashCode, "member")
			.test("member[mem",		"member", "xyz", hashCode, "xyzw", "xy")
			.test("member[xyz].",		"member", "xy", "xyz", "xyzw")
			.test("member[xyzw].x",	"xy", "xyz", "xyzw", "member");

		new ErrorTestExecutor(testInstance)
			.test("xy[",			ParseException.class)
			.test("xyz[",			ParseException.class)
			.test("xyzw[",			ParseException.class)
			.test("member[xy].",	ParseException.class)
			.test("member[xyz]",	-1, IllegalStateException.class)
			.test("member[xyz)",	ParseException.class);
	}

	@Test
	public void testFieldArrayWithEvaluation() {
		TestClass2 testInstance = new TestClass2();
		new ErrorTestExecutor(testInstance)
			.test("array[", ParseException.class);

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("array[",		"i0", "i1", "i2")
			.test("array[i0].",	"value")
			.test("array[i1].",	"index");

		new ErrorTestExecutor(testInstance)
			.enableDynamicTyping()
			.test("array[i2].",				ParseException.class)
			.test("array[array[i1].index].",	ParseException.class);
	}

	private static class TestClass1
	{
		private String		xy		= "xy";
		private int			xyz		= 7;
		private char		xyzw	= 'W';

		private TestClass1[]	member	= null;
	}

	private static class ElementClass0
	{
		private double value = 1.0;
	}

	private static class ElementClass1
	{
		private int index = 3;
	}

	private static class TestClass2
	{
		private int 	i0		= 0;
		private int		i1		= 1;
		private int 	i2		= 2;

		private Object 	array	= new Object[] { new ElementClass0(), new ElementClass1() };
	}
}
