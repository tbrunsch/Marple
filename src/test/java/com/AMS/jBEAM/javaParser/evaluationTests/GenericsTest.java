package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GenericsTest
{
	@Test
	public void testGenerics() {
		TestClass testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("testCollInt(collListInt)",			testInstance.collListInt)
			.test("testCollInt(collSetInt)",			testInstance.collSetInt)
			.test("testCollInt(listInt)",				testInstance.listInt)
			.test("testCollString(collListString)",	testInstance.collListString)
			.test("testCollString(collSetString)",		testInstance.collSetString)
			.test("testCollString(listString)",		testInstance.listString)
			.test("testListInt(listInt)",				testInstance.listInt)
			.test("testListString(listString)",		testInstance.listString);

		new ErrorTestExecutor(testInstance)
			.test("testCollInt(collListString)")
			.test("testCollInt(collSetString)")
			.test("testCollInt(listString)")

			.test("testCollString(collListInt)")
			.test("testCollString(collSetInt)")
			.test("testCollString(listInt)")

			.test("testListInt(collListInt)")
			.test("testListInt(collListString)")
			.test("testListInt(collSetInt)")
			.test("testListInt(collSetString)")
			.test("testListInt(listString)")

			.test("testListString(collListInt)")
			.test("testListString(collListString)")
			.test("testListString(collSetInt)")
			.test("testListString(collSetString)")
			.test("testListString(listInt)")

			.test("testSetInt(collListInt)")
			.test("testSetInt(collListString)")
			.test("testSetInt(collSetInt)")
			.test("testSetInt(collSetString)")
			.test("testSetInt(listInt)")
			.test("testSetInt(listString)")

			.test("testSetString(collListInt)")
			.test("testSetString(collListString)")
			.test("testSetString(collSetInt)")
			.test("testSetString(collSetString)")
			.test("testSetString(listInt)")
			.test("testSetString(listString)");

		new TestExecutor(testInstance)
			.evaluationMode(EvaluationMode.DUCK_TYPING)
			.test("testListInt(collListInt)",			testInstance.collListInt)
			.test("testListString(collListString)",	testInstance.collListString)
			.test("testSetInt(collSetInt)",			testInstance.collSetInt)
			.test("testSetString(collSetString)",		testInstance.collSetString);
	}

	private static class TestClass
	{
		private final Collection<Integer>	collListInt		= Lists.newArrayList(1);
		private final Collection<String>	collListString	= Lists.newArrayList("2");
		private final Collection<Integer>	collSetInt		= Sets.newHashSet(3);
		private final Collection<String>	collSetString	= Sets.newHashSet("4");
		private final List<Integer> 		listInt			= Lists.newArrayList(5);
		private final List<String>			listString		= Lists.newArrayList("6");

		Collection<Integer>	testCollInt(Collection<Integer> c) { return c; }
		Collection<String>	testCollString(Collection<String> c) { return c; }
		List<Integer>		testListInt(List<Integer> l) { return l; }
		List<String>		testListString(List<String> l) { return l; }
		Set<Integer> 		testSetInt(Set<Integer> s) { return s; }
		Set<String>			testSetString(Set<String> s) { return s; }
	}
}
