package com.AMS.jBEAM.javaParser.evaluationTests;

import com.AMS.jBEAM.javaParser.common.CustomHierarchy;
import org.junit.Test;

public class CustomHierarchyTest
{
	@Test
	public void testCustomHierarchy() {
		CustomHierarchy h = new CustomHierarchy();

		new TestExecutor(null)
			.customHierarchyRoot(h.ROOT)
			.test("{Component Manager}",											h.COMPONENT_MANAGER)
			.test("{Component Manager}.components.get(0)",							h.COMPONENT_MANAGER.getComponents().get(0))
			.test("{Excel Importer}",												h.EXCEL_IMPORTER)
			.test("{Excel Importer}.componentType",								h.EXCEL_IMPORTER.getComponentType())
			.test("{Excel Importer#Activity}",										h.ACTIVITY)
			.test("{Excel Importer#Activity}.dataType",							h.ACTIVITY.getDataType())
			.test("{Productivity Calculation}.dataItems.get(1)",					h.PRODUCTIVITY_CALCULATION.getDataItems().get(1))
			.test("{Productivity Calculation#Relative Productivity Potential}",	h.RELATIVE_PRODUCTIVITY_POTENTIAL)
			.test("{Productivity Calculation#Total Productivity (h)}.value",		h.TOTAL_PRODUCTIVITY.getValue());

		new ErrorTestExecutor(null)
			.customHierarchyRoot(h.ROOT)
			.test("Component Manager")
			.test("{Component Manager")
			.test("{Component Management}")
			.test("{Excel Importer#componentType}");
	}
}
