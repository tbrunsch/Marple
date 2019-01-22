package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.result.CompletionSuggestionIF;
import com.google.common.base.Joiner;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompletionTest
{
	@Test
	public void testField() {
		abstract class BasicTestClass
		{
			private int 	xy 		= 13;
			private char 	XYZ		= 'W';
			private float	X		= 1.0f;

			private short	other	= 0;
		}

		class TestClass extends BasicTestClass
		{
			private String 	XY 		= "27";
			private long	xy_z	= 13;
			private double	x		= 2.72;
		}

		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("xy",	"xy", "XY", "xy_z", "XYZ", "x", "X")
			.test("XYZ",	"XYZ", "XY", "X", "x", "xy")
			.test("X",		"X", "x", "XY", "XYZ", "xy_z", "xy")
			.test("XY",	"XY", "xy", "XYZ", "xy_z", "X", "x")
			.test("xy_z",	"xy_z", "x", "xy", "XY", "X")
			.test("x",		"x", "X", "xy_z", "xy", "XY", "XYZ")
			.test("XYW",	"XY", "X", "x", "xy");

		new ErrorTestExecutor(testInstance)
			.test("xy", -1, IllegalStateException.class)
			.test("bla", -1, ParseException.class)
			.test("xy,", 3, ParseException.class);
	}

	@Test
	public void testFieldDotField() {
		class TestClass
		{
			private int 		xy 		= 13;
			private float		X		= 1.0f;
			private char 		XYZ		= 'W';

			private TestClass	member	= null;
		}

		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("member.",		"member", "X", "xy", "XYZ")
			.test("member.x",		"X", "xy", "XYZ", "member")
			.test("member.xy",		"xy", "XYZ", "X", "member")
			.test("member.xyz",	"XYZ", "xy", "X", "member")
			.test("member.mem",	"member", "X", "xy", "XYZ");

		new ErrorTestExecutor(testInstance)
			.test("membeR.",		-1, ParseException.class)
			.test("MEMBER.xy",		-1, ParseException.class)
			.test("member.xy.XY",	-1, ParseException.class)
			.test("member.xy",		-1, IllegalStateException.class);
	}

	@Test
	public void testFieldDotFieldWithEvaluation() {
		class BaseClass
		{
			private int x;
			private int xyz;
		}

		class DescendantClass extends BaseClass
		{
			private int	xy;
		}

		class TestClass
		{
			private BaseClass member = new DescendantClass();
		}

		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("member.",		"x", "xyz")
			.test("member.x",		"x", "xyz")
			.test("member.xy",		"xyz", "x")
			.test("member.xyz",	"xyz", "x");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("member.",		"xy", "x", "xyz")
			.test("member.x",		"x", "xy", "xyz")
			.test("member.xy",		"xy", "xyz", "x")
			.test("member.xyz",	"xyz", "xy", "x");
	}

	@Test
	public void testFieldArray() {
		class TestClass
		{
			private String		xy		= "xy";
			private int			xyz		= 7;
			private char		xyzw	= 'W';

			private TestClass[]	member	= null;
		}

		final String hashCode = "hashCode()";
		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("member[",			"xyz", hashCode, "xyzw", "member", "xy")
			.test("member[x",			"xyz", "xyzw", "xy", hashCode, "member")
			.test("member[xy",			"xy", "xyz", "xyzw", hashCode, "member")
			.test("member[xyz",		"xyz", "xyzw", "xy", hashCode, "member")
			.test("member[xyzw",		"xyzw", "xyz", "xy", hashCode, "member")
			.test("member[m",			"member", "xyz", hashCode, "xyzw", "xy")
			.test("member[xyz].",		"member", "xy", "xyz", "xyzw")
			.test("member[xyzw].x",	"xy", "xyz", "xyzw", "member");

		new ErrorTestExecutor(testInstance)
			.test("xy[",			3, ParseException.class)
			.test("xyz[",			4, ParseException.class)
			.test("xyzw[",			5, ParseException.class)
			.test("member[xy].",	11, ParseException.class)
			.test("member[xyz]",	-1, IllegalStateException.class)
			.test("member[xyz)",	11, ParseException.class);
	}

	@Test
	public void testFieldArrayWithEvaluation() {
		class ElementClass0
		{
			private double value = 1.0;
		}

		class ElementClass1
		{
			private int index = 3;
		}

		class TestClass
		{
			private int 	i0		= 0;
			private int		i1		= 1;
			private int 	i2		= 2;

			private Object 	array	= new Object[] { new ElementClass0(), new ElementClass1() };
		}

		TestClass testInstance = new TestClass();
		new ErrorTestExecutor(testInstance)
			.test("array[", 6, ParseException.class);

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("array[",		"i0", "i1", "i2")
			.test("array[i0].",	"value")
			.test("array[i1].",	"index");

		new ErrorTestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("array[i2].",				10, ParseException.class)
			.test("array[array[i1].index].",	23, ParseException.class);
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
			.evaluationMode(EvaluationMode.STRONGLY_TYPED)
			.test("f(g(), s)", 9, ParseException.class);

		assertEquals("Triggered side effect despite parse error", testInstance.sideEffectCounter, 0);
	}

	@Test
	public void testMethod() {
		/*
		 * Convention for this test: Methods whose names consist of n characters have n arguments.
		 *
		 * This allows for auto-generating the insertion text of a method:
		 *
		 * "x" -> "x()", "xy" -> "xy(, )", "xyz" -> "xyz(, , )" etc.
		 *
		 * Exception: Method "other" whose sole purpose is not to appear among the first suggestions,
		 *			which makes this test stronger.
		 */
		Function<String, String> methodFormatter = s -> s + "(" + Joiner.on(", ").join(IntStream.range(0, s.length()).mapToObj(i -> "").collect(Collectors.toList())) + ")";
		Function<String[], String[]> methodsFormatter = methods -> Arrays.stream(methods).map(methodFormatter::apply).toArray(size -> new String[size]);

		abstract class BasicTestClass
		{
			private int 	xy(char c, float f)						{ return 13; }
			private char 	XYZ(float f, int i, double d)			{ return 'W'; }
			private float	X(int i)								{ return 1.0f; }

			private short	other()									{ return 0; }
		}

		class TestClass extends BasicTestClass
		{
			private String 	XY(long l, double d)					{ return "27"; }
			private long	xy_z(double d, short s, int i, byte b)	{ return 13; }
			private double	x(double d)								{ return 2.72; }

			private byte	XYZ(char c, double d, boolean b)		{ return 1; }
		}

		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("xy",	methodsFormatter.apply(new String[]{"xy", "XY", "xy_z", "XYZ", "XYZ", "x", "X"}))
			.test("XYZ",	methodsFormatter.apply(new String[]{"XYZ", "XYZ", "XY", "X", "x", "xy"}))
			.test("X",		methodsFormatter.apply(new String[]{"X", "x", "XY", "XYZ", "XYZ", "xy_z", "xy"}))
			.test("XY",	methodsFormatter.apply(new String[]{"XY", "xy", "XYZ", "XYZ", "xy_z", "X", "x"}))
			.test("xy_z",	methodsFormatter.apply(new String[]{"xy_z", "x", "xy", "XY", "X"}))
			.test("x",		methodsFormatter.apply(new String[]{"x", "X", "xy_z", "xy", "XY", "XYZ", "XYZ"}))
			.test("XYW",	methodsFormatter.apply(new String[]{"XY", "X", "x", "xy"}));

		new ErrorTestExecutor(testInstance)
			.test("other()",	-1, IllegalStateException.class)
			.test("bla",		-1, ParseException.class)
			.test("other(),",	8, ParseException.class);
	}

	@Test
	public void testMethodArguments() {
		class TestClass
		{
			private int 	prefixI	= 1;
			private double 	prefixD	= 1.0;
			private char	prefixC	= 'A';

			private int		prefixI(double arg)	{ return 1; }
			private double	prefixD(char arg)	{ return 1.0; }
			private char	prefixC(int arg)	{ return 'A'; }
		}

		Object testInstance = new TestClass();
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
			.test("prefixD(prefixI)",	16, ParseException.class)
			.test("prefixC(prefixI,",	16, ParseException.class)
			.test("prefixI(prefixD))",	-1, ParseException.class);
	}

	@Test
	public void testMethodArgumentsWithEvaluation() {
		class TestClass
		{
			private Object getObject() { return new TestClass(); }
			private Object getTestClassObject(TestClass testClass) { return testClass.getObject(); }
		}

		Object testInstance = new TestClass();
		new ErrorTestExecutor(testInstance)
			.test("getTestClassObject(getObject()).get", 35, ParseException.class);

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("getTestClassObject(getObject()).get", "getObject()", "getTestClassObject()", "getClass()");
	}

	@Test
	public void testMethodDotFieldOrMethod() {
		class TestClass1
		{
			private int 	i	= 1;
			private double	d	= 1.0;

			private Object getObject(double d) { return null; }
		}

		class TestClass2
		{
			private short	s	= 1;
			private char	c	= 'A';

			private TestClass1 getTestClass(char c) { return null; }
		}

		Object testInstance = new TestClass2();
		new TestExecutor(testInstance)
			.test("getTestClass",									"getTestClass()")
			.test("getTestClass(c).",								"d", "i", "getObject()")
			.test("getTestClass(c).get",							"getObject()")
			.test("getTestClass(c).getObject(",					"c", "s")
			.test("getTestClass(c).getObject(getTestClass(c).d).",	"clone()", "equals()");

		new ErrorTestExecutor(testInstance)
			.test("getTestClazz().",								-1, ParseException.class)
			.test("getTestClazz().i",								-1, ParseException.class)
			.test("getTestClass().i.d",							-1, ParseException.class)
			.test("getTestClass(c).getObject(getTestClass(c).d)",	-1, IllegalStateException.class);
	}

	@Test
	public void testMethodDotFieldOrMethodWithEvaluation() {
		class BaseClass
		{
			private int x;
			private int xyz;

			public int getInt() { return 1; }
		}

		class DescendantClass extends BaseClass
		{
			private int	xy;

			public double getDouble() { return 1.0; }
		}

		class TestClass
		{
			private BaseClass getObject() { return new DescendantClass(); }
		}

		final String getClass = "getClass()";
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("getObject().",		"x", "xyz", "getInt()")
			.test("getObject().x",		"x", "xyz", "getInt()")
			.test("getObject().xy",	"xyz", "x", "getInt()")
			.test("getObject().xyz",	"xyz", "x", "getInt()")
			.test("getObject().get",	"getInt()", getClass, "x", "xyz");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("getObject().",		"xy", "x", "xyz", "getDouble()", "getInt()")
			.test("getObject().x",		"x", "xy", "xyz", "getDouble()", "getInt()")
			.test("getObject().xy",	"xy", "xyz", "x", "getDouble()", "getInt()")
			.test("getObject().xyz",	"xyz", "xy", "x", "getDouble()", "getInt()")
			.test("getObject().get",	"getDouble()", "getInt()", getClass, "xy", "x", "xyz");
	}

	@Test
	public void testMethodArray() {
		class TestClass
		{
			private String 		xy		= "xy";
			private int 		xyz		= 7;
			private char		xyzw	= 'W';

			private TestClass[] getTestClasses() { return new TestClass[0]; }
		}

		final String hashCode = "hashCode()";
		final String getClass = "getClass()";
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("getTestClasses()[",			"xyz", hashCode, "xyzw", "xy")
			.test("getTestClasses()[x",		"xyz", "xyzw", "xy", hashCode)
			.test("getTestClasses()[xy",		"xy", "xyz", "xyzw", hashCode)
			.test("getTestClasses()[xyz",		"xyz", "xyzw", "xy", hashCode)
			.test("getTestClasses()[xyzw",		"xyzw", "xyz", "xy", hashCode)
			.test("getTestClasses()[g",		"getTestClasses()", getClass, "xyz", hashCode, "xyzw", "xy")
			.test("getTestClasses()[xyz].",	"xy", "xyz", "xyzw")
			.test("getTestClasses()[xyzw].x",	"xy", "xyz", "xyzw");

		new ErrorTestExecutor(testInstance)
			.test("xy[", 3, ParseException.class)
			.test("xyz[", 4, ParseException.class)
			.test("xyzw[", 5, ParseException.class)
			.test("getTestClasses()[xy].", 21, ParseException.class)
			.test("getTestClasses()[xyz]", -1, IllegalStateException.class)
			.test("getTestClasses()[xyz)", 21, ParseException.class);
	}

	@Test
	public void testMethodArrayWithEvaluation() {
		class TestClass
		{
			private int index	= 1;
			private int size 	= 3;

			private Object getArray(int size) { return new TestClass[size]; }
		}

		Object testInstance = new TestClass();
		new ErrorTestExecutor(testInstance)
			.test("getArray(size)[", 15, ParseException.class);

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("getArray(size)[",			"index", "size")
			.test("getArray(size)[index].",	"index", "size");
	}

	@Test
	public void testMethodOverload() {
		class TestClass1
		{
			int myInt;
		}

		class TestClass2
		{
			String myString;
		}

		class TestClass3
		{
			int intValue;
			String stringValue;

			TestClass1 getTestClass(int i, String s) { return null; }
			TestClass2 getTestClass(String s, int i) { return null; }
		}

		final String hashCode = "hashCode()";
		final String toString = "toString()";
		Object testInstance = new TestClass3();
		new TestExecutor(testInstance)
			.test("getTestClass(",							"intValue", "stringValue")
			.test("getTestClass(i",						"intValue", "stringValue", hashCode)
			.test("getTestClass(s",						"stringValue", "intValue")
			.test("getTestClass(intValue,",				"stringValue", toString, "intValue")
			.test("getTestClass(stringValue,",				"intValue", hashCode, "stringValue")
			.test("getTestClass(intValue,stringValue).",	"myInt")
			.test("getTestClass(stringValue,intValue).",	"myString");
	}

	@Test
	public void testMethodOverloadWithEvaluation() {
		class TestClass1
		{
			int myInt;
		}

		class TestClass2
		{
			String myString;
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
			.test("getTestClass(getTestClass(i)).", 30, ParseException.class);

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("getTestClass(getTestClass(i)).", "myInt")
			.test("getTestClass(getTestClass(j)).", "myString");
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
			.test("(",								"x", "y", "getFloat()", "goDoNothing()")
			.test("(g",							"getFloat()", "goDoNothing()", getClass)
			.test("(getFloat(y).toString()).le",	"length()");
	}

	@Test
	public void testClassCast() {
		class TestClass
		{
			final int i;
			final double d;
			final Object o;

			TestClass(int i, double d) {
				this.i = i;
				this.d = d;
				o = this;
			}

			TestClass get(TestClass o) { return o; }
		}

		Object testInstance = new TestClass(5, -2.0);
		new TestExecutor(testInstance)
			.test("get((TestClass) o).",				"d", "i", "o")
			.test("get((TestClass) this).",			"d", "i", "o")
			.test("get(this).",						"d", "i", "o")				// no cast required for this
			.test("(com.AMS.jBEAM.javaPars",			"javaParser")
			.test("(com.AMS.jBEAM.javaParser.JavaCo",	"CompletionTest");

		new ErrorTestExecutor(testInstance)
			.test("get(o).", 7, ParseException.class);
	}

	private static class ClassParserTestClass
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
	}

	@Test
	public void testClass() {
		Object testInstance = null;
		new TestExecutor(testInstance)
			.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE)
			.test("com.AMS.jBEAM.javaParser.CompletionTest.ClassParserTestClass.",	"f", "l", "getDouble()", "getInt()")
			.test("String.CASE_I",													"CASE_INSENSITIVE_ORDER")
			.test("String.val",													"valueOf()");

		new TestExecutor(testInstance)
			.minimumAccessLevel(AccessLevel.PUBLIC)
			.test("java.lang.Ma",		"Math")
			.test("java.lang.Math.p",	"pow(, )", "PI")
			.test("java.lang.Math.P",	"PI", "pow(, )");
	}

	private static class ConstructorParserTestClass
	{
		final int i;
		final double d;
		final Object o;

		ConstructorParserTestClass(int i, float f) {
			this.i = i;
			this.d = f;
			o = this;
		}

		ConstructorParserTestClass(String s, double d, int i) {
			this.i = i;
			this.d = d;
			this.o = s;
		}

		ConstructorParserTestClass(Object o, int i) {
			this.i = i;
			this.d = 0.0;
			this.o = o;
		}
	}

	@Test
	public void testConstructor() {
		Object testInstance = new ConstructorParserTestClass(5, -2.0f);
		new TestExecutor(testInstance)
			.test("new ConstructorParserTestC",						"ConstructorParserTestClass")
			.test("new ConstructorParserTestClass(",					"i", "o")
			.test("new ConstructorParserTestClass(i, ",				"i")
			.test("new ConstructorParserTestClass(o, ",				"i")
			.test("new ConstructorParserTestClass(\"bla\", ",			"d", "i")
			.test("new ConstructorParserTestClass(\"bla\", i, ",		"i")
			.test("new ConstructorParserTestClass(\"bla\", d, i).",	"d", "i", "o")
			.test("new com.AMS.jBEAM.javaPars",						"javaParser")
			.test("new com.AMS.jBEAM.javaParser.JavaCo",				"CompletionTest");
	}

	@Test
	public void testVariables() {
		class TestClass
		{
			int xy;
			byte xyzw;
			float x;

			void test(byte b) {}
		}

		Variable variable1 = new Variable("xyz", 13.0, true);
		Variable variable2 = new Variable("abc", "Test", true);

		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.addVariable(variable1)
			.addVariable(variable2)
			.test("x",			"x", "xyz", "xy", "xyzw", "abc")
			.test("xy",		"xy", "xyz", "xyzw", "x", "abc")
			.test("xyz",		"xyz", "xyzw", "x", "xy", "abc")
			.test("xyzw",		"xyzw", "xyz", "x", "xy", "abc")
			.test("abc",		"abc", "xyz", "x", "xy", "xyzw")
			.test("test(",		"xyzw", "abc", "xyz", "x", "xy")
			.test("test(x",	"x", "xyzw", "xyz", "xy", "abc")
			.test("test(xy",	"xy", "xyzw", "xyz", "x", "abc")
			.test("test(xyz",	"xyz", "xyzw", "x", "xy", "abc")
			.test("test(xyzw",	"xyzw", "xyz", "x", "xy", "abc")
			.test("test(abc",	"abc", "xyzw", "xyz", "x", "xy");
	}

	private static List<String> extractSuggestions(List<CompletionSuggestionIF> completions) {
		return completions.stream()
			.map(completion -> completion.getTextToInsert())
			.collect(Collectors.toList());
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
			settingsBuilder.evaluationModeCodeCompletion(evaluationMode);
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

		TestExecutor test(String javaExpression, String... expectedSuggestions) {
			ensureValidSettings();

			JavaParser parser = new JavaParser();
			int caret = javaExpression.length();
			List<String> suggestions = null;
			try {
				suggestions = extractSuggestions(parser.suggestCodeCompletion(javaExpression, settings, caret, testInstance));
			} catch (ParseException e) {
				assertTrue("Exception during code completion: " + e.getMessage(), false);
			}
			assertTrue(MessageFormat.format("Expression: {0}, expected completions: {1}, actual completions: {2}",
					javaExpression,
					expectedSuggestions,
					suggestions),
					suggestions.size() >= expectedSuggestions.length);
			for (int i = 0; i < expectedSuggestions.length; i++) {
				assertEquals("Expression: " + javaExpression, expectedSuggestions[i], suggestions.get(i));
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

		ErrorTestExecutor test(String javaExpression, int caret, Class<? extends Exception> expectedExceptionClass) {
			ensureValidSettings();

			JavaParser parser = new JavaParser();
			try {
				parser.suggestCodeCompletion(javaExpression, settings, caret, testInstance);
				assertTrue("Expression: " + javaExpression + " - Expected an exception", false);
			} catch (ParseException | IllegalStateException e) {
				assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
			}
			return this;
		}
	}
}
