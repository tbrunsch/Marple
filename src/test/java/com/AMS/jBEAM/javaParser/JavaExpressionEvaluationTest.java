package com.AMS.jBEAM.javaParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JavaExpressionEvaluationTest
{
	@Test
	public void testField() {
		class TestClass
		{
			int i = 3;
			double d = 2.4;
			String s = "xyz";
			Object l = 1L;
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("i", 3)
			.test("d", 2.4)
			.test("s", "xyz")
			.test("l", (long) 1);

		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("")
			.test("xyz")
			.test("d,");
	}

	@Test
	public void testFieldDotField() {
		class TestClass1
		{
			int i = 2;
			float f = 1.3f;
		}

		class TestClass2
		{
			TestClass1 tc = new TestClass1();
		}

		Object testInstance = new TestClass2();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("tc.i", 2)
			.test("tc.f", 1.3f);
	}

	@Test
	public void testFieldDotFieldWithDuckTyping() {
		class TestClass1
		{
			int i = 2;
			float f = 1.3f;
		}

		class TestClass2
		{
			Object tc = new TestClass1();
		}

		Object testInstance = new TestClass2();
		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("tc.i")
			.test("tc.f");

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("tc.i", 2)
			.test("tc.f", 1.3f);
	}

	@Test
	public void testFieldArray() {
		class TestClass
		{
			int i0 = 0;
			int i1 = 1;
			int i2 = 2;
			int i3 = 3;

			int[] array = { 7, 3, 4, 1 };
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("array[i0]", 7)
			.test("array[i1]", 3)
			.test("array[i2]", 4)
			.test("array[i3]", 1);
	}

	@Test
	public void testFieldArrayWithDuckTyping() {
		class TestClass
		{
			int i0 = 0;
			int i1 = 1;

			Object array = new int[]{ 7, 3, 4, 1 };
		}

		Object testInstance = new TestClass();
		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("array[i0]")
			.test("array[i1]");

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("array[i0]", 7)
			.test("array[i1]", 3);
	}

	@Test
	public void testMethod() {
		class TestClass
		{
			int getInt() { return 3; }
			double getDouble() { return 2.7; }
			String getString() { return "xyz"; }
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getInt()", 3)
			.test("getDouble()", 2.7)
			.test("getString()", "xyz");
	}

	@Test
	public void testMethodArguments() {
		class TestClass {
			int i = 3;
			double d = 2.5;

			double add(int a, double b) { return a + b; }
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("add(i,d)", 5.5);

		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("add(d,i)");
	}

	@Test
	public void testMethodArgumentsDuckTyping() {
		class TestClass {
			int i = 3;
			double d = 2.5;

			Object add(int a, double b) { return a + b; }
		}

		Object testInstance = new TestClass();
		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("add(i,add(i,d))");

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("add(i,add(i,d))", 8.5);
	}

	@Test
	public void testMethodDotFieldOrMethod() {
		class TestClass {
			int i = 7;
			double d = 1.2;

			TestClass getTestClass() { return new TestClass(); }
			String getString() { return "xyz"; }
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getTestClass().i", 7)
			.test("getTestClass().d", 1.2)
			.test("getTestClass().getString()", "xyz");
	}

	@Test
	public void testMethodDotFieldOrMethodDuckTyping() {
		class TestClass {
			int i = 7;
			double d = 1.2;

			Object getTestClass() { return new TestClass(); }
			String getString() { return "xyz"; }
		}

		Object testInstance = new TestClass();
		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getTestClass().i")
			.test("getTestClass().d")
			.test("getTestClass().getString()");

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("getTestClass().i", 7)
			.test("getTestClass().d", 1.2)
			.test("getTestClass().getString()", "xyz");
	}

	@Test
	public void testMethodArray() {
		class TestClass
		{
			private final int i0;
			private final int i1;

			TestClass(int i0, int i1) {
				this.i0 = i0;
				this.i1 = i1;
			}

			TestClass[] getTestClasses() {
				return new TestClass[] { new TestClass(13, 7), new TestClass(4, 9) };
			}
		}

		Object testInstance = new TestClass(0, 1);
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getTestClasses()[i0].i0", 13)
			.test("getTestClasses()[i0].i1", 7)
			.test("getTestClasses()[i1].i0", 4)
			.test("getTestClasses()[i1].i1", 9);
	}

	@Test
	public void testMethodArrayWithDuckTyping() {
		class TestClass
		{
			private final Object i0;
			private final int i1;

			Object getI1() {
				return i1;
			}

			TestClass(int i0, int i1) {
				this.i0 = i0;
				this.i1 = i1;
			}

			TestClass[] getTestClasses() {
				return new TestClass[] { new TestClass(13, 7), new TestClass(4, 9) };
			}
		}

		Object testInstance = new TestClass(0, 1);
		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getTestClasses()[i0].i0")
			.test("getTestClasses()[i0].getI1()")
			.test("getTestClasses()[getI1()].i0")
			.test("getTestClasses()[getI1()].getI1()");

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("getTestClasses()[i0].i0", 13)
			.test("getTestClasses()[i0].getI1()", 7)
			.test("getTestClasses()[getI1()].i0", 4)
			.test("getTestClasses()[getI1()].getI1()", 9);
	}

	@Test
	public void testMethodOverload() {
		class TestClass1
		{
			int i = 3;
		}

		class TestClass2
		{
			double d = 2.7;
		}

		class TestClass3
		{
			int myInt = 3;
			String myString = "xyz";

			TestClass1 getTestClass(int i) { return new TestClass1(); }
			TestClass2 getTestClass(String s) { return new TestClass2(); }
		}

		Object testInstance = new TestClass3();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getTestClass(myInt).i", 3)
			.test("getTestClass(myString).d", 2.7);

		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getTestClass(myInt).d")
			.test("getTestClass(myString).i");
	}

	@Test
	public void testMethodOverloadDuckTyping() {
		class TestClass1
		{
			int myInt = 7;
		}

		class TestClass2
		{
			String myString = "abc";
		}

		class TestClass3
		{
			int i = 0;
			int j = 1;

			Object getTestClass(int i) { return i == 0 ? new TestClass1() : new TestClass2(); }

			TestClass1 getTestClass(TestClass1 testClass) { return testClass; }
			TestClass2 getTestClass(TestClass2 testClass) { return testClass; }
		}

		Object testInstance = new TestClass3();
		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getTestClass(getTestClass(i)).myInt")
			.test("getTestClass(getTestClass(j)).myString");

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("getTestClass(getTestClass(i)).myInt", 7)
			.test("getTestClass(getTestClass(j)).myString", "abc");

		new ErrorTestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("getTestClass(getTestClass(i)).myString")
			.test("getTestClass(getTestClass(j)).myInt");
	}

	@Test
	public void testStringLiteral() {
		class TestClass
		{
			String getString(String s) { return s; }
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("\"xyz\"", "xyz")
			.test("getString(\"xyz\")", "xyz")
			.test("getString(\"\\\"\")", "\"")
			.test("getString(\"\\n\")", "\n")
			.test("getString(\"\\r\")", "\r")
			.test("getString(\"\\t\")", "\t");

		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getString(xyz)")
			.test("getString(\"xyz")
			.test("getString(\"xyz)")
			.test("getString(xyz\")")
			.test("getString(\"\\\")");
	}

	@Test
	public void testCharacterLiteral() {
		class TestClass
		{
			char getChar(char c) { return c; }
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("'x'", 'x')
			.test("getChar('x')", 'x')
			.test("getChar('\\'')", '\'')
			.test("getChar('\"')", '"')
			.test("getChar('\\\"')", '\"')
			.test("getChar('\\n')", '\n')
			.test("getChar('\\r')", '\r')
			.test("getChar('\\t')", '\t');

		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getChar(x)")
			.test("getChar('x")
			.test("getChar('x)")
			.test("getChar(x')")
			.test("getChar('\')");
	}

	@Test
	public void testBooleanLiteral() {
		class TestClass
		{
			boolean getBoolean(boolean b) { return b; }
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("true", true)
			.test("false", false)
			.test("getBoolean(true)", true)
			.test("getBoolean(false)", false)
			.test("true.equals(true)", true)		// unintended, but acceptable
			.test("true.equals(false)", false)	// extension of Java syntax
			.test("false.equals(true)", false)	// because these literals are
			.test("false.equals(false)", true);	// treated as objects

		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getBoolean(tru)")
			.test("getBoolean(fals)")
			.test("getBoolean(TRUE)")
			.test("getBoolean(FALSE)");
	}

	@Test
	public void testNullLiteral() {
		class TestClass
		{
			Object getObject(Object o) { return o; }
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("null", null)
			.test("getObject(null)", null);

		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("nul")
			.test("getObject(nul)")
			.test("getObject(null");
	}

	@Test
	public void testIntegerLiteral() {
		class TestClass
		{
			byte getByte(byte b) { return b; }
			short getShort(short s) { return s; }
			int getInt(int i) { return i; }
			long getLong(long l) { return l; }
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("120", (byte) 120)
			.test("getByte(120)", (byte) 120)
			.test("1234", (short) 1234)
			.test("getShort(1234)", (short) 1234)
			.test("100000", 100000)
			.test("getInt(100000)", 100000)
			.test("5000000000L", 5000000000L)
			.test("getLong(5000000000l)", 5000000000l);

		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("getByte(1234)")
			.test("getShort(100000)")
			.test("getInt(5000000000)");
	}

	@Test
	public void testFloatingPointLiteral() {
		class TestClass
		{
			float getFloat(float f) { return f; }
			double getDouble(double d) { return d; }
		}

		Object testInstance = new TestClass();
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("123f", 123f)
			.test("123e0", 123e0)
			.test("-123e+07", -123e+07)
			.test("+123e-13F", +123e-13F)
			.test("-123.456E1d", -123.456E1d)
			.test("123.d", 123.d)
			.test("123.e2D", 123.e2D)
			.test("123.456f", 123.456f)
			.test("+.1e-1d", +.1e-1d)
			.test("-.2e3f", -.2e3f);
	}

	@Test
	public void testSpacesInExpressions() {
		class TestClass
		{
			private final String s;
			private final short sValue;
			private final char c;
			private final int i;
			private final float f;
			private final long l;
			private final boolean b;
			private final double d;

			TestClass(String s, short sValue, char c, int i, float f, long l, boolean b, double d) {
				this.s = s;
				this.sValue = sValue;
				this.c = c;
				this.i = i;
				this.f = f;
				this.l = l;
				this.b = b;
				this.d = d;
			}

			TestClass getTestClass(String s, short sValue, char c, int i, float f, long l, boolean b, double d) {
				return new TestClass(s + "_xyz", sValue, c, i + 1, f / 2.f, l * 3, !b, 3 - d);
			}
		}

		Object testInstance = new TestClass("abc", (short) 13, 'X', 123456789, -13e02f, 1L, false, 2.34e-56);
		new TestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test(" s ", "abc")
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ) . sValue ", (short) 13)
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ) .s", "abc" + "_xyz")
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ). c", 'X')
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ).i ", 123456789 + 1)
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d) .f", -13e02f / 2.f)
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d ). l", 1L * 3)
			.test("  getTestClass (s,sValue,  c,i  ,  f,l,b,d ).b", !false)
			.test("  getTestClass( s, sValue, c, i, f, l, b, d  ) . d", 3 - 2.34e-56);
	}

	/*
	 * Class for creating tests with expected successful code completions
	 */
	private static class TestExecuter
	{
		private final Object					testInstance;
		private final EvaluationMode 			evaluationMode;

		TestExecuter(Object testInstance, EvaluationMode evaluationMode) {
			this.testInstance = testInstance;
			this.evaluationMode = evaluationMode;
		}

		TestExecuter test(String javaExpression, Object expectedValue) {
			JavaParser parser = new JavaParser();
			try {
				Object actualValue = parser.evaluate(javaExpression, evaluationMode, testInstance);
				assertEquals("Expression: " + javaExpression, expectedValue, actualValue);
			} catch (JavaParseException e) {
				assertTrue("Exception during expression evaluation: " + e.getMessage(), false);
			}
			return this;
		}
	}

	/*
	 * Class for creating tests with expected exceptions
	 */
	static class ErrorTestExecuter
	{
		private final Object													testInstance;
		private final EvaluationMode 											evaluationMode;

		ErrorTestExecuter(Object testInstance, EvaluationMode evaluationMode) {
			this.testInstance = testInstance;
			this.evaluationMode = evaluationMode;
		}

		ErrorTestExecuter test(String javaExpression) {
			Class<? extends Exception> expectedExceptionClass = JavaParseException.class;
			JavaParser parser = new JavaParser();
			try {
				parser.evaluate(javaExpression, evaluationMode, testInstance);
				assertTrue("Expression: " + javaExpression + " - Expected an exception", false);
			} catch (JavaParseException | IllegalStateException e) {
				assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
			}
			return this;
		}
	}
}
