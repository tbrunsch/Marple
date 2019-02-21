package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.settings.Variable;
import org.junit.Test;

public class NullTest
{
	@Test
	public void testNull() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.addVariable(new Variable("myNull", null, true))
			.test("f(null)",				0)
			.test("f(myNull)",				0)
			.test("f(sNull)",				0)
			.test("f((String) oNull)",		0)
			.test("(String) null",			null)
			.test("myNull = null",			null);

		new ErrorTestExecutor(testInstance)
			.addVariable(new Variable("myNull", null, true))
			.test("f(oNull)")
			.test("null + 0")
			.test("0 + iNull")
			.test("!null")
			.test("null.toString()")
			.test("sNull.length()")
			.test("((TestClass) null).sNull")
			.test("daNull[0]");
	}

	private static class TestClass
	{
		private final String sNull = null;
		private final Object oNull = null;
		private final Integer iNull = null;
		private final double[] daNull = null;

		int f(String s) { return 0; }
	}
}
