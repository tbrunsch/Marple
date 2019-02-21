package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.utils.wrappers.ClassInfo;
import org.junit.Test;

public class ClassCastTest
{
	@Test
	public void testClassCast() {
		Object testInstance = new TestClass(5, -2.0, "abc");
		String className = ClassInfo.forNameUnchecked(TestClass.class.getName()).getUnqualifiedName();

		new TestExecutor(testInstance)
			.test("merge((" + className + ") o1).i",			18)
			.test("merge((" + className + ") o1).d",			-4.5)
			.test("((" + className + ") o1).merge(this).i",	18)
			.test("((" + className + ") o1).merge(this).d",	4.5)
			.test("getId(o1)",									1)
			.test("getId((" + className + ") o1)",				3)
			.test("getId(o2)",									1)
			.test("getId((java.lang.String) o2)",				2)
			.test("getId((String) o2)",						2)
			.test("(int) i",									5)
			.test("(double) d",								-2.0)
			.test("(int) d",									-2)
			.test("(int) 2.3",									2);

		new ErrorTestExecutor(testInstance)
			.test("(" + className + ") o2")
			.test("(String) o1");
	}

	private static class TestClass
	{
		private final int i;
		private final double d;
		private final Object o1;
		private final Object o2;

		TestClass() {
			i = 13;
			d = 2.5;
			o1 = this;
			o2 = "xyz";
		}

		TestClass(int i, double d, String o2) {
			this.i = i;
			this.d = d;
			o1 = new TestClass();
			this.o2 = o2;
		}

		TestClass merge(TestClass o) { return new TestClass(i + o.i, d - o.d, (String) o2); };

		int getId(Object o) { return 1; }
		int getId(String s) { return 2; }
		int getId(TestClass o) { return 3; }
	}
}
