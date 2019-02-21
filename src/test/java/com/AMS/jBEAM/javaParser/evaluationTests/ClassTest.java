package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.settings.AccessLevel;
import com.AMS.jBEAM.javaParser.utils.ClassUtils;
import org.junit.Test;

import java.util.Collections;

public class ClassTest
{
	@Test
	public void testClass() {
		String className = TestClass.class.getName().replace('$', '.');
		new TestExecutor(null)
			.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE)
			.test(className + ".l",			-17L)
			.test(className + ".f",			27.5f)
			.test(className + ".getInt()",		0)
			.test(className + ".getDouble()",	2.0);

		new TestExecutor(null)
			.minimumAccessLevel(AccessLevel.PUBLIC)
			.importPackage("java.util")
			.test("Math.pow(1.5, 2.5)", Math.pow(1.5, 2.5))
			.test("Math.PI", Math.PI)
			.test("Collections.emptyList()", Collections.emptyList());

		String packageName = ClassUtils.getParentPath(getClass().getPackage().getName()) + ".classesForTest";
		new TestExecutor(null)
			.test(packageName + ".dummies.MyClass.VALUE", 				5)
			.test(packageName + ".dummies.MyOtherClass.OTHER_VALUE", 	7.5)
			.test(packageName + ".moreDummies.MyDummy.FIRST_DUMMY", 	true);

		new TestExecutor(null)
			.importPackage(packageName)
			.test("DummyClass.FIRST_CHARACTER",	'D')
			.test("MyDummyClass.FIRST_CHARACTER",	'M');

		new TestExecutor(null)
			.importClass(packageName + ".dummies.YetAnotherDummyClass")
			.test("YetAnotherDummyClass.NAME",	"YetAnotherDummyClass");

		new TestExecutor(null)
			.importClass(packageName + ".DummyClass.InternalClassStage1")
			.test("InternalClassStage1.value",					5.0)
			.test("InternalClassStage1.InternalClassStage2.i",	3);

		new TestExecutor(null)
			.importClass(packageName + ".DummyClass.InternalClassStage1.InternalClassStage2")
			.test("InternalClassStage2.i",	3);

		new ErrorTestExecutor(null)
			.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE)
			.test(className + ".i")
			.test(className + ".b")
			.test(className + ".d")
			.test(className + ".getLong()")
			.test(className + ".getString()")
			.test(className + ".getFloat()");

		new ErrorTestExecutor(null)
			.importPackage(packageName + ".dummies")
			.test("DummyClass.FIRST_CHARACTER")
			.test("MyDummy2.FIRST_DUMMY");

		new ErrorTestExecutor(null)
			.importClass(packageName + ".dummies.MyClass")
			.test("MyOtherClass.OTHER_VALUE");
	}

	private static class TestClass
	{
		int i = 23;
		static long l = -17L;
		private static byte b = (byte) 25;
		double d = 1.3;
		static float f = 27.5f;

		static int getInt() { return 0; }
		long getLong() { return 1L; }
		private static String getString() { return "abc"; }
		static double getDouble() { return 2.0; }
		float getFloat() { return 3.0f; }
	}
}
