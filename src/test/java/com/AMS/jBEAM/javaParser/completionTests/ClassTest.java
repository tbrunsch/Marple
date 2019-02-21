package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.settings.AccessLevel;
import com.AMS.jBEAM.javaParser.utils.ClassUtils;
import org.junit.Test;

public class ClassTest
{
	@Test
	public void testPackage() {
		String packageName = getClass().getPackage().getName();
		String subPackageName = ClassUtils.getLeafOfPath(packageName);
		String truncatedPackageName = packageName.substring(0, packageName.length() - subPackageName.length()/2);

		new TestExecutor(null)
				.test("java.ut",	"util")
				.test(truncatedPackageName,		subPackageName);
	}

	@Test
	public void testClass() {
		String className = ClassUtils.getRegularClassName(TestClass.class.getName());
		new TestExecutor(null)
			.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE)
			.test(className + ".",				"f", "l", "getDouble()", "getInt()", "InnerClass")
			.test(className + ".I",			"InnerClass")
			.test(className + ".InnerClass.",	"test")
			.test("String.CASE_I",				"CASE_INSENSITIVE_ORDER")
			.test("String.val",				"valueOf()");

		String packageName = ClassUtils.getParentPath(getClass().getPackage().getName()) + ".classesForTest";
		new TestExecutor(null)
			.test(packageName + ".du",						"dummies", "DummyClass", "MyDummyClass", "moreDummies")
			.test(packageName + ".Du",						"DummyClass", "dummies", "MyDummyClass", "moreDummies")
			.test(packageName + ".m",						"moreDummies")
			.test(packageName + ".dummies.MyC",			"MyClass")
			.test(packageName + ".dummies.MyO",			"MyOtherClass")
			.test(packageName + ".dummies.Y",				"YetAnotherDummyClass")
			.test(packageName + ".moreDummies.MyDummy",	"MyDummy", "MyDummy2")
			.test(packageName + ".moreDummies.MyDummy2",	"MyDummy2", "MyDummy");

		new TestExecutor(null)
			.importPackage(packageName)
			.test("Du", "DummyClass")
			.test("My", "MyDummyClass");

		new TestExecutor(null)
			.importPackage(packageName + ".dummies")
			.test("MyC", "MyClass")
			.test("Y", "YetAnotherDummyClass");

		new TestExecutor(null)
			.importClass(packageName + ".moreDummies.MyDummy2")
			.test("MyDummy", "MyDummy2");

		new TestExecutor(null)
			.importClass(packageName + ".DummyClass.InternalClassStage1")
			.test("InternalC",									"InternalClassStage1")
			.test("InternalClassStage1.v",						"value")
			.test("InternalClassStage1.i",						"InternalClassStage2")
			.test("InternalClassStage1.InternalClassStage2.",	"i");

		new TestExecutor(null)
			.importClass(packageName + ".DummyClass.InternalClassStage1.InternalClassStage2")
			.test("InternalC",				"InternalClassStage2")
			.test("InternalClassStage2.",	"i");

		new TestExecutor(null)
			.minimumAccessLevel(AccessLevel.PUBLIC)
			.test("Ma",		"Math")
			.test("Math.p",	"pow(, )", "PI")
			.test("Math.P",	"PI", "pow(, )");
	}

	private static class TestClass
	{
		int i;
		static long l;
		private static byte b;
		double d;
		static float f;

		static int getInt() { return 0; }
		long getLong() { return 1L; }
		private static String getString() { return "abc"; }
		static double getDouble() { return 2.0; }
		float getFloat() { return 3.0f; }

		static final class InnerClass
		{
			static final int test = 13;
		}
	}
}
