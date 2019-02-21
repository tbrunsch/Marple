package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.settings.AccessLevel;
import org.junit.Test;

public class ConstructorTest
{
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
	public void testArrayCreation() {
		Object testInstance = new ArrayCreationTestClass();
		new TestExecutor(testInstance)
			.test("fill(new int[3])[0]",			2)
			.test("fill(new int[3])[1]",			1)
			.test("fill(new int[3])[2]",			0)
			.test("fill(new String[4])[0]",		"2")
			.test("fill(new String[4])[1]",		"1")
			.test("fill(new String[4])[2]",		"0")
			.test("fill(new String[4])[3]",		"-1")
			.test("get(new int[]{ 3, 1, 4 })[0]",	3)
			.test("get(new int[]{ 3, 1, 4 })[1]",	1)
			.test("get(new int[]{ 3, 1, 4 })[2]",	4)
			.test("get(new String[]{ \"only\", \"a\", \"test\" })[0]",	"only")
			.test("get(new String[]{ \"only\", \"a\", \"test\" })[1]",	"a")
			.test("get(new String[]{ \"only\", \"a\", \"test\" })[2]",	"test");

		new ErrorTestExecutor(null)
			.test("new int[-1]")
			.test("new int[]{ 1.3 }")
			.test("new String[]{ 1 }");
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

	private static class ArrayCreationTestClass
	{
		int[] fill(int[] a) {
			for (int i = 0; i < a.length; i++) {
				a[i] = 2 - i;
			}
			return a;
		}

		String[] fill(String[] a) {
			for (int i = 0; i < a.length; i++) {
				a[i] = String.valueOf(2 - i);
			}
			return a;
		}

		int[] get(int[] a) {
			return a;
		}

		String[] get(String[] a) {
			return a;
		}
	}
}
