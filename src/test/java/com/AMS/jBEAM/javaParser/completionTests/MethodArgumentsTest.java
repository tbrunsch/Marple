package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import org.junit.Test;

public class MethodArgumentsTest
{
	@Test
	public void testMethodArguments() {
		Object testInstance = new TestClass1();
		new TestExecutor(testInstance)
			.test("prefix",	"prefixC", "prefixD", "prefixI", "prefixC()", "prefixD()", "prefixI()")
			.test("prefixI",	"prefixI", "prefixI()", "prefixC", "prefixD")
			.test("prefixD",	"prefixD", "prefixD()", "prefixC", "prefixI")
			.test("prefixC",	"prefixC", "prefixC()", "prefixD", "prefixI")
			.test("prefixI(",	"prefixD", "prefixD()", "prefixC", "prefixI", "prefixC()", "prefixI()")
			.test("prefixD(",	"prefixC", "prefixC()", "prefixD", "prefixI", "prefixD()", "prefixI()")
			.test("prefixC(",	"prefixI", "prefixI()", "hashCode()", "prefixC", "prefixC()", "prefixD", "prefixD()");

		new ErrorTestExecutor(testInstance)
			.test("prefixI(prefixD)",	-1, IllegalStateException.class)
			.test("prefixD(prefixI)",	ParseException.class)
			.test("prefixC(prefixI,",	ParseException.class)
			.test("prefixI(prefixD))",	-1, ParseException.class);
	}

	@Test
	public void testMethodArgumentsWithEvaluation() {
		Object testInstance = new TestClass2();
		new ErrorTestExecutor(testInstance)
			.test("getTestClassObject(getObject()).get", ParseException.class);

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("getTestClassObject(getObject()).get", "getObject()", "getTestClassObject()", "getClass()");
	}

	private static class TestClass1
	{
		private int 	prefixI	= 1;
		private double 	prefixD	= 1.0;
		private char	prefixC	= 'A';

		private int		prefixI(double arg)	{ return 1; }
		private double	prefixD(char arg)	{ return 1.0; }
		private char	prefixC(int arg)	{ return 'A'; }
	}

	private static class TestClass2
	{
		private Object getObject() { return new TestClass2(); }
		private Object getTestClassObject(TestClass2 testClass) { return testClass.getObject(); }
	}
}
