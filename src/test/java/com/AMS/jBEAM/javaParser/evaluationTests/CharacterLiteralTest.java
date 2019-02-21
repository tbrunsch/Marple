package com.AMS.jBEAM.javaParser.evaluationTests;

import org.junit.Test;

public class CharacterLiteralTest
{
	@Test
	public void testCharacterLiteral() {
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

	private static class TestClass
	{
		char getChar(char c) { return c; }
	}
}
