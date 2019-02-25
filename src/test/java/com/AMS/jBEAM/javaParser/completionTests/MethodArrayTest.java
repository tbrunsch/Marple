package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import org.junit.Test;

import java.util.stream.IntStream;

public class MethodArrayTest
{
	@Test
	public void testMethodArray() {
		final String hashCode = "hashCode()";
		final String getClass = "getClass()";
		Object testInstance = new TestClass1();
		new TestExecutor(testInstance)
			.test("getTestClasses()[",			"xyz", hashCode, "xyzw", "xy")
			.test("getTestClasses()[x",		"xyz", "xyzw", "xy", hashCode)
			.test("getTestClasses()[xy",		"xy", "xyz", "xyzw", hashCode)
			.test("getTestClasses()[xyz",		"xyz", "xyzw", "xy", hashCode)
			.test("getTestClasses()[xyzw",		"xyzw", "xyz", "xy", hashCode)
			.test("getTestClasses()[get",		"getTestClasses()", getClass, "xyz", hashCode, "xyzw", "xy")
			.test("getTestClasses()[xyz].",	"xy", "xyz", "xyzw")
			.test("getTestClasses()[xyzw].x",	"xy", "xyz", "xyzw");

		new ErrorTestExecutor(testInstance)
			.test("xy[",								ParseException.class)
			.test("xyz[",								ParseException.class)
			.test("xyzw[",								ParseException.class)
			.test("getTestClasses()[xy].",				ParseException.class)
			.test("getTestClasses()[xyz]", -1,	IllegalStateException.class)
			.test("getTestClasses()[xyz)",				ParseException.class);
	}

	@Test
	public void testMethodArrayWithEvaluation() {
		Object testInstance = new TestClass2();
		new ErrorTestExecutor(testInstance)
			.test("getArray(size)[", ParseException.class);

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("getArray(size)[",			"index", "size")
			.test("getArray(size)[index].",	"index", "size");
	}

	private static class TestClass1
	{
		private String 		xy		= "xy";
		private int 		xyz		= 7;
		private char		xyzw	= 'W';

		private TestClass1[] getTestClasses() { return new TestClass1[0]; }
	}

	private static class TestClass2
	{
		private int index	= 1;
		private int size 	= 3;

		private Object getArray(int size) {
			return IntStream.range(0, size).mapToObj(i -> new TestClass2()).toArray(n -> new TestClass2[n]);
		}
	}
}
