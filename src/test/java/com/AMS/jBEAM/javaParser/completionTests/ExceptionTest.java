package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.settings.AccessLevel;
import org.junit.Test;

public class ExceptionTest
{
	@Test
	public void testMethodException() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("doSomething(getInt(s), ",	"x");

		new ErrorTestExecutor(testInstance)
			.test("String.valueOf(x = 2.0).l", ParseException.class);

		new ErrorTestExecutor(testInstance)
			.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE)
			.test("x = 2.0", ParseException.class);

		new ErrorTestExecutor(testInstance)
			.enableDynamicTyping()
			.test("doSomething(getInt(s), ",	ParseException.class)
			.test("doSomething(++i)",			ParseException.class)
			.test("new TestClass('c').",		ParseException.class);
	}

	private static class TestClass
	{
		private final String	s = "";
		private final double	x = 1.0;
		private final int		i = 13;

		TestClass() {}

		TestClass(char c) {
			throw new UnsupportedOperationException();
		}

		int getInt(String s) {
			throw new UnsupportedOperationException();
		}

		void doSomething(byte b, String s) {}
		void doSomething(int i, double d) {}
	}
}
