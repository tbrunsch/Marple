package com.AMS.jBEAM.javaParser;

import org.junit.Test;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        new TestBuilder(testInstance)
            .addTest("xy",   "xy", "XY", "xy_z", "XYZ", "x", "X")
			.addTest("XYZ",  "XYZ", "XY", "X", "x", "xy")
			.addTest("X", "X", "x", "XY", "XYZ", "xy_z", "xy")
			.addTest("XY", "XY", "xy", "XYZ", "xy_z", "X", "x")
			.addTest("xy_z", "xy_z", "x", "xy", "XY", "X")
			.addTest("x", "x", "X", "xy_z", "xy", "XY", "XYZ")
			.addTest("XYW", "XY", "X", "x", "xy")
            .performTests();

        new ErrorTestBuilder(testInstance)
			.addTest("xy", -1, IllegalStateException.class)
			.addTest("bla", -1, JavaParseException.class)
			.addTest("xy,", 3, JavaParseException.class)
			.performTests();
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
		new TestBuilder(testInstance)
			.addTest("member.", "xy", "X", "XYZ", "member")
			.addTest("member.x", "X", "xy", "XYZ", "member")
			.addTest("member.xy", "xy", "XYZ", "X", "member")
			.addTest("member.xyz", "XYZ", "xy", "X", "member")
			.addTest("member.mem", "member", "xy", "X", "XYZ")
			.performTests();

		new ErrorTestBuilder(testInstance)
			.addTest("membeR.", -1, JavaParseException.class)
			.addTest("MEMBER.xy", -1, JavaParseException.class)
			.addTest("member.xy.XY", -1, JavaParseException.class)
			.addTest("member.xy", -1, IllegalStateException.class)
			.performTests();
    }

    @Test
    public void testFieldArray() {
    	class TestClass
		{
			private String 		xy		= "xy";
			private int 		xyz		= 7;
			private char		xyzw	= 'W';

			private TestClass[]	member	= null;
		}

		TestClass testInstance = new TestClass();
		new TestBuilder(testInstance)
			.addTest("member[", "xyz", "hashCode()", "xyzw", "xy", "member")
			.addTest("member[x", "xyz", "xyzw", "xy", "member")
			.addTest("member[xy", "xy", "xyz", "xyzw", "member")
			.addTest("member[xyz", "xyz", "xyzw", "xy", "member")
			.addTest("member[xyzw", "xyzw", "xyz", "xy", "member")
			.addTest("member[m", "member", "xyz", "xyzw", "xy")
			.addTest("member[xyz].", "xy", "xyz", "xyzw", "member")
			.addTest("member[xyzw].x", "xy", "xyz", "xyzw", "member")
			.performTests();

    	new ErrorTestBuilder(testInstance)
			.addTest("xy[", 3, JavaParseException.class)
			.addTest("xyz[", 4, JavaParseException.class)
			.addTest("xyzw[", 5, JavaParseException.class)
			.addTest("member[xy].", 11, JavaParseException.class)
			.addTest("member[xyz]", -1, IllegalStateException.class)
			.addTest("member[xyz)", 11, JavaParseException.class)
			.performTests();
    }

    private static List<String> extractSuggestions(List<CompletionSuggestion> completions) {
        return completions.stream()
            .map(completion -> completion.getInsertionInfo().getTextToInsert())
            .collect(Collectors.toList());
    }

    /*
     * Class for creating tests with expected successful code completions
     */
    private static class TestBuilder
    {
    	private final Object					testInstance;
        private final Map<String, List<String>> expectedResults = new LinkedHashMap<>();

        TestBuilder(Object testInstance) {
        	this.testInstance = testInstance;
		}

        TestBuilder addTest(String javaExpression, String... expectedSuggestions) {
            expectedResults.put(javaExpression, Arrays.asList(expectedSuggestions));
            return this;
        }

        void performTests() {
            JavaParser parser = new JavaParser();
            for (Map.Entry<String, List<String>> inputOutputPair : expectedResults.entrySet()) {
                String javaExpression = inputOutputPair.getKey();
                List<String> expectedSuggestions = inputOutputPair.getValue();
                int caret = javaExpression.length();
                List<String> suggestions = null;
                try {
                    suggestions = extractSuggestions(parser.suggestCodeCompletion(javaExpression, caret, testInstance));
                } catch (JavaParseException e) {
                    assertTrue("Exception during code completion: " + e.getMessage(), false);
                }
                assertTrue(MessageFormat.format("Expression: {0}, expected completions: {1}, actual completions: {2}",
                        javaExpression,
                        expectedSuggestions,
                        suggestions),
                        suggestions.size() > expectedSuggestions.size());
                for (int i = 0; i < expectedSuggestions.size(); i++) {
                    assertEquals("Expression: " + javaExpression, expectedSuggestions.get(i), suggestions.get(i));
                }
            }
        }
    }

    /*
     * Class for creating tests with expected exceptions
     */
    private static class ErrorTestBuilder
	{
		private static class JavaExpressionCaretPair
		{
			private final String javaExpression;

			private final int caret;

			private JavaExpressionCaretPair(String javaExpression, int caret) {
				this.javaExpression = javaExpression;
				this.caret = caret;
			}

			String getJavaExpression() {
				return javaExpression;
			}

			int getCaret() {
				return caret;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				JavaExpressionCaretPair that = (JavaExpressionCaretPair) o;
				return caret == that.caret &&
						Objects.equals(javaExpression, that.javaExpression);
			}

			@Override
			public int hashCode() {
				return Objects.hash(javaExpression, caret);
			}
		}

		private final Object													testInstance;
		private final Map<JavaExpressionCaretPair, Class<? extends Exception>>	testDataToExpectedExceptionClass	= new LinkedHashMap<>();

		ErrorTestBuilder(Object testInstance) {
			this.testInstance = testInstance;
		}

		ErrorTestBuilder addTest(String javaExpression, int caret, Class<? extends Exception> expectedExceptionClass) {
			testDataToExpectedExceptionClass.put(new JavaExpressionCaretPair(javaExpression, caret), expectedExceptionClass);
			return this;
		}

		void performTests() {
			JavaParser parser = new JavaParser();
			for (Map.Entry<JavaExpressionCaretPair, Class<? extends Exception>> inputOutputPair : testDataToExpectedExceptionClass.entrySet()) {
				JavaExpressionCaretPair testData = inputOutputPair.getKey();
				String javaExpression = testData.getJavaExpression();
				int caret = testData.getCaret();
				Class<? extends Exception> expectedExceptionClass = inputOutputPair.getValue();
				try {
					parser.suggestCodeCompletion(javaExpression, caret, testInstance);
					assertTrue("Expression: " + javaExpression + " - Expected an exception", false);
				} catch (JavaParseException | IllegalStateException e) {
					assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
				}
			}
		}
	}
}
