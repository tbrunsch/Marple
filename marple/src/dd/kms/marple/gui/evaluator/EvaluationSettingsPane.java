package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;

class EvaluationSettingsPane extends JTabbedPane
{
	private final GeneralSettingsPanel	generalSettingsPanel;
	private final VariablePanel			variablePanel;

	EvaluationSettingsPane(InspectionContext inspectionContext) {
		generalSettingsPanel = new GeneralSettingsPanel(inspectionContext);
		variablePanel = new VariablePanel(inspectionContext);

		addTab("General Settings",	generalSettingsPanel);
		addTab("Variables", variablePanel);
		addTab("Imports", new ImportPanel(inspectionContext));
		addTab("Custom Hierarchy", new CustomHierarchyPanel(inspectionContext));

		setPreferredSize(new Dimension(400, 300));
	}
}
