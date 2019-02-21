package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.JavaParser;
import com.AMS.jBEAM.javaParser.ParseException;
import com.AMS.jBEAM.javaParser.common.AbstractTestExecutor;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import com.AMS.jBEAM.javaParser.settings.ParserSettings;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Class for creating tests with expected exceptions
 */
public class ErrorTestExecutor extends AbstractTestExecutor<ErrorTestExecutor>
{
	public ErrorTestExecutor(Object testInstance) {
		super(testInstance);
	}

	public ErrorTestExecutor evaluationMode(EvaluationMode evaluationMode) {
		settingsBuilder.evaluationModeCodeCompletion(evaluationMode);
		return this;
	}

	public ErrorTestExecutor test(String javaExpression, int caret, Class<? extends Exception> expectedExceptionClass) {
		ParserSettings settings = settingsBuilder.build();

		JavaParser parser = new JavaParser();
		try {
			parser.suggestCodeCompletion(javaExpression, settings, caret, testInstance);
			fail("Expression: " + javaExpression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
		}
		return this;
	}
}