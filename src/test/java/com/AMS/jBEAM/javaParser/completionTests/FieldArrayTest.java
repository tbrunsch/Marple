package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
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
			.test("xy[",			3, ParseException.class)
			.test("xyz[",			4, ParseException.class)
			.test("xyzw[",			5, ParseException.class)
			.test("member[xy].",	11, ParseException.class)
			.test("member[xyz]",	-1, IllegalStateException.class)
			.test("member[xyz)",	11, ParseException.class);
	}

	@Test
	public void testFieldArrayWithEvaluation() {
		TestClass2 testInstance = new TestClass2();
		new ErrorTestExecutor(testInstance)
			.test("array[", 6, ParseException.class);

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("array[",		"i0", "i1", "i2")
			.test("array[i0].",	"value")
			.test("array[i1].",	"index");

		new ErrorTestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("array[i2].",				10, ParseException.class)
			.test("array[array[i1].index].",	23, ParseException.class);
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
