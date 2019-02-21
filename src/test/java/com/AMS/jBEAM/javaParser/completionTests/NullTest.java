package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.settings.Variable;
import org.junit.Test;

public class NullTest
{
	@Test
	public void testNull() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.addVariable(new Variable("myNull", null, true))
			.test("f(",			"myNull", "sNull")
			.test("f((String) oN",	"oNull")
			.test("sNull.le",		"length()");

		new ErrorTestExecutor(testInstance)
			.addVariable(new Variable("myNull", null, true))
			.test("myNull.",	7, ParseException.class)
			.test("null.",		5, ParseException.class);
	}

	private static class TestClass
	{
		private String sNull = null;
		private Object oNull = null;
		private Integer iNull = null;
		private double[] daNull = null;

		int f(String s) { return 0; }
	}
}
