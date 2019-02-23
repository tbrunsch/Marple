package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.common.CustomHierarchy;
import com.AMS.jBEAM.javaParser.settings.Variable;
import org.junit.Test;

public class WildcardTest
{
	@Test
	public void testWildcardCompletion() {
		Object testInstance = new TestClass();
		Variable variable1 = new Variable("tempFloatVariable", 13.5f, true);
		Variable variable2 = new Variable("tempCharVariable", 'c', true);
		new TestExecutor(testInstance)
			.importPackage("java.util")
			.addVariable(variable1)
			.addVariable(variable2)
			.customHierarchyRoot(CustomHierarchy.ROOT)
			.test("xY", 							"xYZ", "xyz", "xxYyZz")
			.test("xYZ",							"xYZ", "xyz", "xxYyZz")
			.test("ArLi",							"ArrayList")
			.test("LHS",							"LinkedHashSet")
			.test("gVA",							"getValueAsDouble()", "getValueAsInt()")
			.test("geValA",						"getValueAsDouble()", "getValueAsInt()")
			.test("gVAD",							"getValueAsDouble()")
			.test("gVAI",							"getValueAsInt()")
			.test("tFV",							"tempFloatVariable")
			.test("tCV",							"tempCharVariable")
			.test("{CM",							"Component Manager")
			.test("{Productivity Calculation#RP",	"Relative Productivity", "Relative Productivity Potential");
	}

	private static class TestClass
	{
		private int xxyyzz;
		private int xyz;
		private int xYZ;
		private int xxYyZz;

		double getValueAsDouble() { return 0.0; }
		int getValueAsInt() { return 0; }
	}
}
