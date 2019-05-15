package dd.kms.marple.gui.evaluator;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;

class EvaluationSettingsPane extends JTabbedPane
{
	private final GeneralSettingsPanel	generalSettingsPanel;
	private final VariablePanel			variablePanel;
	private final ImportPanel			importPanel;
	private final CustomHierarchyPanel	customHierarchyPanel;

	EvaluationSettingsPane(InspectionContext inspectionContext) {
		generalSettingsPanel = new GeneralSettingsPanel(inspectionContext);
		variablePanel = new VariablePanel(inspectionContext);
		importPanel = new ImportPanel(inspectionContext);
		customHierarchyPanel = new CustomHierarchyPanel(inspectionContext);

		addTab("General Settings",	generalSettingsPanel);
		addTab("Variables", variablePanel);
		addTab("Imports", importPanel);
		addTab("Custom Hierarchy", customHierarchyPanel);

		setPreferredSize(new Dimension(400, 300));
	}

	void updateContent() {
		generalSettingsPanel.updateContent();
		variablePanel.updateContent();
		importPanel.updateContent();
	}
}
