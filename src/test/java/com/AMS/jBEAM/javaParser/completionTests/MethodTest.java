package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class MethodTest
{
	@Test
	public void testMethod() {
		/*
		 * Convention for this test: Methods whose names consist of n characters have n arguments.
		 *
		 * This allows for auto-generating the insertion text of a method:
		 *
		 * "x" -> "x()", "xy" -> "xy(, )", "xyz" -> "xyz(, , )" etc.
		 *
		 * Exception: Method "other" whose sole purpose is not to appear among the first suggestions,
		 *			which makes this test stronger.
		 */
		Object testInstance = new TestClass2();
		new TestExecutor(testInstance)
			.test("xy",	formatMethods("xy", "XY", "xy_z", "XYZ", "XYZ", "x", "X"))
			.test("XYZ",	formatMethods("XYZ", "XYZ", "XY", "X", "x", "xy"))
			.test("X",		formatMethods("X", "x", "XY", "XYZ", "XYZ", "xy_z", "xy"))
			.test("XY",	formatMethods("XY", "xy", "XYZ", "XYZ", "xy_z", "X", "x"))
			.test("xy_z",	formatMethods("xy_z", "x", "xy", "XY", "X"))
			.test("x",		formatMethods("x", "X", "xy_z", "xy", "XY", "XYZ", "XYZ"))
			.test("XYW",	formatMethods("XY", "X", "x", "xy"));

		new ErrorTestExecutor(testInstance)
			.test("other()",	-1, IllegalStateException.class)
			.test("bla",		-1, ParseException.class)
			.test("other(),",	8, ParseException.class);
	}

	private static String formatMethod(String methodName) {
		int numArguments = methodName.length();	// convention in testMethod()
		return methodName + "(" + IntStream.range(0, numArguments).mapToObj(i -> "").collect(Collectors.joining(", ")) + ")";
	}

	private static String[] formatMethods(String... methodNames) {
		return Arrays.stream(methodNames).map(MethodTest::formatMethod).toArray(size -> new String[size]);
	}

	private static class TestClass1
	{
		private int sideEffectCounter = 0;

		double f(int i, String s) {
			return 1.0;
		}
		int g() { return sideEffectCounter++; }
	}

	private static abstract class BasicTestClass
	{
		private int 	xy(char c, float f)						{ return 13; }
		private char 	XYZ(float f, int i, double d)			{ return 'W'; }
		private float	X(int i)								{ return 1.0f; }

		private short	other()									{ return 0; }
	}

	private static class TestClass2 extends BasicTestClass
	{
		private String 	XY(long l, double d)					{ return "27"; }
		private long	xy_z(double d, short s, int i, byte b)	{ return 13; }
		private double	x(double d)								{ return 2.72; }

		private byte	XYZ(char c, double d, boolean b)		{ return 1; }
	}
}
