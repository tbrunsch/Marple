package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import org.junit.Test;

public class FieldTest
{
	@Test
	public void testField() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("xy",	"xy", "XY", "xy_z", "XYZ", "x", "X")
			.test("XYZ",	"XYZ", "XY", "X", "x", "xy")
			.test("X",		"X", "x", "XY", "XYZ", "xy_z", "xy")
			.test("XY",	"XY", "xy", "XYZ", "xy_z", "X", "x")
			.test("xy_z",	"xy_z", "x", "xy", "XY", "X")
			.test("x",		"x", "X", "xy_z", "xy", "XY", "XYZ")
			.test("XYW",	"XY", "X", "x", "xy");

		new ErrorTestExecutor(testInstance)
			.test("xy", -1,	IllegalStateException.class)
			.test("bla", -1, ParseException.class)
			.test("xy,",			ParseException.class);
	}

	private static abstract class BasicTestClass
	{
		private int 	xy 		= 13;
		private char 	XYZ		= 'W';
		private float	X		= 1.0f;

		private short	other	= 0;
	}

	private static class TestClass extends BasicTestClass
	{
		private String 	XY 		= "27";
		private long	xy_z	= 13;
		private double	x		= 2.72;
	}
}
