package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.debug.ParserLogEntry;
import com.AMS.jBEAM.javaParser.debug.ParserLoggerIF;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExpressionEvaluationTest
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
		new TestExecutor(testInstance)
			.test("i", 3)
			.test("d", 2.4)
			.test("s", "xyz")
			.test("l", (long) 1);

		new ErrorTestExecutor(testInstance)
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
		new TestExecutor(testInstance)
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
		new ErrorTestExecutor(testInstance)
			.test("tc.i")
			.test("tc.f");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
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
		new TestExecutor(testInstance)
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
		new ErrorTestExecutor(testInstance)
			.test("array[i0]")
			.test("array[i1]");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
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
		new TestExecutor(testInstance)
			.test("getInt()",		3)
			.test("getDouble()",	2.7)
			.test("getString()",	"xyz");
	}

	@Test
	public void testMethodArguments() {
		class TestClass {
			int i = 3;
			double d = 2.5;

			double add(int a, double b) { return a + b; }
		}

		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("add(i,d)", 5.5);

		new ErrorTestExecutor(testInstance)
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
		new ErrorTestExecutor(testInstance)
			.test("add(i,add(i,d))");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
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
		new TestExecutor(testInstance)
			.test("getTestClass().i",				7)
			.test("getTestClass().d",				1.2)
			.test("getTestClass().getString()",	"xyz");
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
		new ErrorTestExecutor(testInstance)
			.test("getTestClass().i")
			.test("getTestClass().d")
			.test("getTestClass().getString()");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("getTestClass().i",				7)
			.test("getTestClass().d",				1.2)
			.test("getTestClass().getString()",	"xyz");
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
		new TestExecutor(testInstance)
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
		new ErrorTestExecutor(testInstance)
			.test("getTestClasses()[i0].i0")
			.test("getTestClasses()[i0].getI1()")
			.test("getTestClasses()[getI1()].i0")
			.test("getTestClasses()[getI1()].getI1()");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("getTestClasses()[i0].i0",			13)
			.test("getTestClasses()[i0].getI1()",		7)
			.test("getTestClasses()[getI1()].i0",		4)
			.test("getTestClasses()[getI1()].getI1()",	9);
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
		new TestExecutor(testInstance)
			.test("getTestClass(myInt).i",		3)
			.test("getTestClass(myString).d",	2.7);

		new ErrorTestExecutor(testInstance)
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
		new ErrorTestExecutor(testInstance)
			.test("getTestClass(getTestClass(i)).myInt")
			.test("getTestClass(getTestClass(j)).myString");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("getTestClass(getTestClass(i)).myInt",		7)
			.test("getTestClass(getTestClass(j)).myString",	"abc");

		new ErrorTestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
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
		new TestExecutor(testInstance)
			.test("\"xyz\"", "xyz")
			.test("getString(\"xyz\")",	"xyz")
			.test("getString(\"\\\"\")",	"\"")
			.test("getString(\"\\n\")",	"\n")
			.test("getString(\"\\r\")",	"\r")
			.test("getString(\"\\t\")",	"\t");

		new ErrorTestExecutor(testInstance)
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
		new TestExecutor(testInstance)
			.test("'x'", 'x')
			.test("getChar('x')",		'x')
			.test("getChar('\\'')",	'\'')
			.test("getChar('\"')",		'"')
			.test("getChar('\\\"')",	'\"')
			.test("getChar('\\n')",	'\n')
			.test("getChar('\\r')",	'\r')
			.test("getChar('\\t')",	'\t');

		new ErrorTestExecutor(testInstance)
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
		new TestExecutor(testInstance)
			.test("true",				true)
			.test("false",				false)
			.test("getBoolean(true)",	true)
			.test("getBoolean(false)",	false);

		new ErrorTestExecutor(testInstance)
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
		new TestExecutor(testInstance)
			.test("null",				null)
			.test("getObject(null)",	null);

		new ErrorTestExecutor(testInstance)
			.test("nul")
			.test("getObject(nul)")
			.test("getObject(null");
	}

	@Test
	public void testThisLiteral() {
		class TestClass
		{
			int value;

			TestClass(int value) { this.value = value; }
			int getValue(TestClass testInstance) { return testInstance.value; }
		}

		Object testInstance = new TestClass(23);
		new TestExecutor(testInstance)
			.test("this.value",		23)
			.test("getValue(this)",	23);
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

		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("120",						120)
			.test("getByte((byte) 120)",		testInstance.getByte((byte) 120))
			.test("1234",						1234)
			.test("getShort((short) 1234)",	testInstance.getShort((short) 1234))
			.test("100000",					100000)
			.test("getInt(100000)",			testInstance.getInt(100000))
			.test("5000000000L",				5000000000L)
			.test("getLong(5000000000l)",		testInstance.getLong(5000000000l));

		new ErrorTestExecutor(testInstance)
			.test("getByte(123)")
			.test("getShort(1000)")
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
		new TestExecutor(testInstance)
			.test("123f",						123f)
			.test("getFloat(123f)",			123f)
			.test("123e0",						123e0)
			.test("getDouble(123e0)",			123e0)
			.test("-123e+07",					-123e+07)
			.test("getDouble(-123e+07)",		-123e+07)
			.test("+123e-13F",					+123e-13F)
			.test("getFloat(+123e-13F)",		+123e-13F)
			.test("-123.456E1d",				-123.456E1d)
			.test("getDouble(-123.456E1d)",	-123.456E1d)
			.test("123.d",						123.d)
			.test("getDouble(123.d)",			123.d)
			.test("123.e2D",					123.e2D)
			.test("getDouble(123.e2D)",		123.e2D)
			.test("123.456f",					123.456f)
			.test("getFloat(123.456f)",		123.456f)
			.test("+.1e-1d",					+.1e-1d)
			.test("getDouble(+.1e-1d)",		+.1e-1d)
			.test("-.2e3f",					-.2e3f)
			.test("getFloat(-.2e3f)",			-.2e3f);
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
		new TestExecutor(testInstance)
			.test(" s ", "abc")
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ) . sValue ",	(short) 13)
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ) .s",			"abc" + "_xyz")
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ). c",			'X')
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ).i ",			123456789 + 1)
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d) .f",				-13e02f / 2.f)
			.test("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d ). l",			1L * 3)
			.test("  getTestClass (s,sValue,  c,i  ,  f,l,b,d ).b",					!false)
			.test("  getTestClass( s, sValue, c, i, f, l, b, d  ) . d",				3 - 2.34e-56);
	}

	@Test
	public void testAmbiguity() {
		class TestClass
		{
			char c = 'A';
			byte b = 123;
			short s = (short) 1234;
			int i = 123456789;
			long l = 5000000000L;
			Object o1 = new Double(1.23);
			Object o2 = new Float(2.34f);

			char get(char c) { return c; }
			byte get(byte b) { return b; }
			int get(int l) { return i; }
			double get(double d) { return d; }
			Object get(Object o) { return o; }
			Object get(Float f) { return f + 1.0f; }
		}

		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("get(c)",	testInstance.get(testInstance.c))
			.test("get(b)",	testInstance.get(testInstance.b))
			.test("get(i)",	testInstance.get(testInstance.i))
			.test("get(l)",	testInstance.get(testInstance.l))
			.test("get(o1)",	testInstance.get(testInstance.o1))
			.test("get(o2)",	testInstance.get(testInstance.o2));

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("get(o1)", testInstance.get(testInstance.o1))
			.test("get(o2)", testInstance.get((Float) testInstance.o2));

		new ErrorTestExecutor(testInstance)
			.test("get(s)");
	}

	@Test
	public void testMethodOverloadSideEffects() {
		/*
		 * It is important that expression are not evaluated multiple times
		 * when searching for the right method overload. Otherwise, side effects
		 * (which is critical anyway) may also occur multiple times.
		 */

		class TestClass
		{
			int count = 0;

			int f(int i, float f) { return i; };
			int f(int i, String s) { return i; };

			int getInt() { return ++count; }
		}

		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("f(getInt(), 1.0f)",		1)
			.test("f(getInt(), \"Test\")",	2)
			.test("f(getInt(), \"Test\")",	3)
			.test("f(getInt(), 1.0f)",		4);
	}

	@Test
	public void testMethodParseErrorAndSideEffect() {
		class TestClass
		{
			int sideEffectCounter = 0;

			double f(int i, String s) {
				return 1.0;
			}
			int g() { return sideEffectCounter++; }
		}

		TestClass testInstance = new TestClass();
		new ErrorTestExecutor(testInstance)
			.test("f(g(), s)");

		assertEquals("Triggered side effect despite parse error", testInstance.sideEffectCounter, 0);
	}

	@Test
	public void testVarArgs() {
		class TestClass
		{
			int i1 = 3;
			int i2 = -5;
			int i3 = 11;

			int[] i123 = { 1, 2, 4 };

			double d = 3.5;

			double sum(double offset, int... ints) {
				return offset + Arrays.stream(ints).sum();
			}
		}

		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("sum(d)",				3.5)
			.test("sum(d, i1)",			6.5)
			.test("sum(d, i1, i2)",		1.5)
			.test("sum(d, i1, i2, i3)",	12.5)
			.test("sum(d, i123)",			10.5)
			.test("sum(i1, i123)",			10.0);

		new ErrorTestExecutor(testInstance)
			.test("sum()")
			.test("sum(i1, d)")
			.test("sum(d, i123, i1)")
			.test("sum(d, i1, i123)")
			.test("sum(d, i123, i123)");
	}

	@Test
	public void testParenthesizedExpression() {
		class TestClass
		{
			int y = 1;
			double x = 2.0;

			void goDoNothing() {}
			Float getFloat(int i) { return i + 0.5f; }
		}

		final String getClass = "getClass()";
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("(getFloat(y).toString())",			"1.5")
			.test("(getFloat(y)).toString()",			"1.5")
			.test("(getFloat(y).toString()).length()",	3)
			.test("((x))",								2.0)
			.test("(((1.3e-7)))",						1.3e-7);
	}

	@Test
	public void testClassCast() {
		class TestClass
		{
			final int i;
			final double d;
			final Object o1;
			final Object o2;

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

		Object testInstance = new TestClass(5, -2.0, "abc");
		new TestExecutor(testInstance)
			.test("merge((TestClass) o1).i",			18)
			.test("merge((TestClass) o1).d",			-4.5)
			.test("((TestClass) o1).merge(this).i",	18)
			.test("((TestClass) o1).merge(this).d",	4.5)
			.test("getId(o1)",							1)
			.test("getId((TestClass) o1)",				3)
			.test("getId(o2)",							1)
			.test("getId((java.lang.String) o2)",		2)
			.test("getId((String) o2)",				2)
			.test("(int) i",							5)
			.test("(double) d",						-2.0)
			.test("(int) d",							-2)
			.test("(int) 2.3",							2);

		new ErrorTestExecutor(testInstance)
			.test("(TestClass) o2")
			.test("(String) o1");
	}

	private static class ClassParserTestClass
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

	@Test
	public void testClass() {
		Object testInstance = null;
		new TestExecutor(testInstance)
			.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE)
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.l",				-17L)
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.f",				27.5f)
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.getInt()",		0)
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.getDouble()",	2.0);

		new TestExecutor(testInstance)
			.minimumAccessLevel(AccessLevel.PUBLIC)
			.test("java.lang.Math.pow(1.5, 2.5)", Math.pow(1.5, 2.5))
			.test("java.lang.Math.PI", Math.PI);

		new ErrorTestExecutor(testInstance)
			.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE)
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.i")
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.b")
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.d")
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.getLong()")
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.getString()")
			.test("com.AMS.jBEAM.javaParser.ExpressionEvaluationTest.ClassParserTestClass.getFloat()");
	}

	private static class ConstructorParserTestClass
	{
		final int i;
		final long l;
		final float f;
		final double d;

		ConstructorParserTestClass(int i, float f) {
			this.i = i;
			this.l = 1L;
			this.f = f;
			this.d = 2.0;
		}

		ConstructorParserTestClass(double d, long l) {
			this.i = 3;
			this.l = l;
			this.f = 4.0f;
			this.d = d;
		}

		ConstructorParserTestClass(int i, long l, float f, double d) {
			this.i = i;
			this.l = l;
			this.f = f;
			this.d = d;
		}
	}

	@Test
	public void testConstructor() {
		Object testInstance = new ConstructorParserTestClass(0, 0.0f);
		new TestExecutor(testInstance)
			.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE)
			.test("new ConstructorParserTestClass(5, 6.0f).i",						5)
			.test("new ConstructorParserTestClass(5, 6.0f).l",						1L)
			.test("new ConstructorParserTestClass(5, 6.0f).f",						6.0f)
			.test("new ConstructorParserTestClass(5, 6.0f).d",						2.0)
			.test("new ConstructorParserTestClass(7.0, 8L).i",						3)
			.test("new ConstructorParserTestClass(7.0, 8L).l",						8L)
			.test("new ConstructorParserTestClass(7.0, 8L).f",						4.0f)
			.test("new ConstructorParserTestClass(7.0, 8L).d",						7.0)
			.test("new ConstructorParserTestClass(9, 10L, 11.f, 12.0).i",			9)
			.test("new ConstructorParserTestClass(9, 10L, 11.f, 12.0).l",			10L)
			.test("new ConstructorParserTestClass(9, 10L, 11.f, 12.0).f",			11.f)
			.test("new ConstructorParserTestClass(9, 10L, 11.f, 12.0).d",			12.0)
			.test("new ConstructorParserTestClass(0, 0, 0, 0).i",					0)
			.test("new ConstructorParserTestClass(0, 0, 0, 0).l",					0L)
			.test("new ConstructorParserTestClass(0, 0, 0, 0).f",					0.f)
			.test("new ConstructorParserTestClass(0, 0, 0, 0).d",					0.0)
			.test("new StringBuilder(\"Test\").append('X').append(13).toString()",	"TestX13");

		new ErrorTestExecutor(testInstance)
			.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE)
			.test("new ConstructorParserTestClass(0)")
			.test("new ConstructorParserTestClass(0, 0)")
			.test("new ConstructorParserTestClass(0, 0, 0)")
			.test("new ConstructorParserTestClass(0, 0, 0, 0, 0)");
	}

	@Test
	public void testBinaryOperator() {
		new TestExecutor(null)
			.test("5 *7- 8 / 3*2 + 4 * 2",	5*7 - 8/3*2 + 4*2)
			.test("5 + 7 * 8",				5 + 7 * 8)
			.test("(5 + 7) * 8",			(5 + 7) * 8)
			.test(" 5%3 -7 / 2.0",			5 % 3 - 7/2.0)
			.test("5 + 4 + \"Test\"",		5 + 4 + "Test")
			.test("-27 >> 2 << 2",			-27 >> 2 << 2)
			.test("-23456 >>> 3 << 1",		-23456 >>> 3 << 1)
			.test("(byte) 23 << 2",		(byte) 23 << 2)
			.test("9*3 < 4*7",				9*3 < 4*7)
			.test("9*4 < 3.0*12",			9*4 < 3.0*12)
			.test("9*3 <= 4*7",			9*3 <= 4*7)
			.test("9*4 <= 3.0*12",			9*4 <= 3.0*12)
			.test("5*5 <= 4*6",			5*5 <= 4*6)
			.test("4*7 > 9*3",				4*7 > 9*3)
			.test("3.0*12 > 9*4",			3.0*12 > 9*4)
			.test("4*7 >= 9*3",			4*7 >= 9*3)
			.test("3.0*12 >= 9*4",			3.0*12 >= 9*4)
			.test("4*6 >= 5*5",			4*6 >= 5*5)
			.test("9*3 == 4*7",			9*3 == 4*7)
			.test("9*4 == 3.0*12",			9*4 == 3.0*12)
			.test("5*5 == 4*6",			5*5 == 4*6)
			.test("9*3 != 4*7",			9*3 != 4*7)
			.test("9*4 != 3.0*12",			9*4 != 3.0*12)
			.test("5*5 != 4*6",			5*5 != 4*6)
			.test("123 & 234",				123 & 234)
			.test("123 ^ 234",				123 ^ 234)
			.test("123 | 234",				123 | 234)
			.test("false && false",		false && false)
			.test("false && true",			false && true)
			.test("true && false",			true && false)
			.test("true && true",			true && true)
			.test("false || false",		false || false)
			.test("false || true",			false || true)
			.test("true || false",			true || false)
			.test("true || true",			true || true);
	}

	@Test
	public void testBinaryOperatorShortCircuitEvaluation() {
		class TestClass
		{
			int counter 			= 0;
			TestClass npeTrigger	= null;

			TestClass reset() {
				counter = 0;
				return this;
			}

			boolean FALSE() {
				counter++;
				return false;
			}

			boolean TRUE() {
				counter++;
				return true;
			}

			int getCounter(boolean dummy) {
				return counter;
			}
		}

		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("reset().getCounter(FALSE())",					1)
			.test("reset().getCounter(FALSE() && FALSE())",		1)
			.test("reset().getCounter(FALSE() && TRUE())",			1)
			.test("reset().getCounter(TRUE() && FALSE())",			2)
			.test("reset().getCounter(TRUE() && TRUE())",			2)
			.test("reset().getCounter(FALSE() || FALSE())",		2)
			.test("reset().getCounter(FALSE() || TRUE())",			2)
			.test("reset().getCounter(TRUE() || FALSE())",			1)
			.test("reset().getCounter(TRUE() || TRUE())",			1)
			.test("npeTrigger != null && npeTrigger.counter > 0",	false);

		new ErrorTestExecutor(testInstance)
			.test("reset().getCounter(FALSE() && 5")
			.test("reset().getCounter(TRUE() || 'X'");
	}

	@Test
	public void testAssignment() {
		class TestClass
		{
			double 	d = 3.0;
			float 	f = 2.f;
			int 	i = 5;

			TestClass reset() {
				d = 3.0;
				f = 2.f;
				i = 5;
				return this;
			}

			TestClass get(double dummy) {
				return this;
			}
		}

		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("reset().get(d = 7.0).d",		7.0)
			.test("reset().get(f = -1).f",			-1.f)
			.test("reset().get(i = 13).i",			13)
			.test("reset().get(d = f = i = -3).d",	-3.0)
			.test("reset().get(d = f = i = -3).f",	-3.f)
			.test("reset().get(d = f = i = -3).i",	-3);
	}

	@Test
	public void testUnaryOperator() {
		class TestClass
		{
			byte b	= 13;
			int i	= -21;
			float f = 2.5f;
			final String s = "Test";
			final int j = 123;

			TestClass reset() {
				b = 13;
				i = -21;
				f = 2.5f;
				return this;
			}

			TestClass get(int dummy) { return this; }
		}

		new TestExecutor(new TestClass())
			.test("++reset().b",			(byte) 14)
			.test("reset().get(++b).b",	(byte) 14)
			.test("++reset().i",			-20)
			.test("reset().get(++i).i",	-20)
			.test("--reset().b",			(byte) 12)
			.test("reset().get(--b).b",	(byte) 12)
			.test("--reset().i",			-22)
			.test("reset().get(--i).i",	-22)
			.test("+reset().b",			13)
			.test("+reset().i",			-21)
			.test("+reset().f",			2.5f)
			.test("-reset().b",			-13)
			.test("-reset().i",			21)
			.test("-reset().f",			-2.5f)
			.test("!false",				true)
			.test("!true",					false)
			.test("!(false || true)",		false)
			.test("!(true && false)",		true)
			.test("~12345", ~12345);

		new ErrorTestExecutor(new TestClass())
			.test("++f")
			.test("++j")
			.test("++s")
			.test("--f")
			.test("--j")
			.test("--s")
			.test("+s")
			.test("-s")
			.test("!1")
			.test("!null")
			.test("~f");
	}

	@Test
	public void testVariable() {
		class TestClass
		{
			byte b = 3;
			int i = -1000;
			float f = 2.5f;

			String test(Object o) { return o.toString(); }
		}

		Variable variable1 = new Variable("xyz", 15.0, true);
		Variable variable2 = new Variable("abc", "Test", true);

		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.addVariable(variable1)
			.addVariable(variable2)
			.test("b + xyz",		18.0)
			.test("xyz * i",		-15000.0)
			.test("(int) xyz / f",	6.0f)
			.test("b + abc",		"3Test")
			.test("abc + i",		"Test-1000")
			.test("abc + f",		"Test2.5")
			.test("xyz + xyz",		30.0)
			.test("abc + abc",		"TestTest")
			.test("test(xyz)",		"15.0")
			.test("test(abc)",		"Test");
	}

	/*
	 * Base class for all TestExecutors
	 */
	private static class AbstractTestExecutor<T extends AbstractTestExecutor>
	{
		final Object			testInstance;

		ParserSettings 			settings;
		ParserSettingsBuilder	settingsBuilder	= new ParserSettingsBuilder()
													.minimumAccessLevel(AccessLevel.PRIVATE);

		AbstractTestExecutor(Object testInstance) {
			this.testInstance = testInstance;
		}

		T evaluationMode(EvaluationMode evaluationMode) {
			verifyBeforeTest();
			settingsBuilder.evaluationModeCodeEvaluation(evaluationMode);
			return (T) this;
		}

		T addVariable(Variable variable) {
			verifyBeforeTest();
			settingsBuilder.addVariable(variable);
			return (T) this;
		}

		T minimumAccessLevel(AccessLevel minimumAccessLevel) {
			verifyBeforeTest();
			settingsBuilder.minimumAccessLevel(minimumAccessLevel);
			return (T) this;
		}

		 T logger(ParserLoggerIF logger) {
			verifyBeforeTest();
			settingsBuilder.logger(logger);
			return (T) this;
		 }

		private void verifyBeforeTest() {
			if (settings != null) {
				throw new IllegalStateException("Settings cannot be changed between tests");
			}
		}

		void ensureValidSettings() {
			if (settings == null) {
				settings = settingsBuilder.build();
			}
		}
	}

	/*
	 * Class for creating tests with expected successful code completions
	 */
	private static class TestExecutor extends AbstractTestExecutor<TestExecutor>
	{
		TestExecutor(Object testInstance) {
			super(testInstance);
		}

		TestExecutor test(String javaExpression, Object expectedValue) {
			ensureValidSettings();

			ParserLoggerIF logger = settings.getLogger();
			logger.log(new ParserLogEntry(LogLevel.INFO, "Test", "Testing expression '" + javaExpression + "'...\n"));

			JavaParser parser = new JavaParser();
			try {
				Object actualValue = parser.evaluate(javaExpression, settings, testInstance);
				assertEquals("Expression: " + javaExpression, expectedValue, actualValue);
			} catch (ParseException e) {
				int numLoggedEntries = logger.getNumberOfLoggedEntries();
				if (numLoggedEntries > 0) {
					System.out.println("Exception after " + numLoggedEntries + " logged entries.");
				}
				assertTrue("Exception during expression evaluation: " + e.getMessage(), false);
			}
			return this;
		}
	}

	/*
	 * Class for creating tests with expected exceptions
	 */
	private static class ErrorTestExecutor extends AbstractTestExecutor<ErrorTestExecutor>
	{
		ErrorTestExecutor(Object testInstance) {
			super(testInstance);
		}

		ErrorTestExecutor test(String javaExpression) {
			ensureValidSettings();

			Class<? extends Exception> expectedExceptionClass = ParseException.class;
			JavaParser parser = new JavaParser();
			try {
				parser.evaluate(javaExpression, settings, testInstance);
				assertTrue("Expression: " + javaExpression + " - Expected an exception", false);
			} catch (ParseException | IllegalStateException e) {
				assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
			}
			return this;
		}
	}
}
