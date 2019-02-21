package com.AMS.jBEAM.javaParser.completionTests;

import org.junit.Test;

public class WildcardTest
{
	@Test
	public void testWildcardCompletion() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.importPackage("java.util")
			.test("xY", 	"xYZ", "xyz", "xxYyZz")
			.test("xYZ",	"xYZ", "xyz", "xxYyZz")
			.test("ArLi",	"ArrayList");
	}

	private static class TestClass
	{
		private int xxyyzz;
		private int xyz;
		private int xYZ;
		private int xxYyZz;
	}
}
