package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import org.junit.Test;

public class FieldDotFieldTest
{
	@Test
	public void testFieldDotField() {
		TestClass1 testInstance = new TestClass1();
		new TestExecutor(testInstance)
			.test("member.",		"member", "X", "xy", "XYZ")
			.test("member.x",		"X", "xy", "XYZ", "member")
			.test("member.xy",		"xy", "XYZ", "X", "member")
			.test("member.xyz",	"XYZ", "xy", "X", "member")
			.test("member.mem",	"member", "X", "xy", "XYZ");

		new ErrorTestExecutor(testInstance)
			.test("membeR.",		-1, ParseException.class)
			.test("MEMBER.xy",		-1, ParseException.class)
			.test("member.xy.XY",	-1, ParseException.class)
			.test("member.xy",		-1, IllegalStateException.class);
	}

	@Test
	public void testFieldDotFieldWithEvaluation() {
		TestClass2 testInstance = new TestClass2();
		new TestExecutor(testInstance)
			.test("member.",		"x", "xyz")
			.test("member.x",		"x", "xyz")
			.test("member.xy",		"xyz", "x")
			.test("member.xyz",	"xyz", "x");

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("member.",		"xy", "x", "xyz")
			.test("member.x",		"x", "xy", "xyz")
			.test("member.xy",		"xy", "xyz", "x")
			.test("member.xyz",	"xyz", "xy", "x");
	}

	private static class TestClass1
	{
		private int 		xy 		= 13;
		private float		X		= 1.0f;
		private char 		XYZ		= 'W';

		private TestClass1 member	= null;
	}

	private static class BaseClass
	{
		private int x;
		private int xyz;
	}

	private static class DescendantClass extends BaseClass
	{
		private int	xy;
	}

	private static class TestClass2
	{
		private BaseClass member = new DescendantClass();
	}
}
