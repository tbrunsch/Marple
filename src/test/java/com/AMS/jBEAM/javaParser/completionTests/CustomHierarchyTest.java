package com.AMS.jBEAM.javaParser.completionTests;

import com.AMS.jBEAM.javaParser.common.CustomHierarchy;
import org.junit.Test;

public class CustomHierarchyTest
{
	@Test
	public void testCustomHierarchy() {
		new TestExecutor(null)
			.customHierarchyRoot(CustomHierarchy.ROOT)
			.test("{Component Ma",											"Component Manager")
			.test("{Component Manager}.comp",								"components")
			.test("{Excel Imp",											"Excel Importer")
			.test("{Excel Importer}.comp",									"componentType")
			.test("{Excel Importer#A",										"Activity")
			.test("{Excel Importer#Activity}.data",						"dataType")
			.test("{Productivity Calculation}.data",						"dataItems")
			.test("{Productivity Calculation#Relative Prod",				"Relative Productivity", "Relative Productivity Potential")
			.test("{Productivity Calculation#Total Productivity (h)}.val",	"value");
	}
}
