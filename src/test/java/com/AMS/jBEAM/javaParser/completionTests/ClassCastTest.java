package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.utils.ClassUtils;
import org.junit.Test;

public class ClassCastTest
{
	@Test
	public void testClassCast() {
		Object testInstance = new TestClass(5, -2.0);
		String packageName = getClass().getPackage().getName();
		String subpackageName = ClassUtils.getLeafOfPath(packageName);
		String className = getClass().getSimpleName();
		String testClassName = TestClass.class.getSimpleName();
		new TestExecutor(testInstance)
			.test("get((" + testClassName + ") o).",													"d", "i", "o")
			.test("get((" + testClassName + ") this).",												"d", "i", "o")
			.test("get(this).",																		"d", "i", "o")				// no cast required for this
			.test("(" + packageName.substring(0, packageName.length() - subpackageName.length()/2),	subpackageName)
			.test("(" + packageName + "." + className.substring(0, className.length()/2),				className);

		new ErrorTestExecutor(testInstance)
			.test("get(o).", ParseException.class);
	}

	private static class TestClass
	{
		private final int i;
		private final double d;
		private final Object o;

		TestClass(int i, double d) {
			this.i = i;
			this.d = d;
			o = this;
		}

		TestClass get(TestClass o) { return o; }
	}
}
