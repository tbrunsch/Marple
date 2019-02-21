package com.AMS.jBEAM.javaParser.completionTests;

import org.junit.Test;

import java.util.*;

public class GenericsTest
{
	@Test
	public void testGenerics() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("testCollInt(",		"collListInt", "collSetInt", "listInt")
			.test("testCollString(",	"collListString", "collSetString", "listString")
			.test("testListInt(",		"listInt")
			.test("testListString(",	"listString")
			.test("testSetInt(",		"collListInt", "collListString", "collSetInt", "collSetString", "listInt", "listString")		// no class match, so fields ordered lexicographically
			.test("testSetString(",	"collListInt", "collListString", "collSetInt", "collSetString", "listInt", "listString");	// no class match, so fields ordered lexicographically

		new TestExecutor(testInstance)
			.enableDynamicTyping()
			.test("testCollInt(",		"collListInt", "collSetInt", "listInt")
			.test("testCollString(",	"collListString", "collSetString", "listString")
			.test("testListInt(",		"collListInt", "listInt")
			.test("testListString(",	"collListString", "listString")
			.test("testSetInt(",		"collSetInt")
			.test("testSetString(",	"collSetString");
	}

	private static class TestClass
	{
		private Collection<Integer> collListInt = new ArrayList<>();
		private Collection<String> collListString = new ArrayList<>();
		private Collection<Integer> collSetInt = new HashSet<>();
		private Collection<String> collSetString = new HashSet<>();
		private List<Integer> listInt = new ArrayList<>();
		private List<String> listString = new ArrayList<>();

		void testCollInt(Collection<Integer> c) {}
		void testCollString(Collection<String> c) {}
		void testListInt(List<Integer> l) {}
		void testListString(List<String> l) {}
		void testSetInt(Set<Integer> s) {}
		void testSetString(Set<String> s) {}
	}
}
