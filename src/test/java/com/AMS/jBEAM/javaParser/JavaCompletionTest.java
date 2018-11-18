package com.AMS.jBEAM.javaParser;

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

public class JavaCompletionTest
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("xy",   "xy", "XY", "xy_z", "XYZ", "x", "X")
			.test("XYZ",  "XYZ", "XY", "X", "x", "xy")
			.test("X", "X", "x", "XY", "XYZ", "xy_z", "xy")
			.test("XY", "XY", "xy", "XYZ", "xy_z", "X", "x")
			.test("xy_z", "xy_z", "x", "xy", "XY", "X")
			.test("x", "x", "X", "xy_z", "xy", "XY", "XYZ")
			.test("XYW", "XY", "X", "x", "xy");

		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("xy", -1, IllegalStateException.class)
			.test("bla", -1, JavaParseException.class)
			.test("xy,", 3, JavaParseException.class);
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("member.", "member", "X", "xy", "XYZ")
			.test("member.x", "X", "xy", "XYZ", "member")
			.test("member.xy", "xy", "XYZ", "X", "member")
			.test("member.xyz", "XYZ", "xy", "X", "member")
			.test("member.mem", "member", "X", "xy", "XYZ");

		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("membeR.", -1, JavaParseException.class)
			.test("MEMBER.xy", -1, JavaParseException.class)
			.test("member.xy.XY", -1, JavaParseException.class)
			.test("member.xy", -1, IllegalStateException.class);
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("member.", "x", "xyz")
			.test("member.x", "x", "xyz")
			.test("member.xy", "xyz", "x")
			.test("member.xyz", "xyz", "x");

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("member.", "xy", "x", "xyz")
			.test("member.x", "x", "xy", "xyz")
			.test("member.xy", "xy", "xyz", "x")
			.test("member.xyz", "xyz", "xy", "x");
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("member[", "xyz", hashCode, "xyzw", "member", "xy")
			.test("member[x", "xyz", "xyzw", "xy", hashCode, "member")
			.test("member[xy", "xy", "xyz", "xyzw", hashCode, "member")
			.test("member[xyz", "xyz", "xyzw", "xy", hashCode, "member")
			.test("member[xyzw", "xyzw", "xyz", "xy", hashCode, "member")
			.test("member[m", "member", "xyz", hashCode, "xyzw", "xy")
			.test("member[xyz].", "member", "xy", "xyz", "xyzw")
			.test("member[xyzw].x", "xy", "xyz", "xyzw", "member");

		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("xy[", 3, JavaParseException.class)
			.test("xyz[", 4, JavaParseException.class)
			.test("xyzw[", 5, JavaParseException.class)
			.test("member[xy].", 11, JavaParseException.class)
			.test("member[xyz]", -1, IllegalStateException.class)
			.test("member[xyz)", 11, JavaParseException.class);
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
		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("array[", 6, JavaParseException.class);

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("array[", "i0", "i1", "i2")
			.test("array[i0].", "value")
			.test("array[i1].", "index");

		new ErrorTestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("array[i2].", 10, JavaParseException.class)
			.test("array[array[i1].index].", 23, JavaParseException.class);
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
		new ErrorTestExecuter(testInstance, EvaluationMode.STRONGLY_TYPED)
			.test("f(g(), s)", 9, JavaParseException.class);

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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("xy",		methodsFormatter.apply(new String[]{"xy", "XY", "xy_z", "XYZ", "XYZ", "x", "X"}))
			.test("XYZ",	methodsFormatter.apply(new String[]{"XYZ", "XYZ", "XY", "X", "x", "xy"}))
			.test("X",		methodsFormatter.apply(new String[]{"X", "x", "XY", "XYZ", "XYZ", "xy_z", "xy"}))
			.test("XY",		methodsFormatter.apply(new String[]{"XY", "xy", "XYZ", "XYZ", "xy_z", "X", "x"}))
			.test("xy_z",	methodsFormatter.apply(new String[]{"xy_z", "x", "xy", "XY", "X"}))
			.test("x",		methodsFormatter.apply(new String[]{"x", "X", "xy_z", "xy", "XY", "XYZ", "XYZ"}))
			.test("XYW",	methodsFormatter.apply(new String[]{"XY", "X", "x", "xy"}));

		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("other()", -1, IllegalStateException.class)
			.test("bla", -1, JavaParseException.class)
			.test("other(),", 8, JavaParseException.class);
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("prefix", "prefixC", "prefixD", "prefixI", "prefixC()", "prefixD()", "prefixI()")
			.test("prefixI", "prefixI", "prefixI()", "prefixC", "prefixD")
			.test("prefixD", "prefixD", "prefixD()", "prefixC", "prefixI")
			.test("prefixC", "prefixC", "prefixC()", "prefixD", "prefixI")
			.test("prefixI(", "prefixD", "prefixD()", "prefixC", "prefixI", "prefixC()", "prefixI()")
			.test("prefixD(", "prefixC", "prefixC()", "prefixD", "prefixI", "prefixD()", "prefixI()")
			.test("prefixC(", "prefixI", "prefixI()", "hashCode()", "prefixC", "prefixC()", "prefixD", "prefixD()");

		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("prefixI(prefixD)", -1, IllegalStateException.class)
			.test("prefixD(prefixI)", 16, JavaParseException.class)
			.test("prefixC(prefixI,", 16, JavaParseException.class)
			.test("prefixI(prefixD))", -1, JavaParseException.class);
	}

	@Test
	public void testMethodArgumentsWithEvaluation() {
		class TestClass
		{
			private Object getObject() { return new TestClass(); }
			private Object getTestClassObject(TestClass testClass) { return testClass.getObject(); }
		}

		Object testInstance = new TestClass();
		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("getTestClassObject(getObject()).get", 35, JavaParseException.class);

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("getTestClass", "getTestClass()")
			.test("getTestClass(c).", "d", "i", "getObject()")
			.test("getTestClass(c).get", "getObject()")
			.test("getTestClass(c).getObject(", "c", "s")
			.test("getTestClass(c).getObject(getTestClass(c).d).", "clone()", "equals()");

		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
				.test("getTestClazz().", -1, JavaParseException.class)
				.test("getTestClazz().i", -1, JavaParseException.class)
				.test("getTestClass().i.d", -1, JavaParseException.class)
				.test("getTestClass(c).getObject(getTestClass(c).d)", -1, IllegalStateException.class);
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("getObject().", "x", "xyz", "getInt()")
			.test("getObject().x", "x", "xyz", "getInt()")
			.test("getObject().xy", "xyz", "x", "getInt()")
			.test("getObject().xyz", "xyz", "x", "getInt()")
			.test("getObject().get", "getInt()", getClass, "x", "xyz");

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("getObject().", "xy", "x", "xyz", "getDouble()", "getInt()")
			.test("getObject().x", "x", "xy", "xyz", "getDouble()", "getInt()")
			.test("getObject().xy", "xy", "xyz", "x", "getDouble()", "getInt()")
			.test("getObject().xyz", "xyz", "xy", "x", "getDouble()", "getInt()")
			.test("getObject().get", "getDouble()", "getInt()", getClass, "xy", "x", "xyz");
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("getTestClasses()[", "xyz", hashCode, "xyzw", "xy")
			.test("getTestClasses()[x", "xyz", "xyzw", "xy", hashCode)
			.test("getTestClasses()[xy", "xy", "xyz", "xyzw", hashCode)
			.test("getTestClasses()[xyz", "xyz", "xyzw", "xy", hashCode)
			.test("getTestClasses()[xyzw", "xyzw", "xyz", "xy", hashCode)
			.test("getTestClasses()[g", "getTestClasses()", getClass, "xyz", hashCode, "xyzw", "xy")
			.test("getTestClasses()[xyz].", "xy", "xyz", "xyzw")
			.test("getTestClasses()[xyzw].x", "xy", "xyz", "xyzw");

		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("xy[", 3, JavaParseException.class)
			.test("xyz[", 4, JavaParseException.class)
			.test("xyzw[", 5, JavaParseException.class)
			.test("getTestClasses()[xy].", 21, JavaParseException.class)
			.test("getTestClasses()[xyz]", -1, IllegalStateException.class)
			.test("getTestClasses()[xyz)", 21, JavaParseException.class);
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
		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("getArray(size)[", 15, JavaParseException.class);

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
			.test("getArray(size)[", "index", "size")
			.test("getArray(size)[index].", "index", "size");
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("getTestClass(", "intValue", "stringValue")
			.test("getTestClass(i", "intValue", "stringValue", hashCode)
			.test("getTestClass(s", "stringValue", "intValue")
			.test("getTestClass(intValue,", "stringValue", toString, "intValue")
			.test("getTestClass(stringValue,", "intValue", hashCode, "stringValue")
			.test("getTestClass(intValue,stringValue).", "myInt")
			.test("getTestClass(stringValue,intValue).", "myString");
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
		new ErrorTestExecuter(testInstance, EvaluationMode.NONE)
			.test("getTestClass(getTestClass(i)).", 30, JavaParseException.class);

		new TestExecuter(testInstance, EvaluationMode.DUCK_TYPING)
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
		new TestExecuter(testInstance, EvaluationMode.NONE)
			.test("(", "x", "y", "getFloat()", "goDoNothing()")
			.test("(g", "getFloat()", "goDoNothing()", getClass)
			.test("(getFloat(y).toString()).le", "length()");
	}

	private static List<String> extractSuggestions(List<CompletionSuggestionIF> completions) {
		return completions.stream()
			.map(completion -> completion.getTextToInsert())
			.collect(Collectors.toList());
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

		TestExecuter test(String javaExpression, String... expectedSuggestions) {
			JavaParser parser = new JavaParser();
			int caret = javaExpression.length();
			List<String> suggestions = null;
			try {
				suggestions = extractSuggestions(parser.suggestCodeCompletion(javaExpression, evaluationMode, caret, testInstance));
			} catch (JavaParseException e) {
				assertTrue("Exception during code completion: " + e.getMessage(), false);
			}
			assertTrue(MessageFormat.format("Expression: {0}, expected completions: {1}, actual completions: {2}",
					javaExpression,
					expectedSuggestions,
					suggestions),
					suggestions.size() > expectedSuggestions.length);
			for (int i = 0; i < expectedSuggestions.length; i++) {
				assertEquals("Expression: " + javaExpression, expectedSuggestions[i], suggestions.get(i));
			}
			return this;
		}
	}

	/*
	 * Class for creating tests with expected exceptions
	 */
	private static class ErrorTestExecuter
	{
		private final Object													testInstance;
		private final EvaluationMode 											evaluationMode;

		ErrorTestExecuter(Object testInstance, EvaluationMode evaluationMode) {
			this.testInstance = testInstance;
			this.evaluationMode = evaluationMode;
		}

		ErrorTestExecuter test(String javaExpression, int caret, Class<? extends Exception> expectedExceptionClass) {
			JavaParser parser = new JavaParser();
			try {
				parser.suggestCodeCompletion(javaExpression, evaluationMode, caret, testInstance);
				assertTrue("Expression: " + javaExpression + " - Expected an exception", false);
			} catch (JavaParseException | IllegalStateException e) {
				assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
			}
			return this;
		}
	}
}
