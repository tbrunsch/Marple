package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.impl.gui.evaluator.imports.ImportPanel;

import javax.swing.*;
import java.awt.*;

public class EvaluationSettingsPane extends JTabbedPane implements Disposable
{
	private final GeneralSettingsPanel	generalSettingsPanel;
	private final VariablePanel			variablePanel;
	private final ImportPanel			importPanel;
	private final CustomHierarchyPanel	customHierarchyPanel;

	public EvaluationSettingsPane(InspectionContext context) {
		generalSettingsPanel = new GeneralSettingsPanel(context);
		variablePanel = new VariablePanel(context);
		importPanel = new ImportPanel(context);
		customHierarchyPanel = new CustomHierarchyPanel(context);

		addTab("General Settings",	generalSettingsPanel);
		addTab("Variables",			variablePanel);
		addTab("Imports",			importPanel);
		addTab("Custom Hierarchy",	customHierarchyPanel);

		setPreferredSize(new Dimension(600, 500));
	}

	public void updateContent() {
		generalSettingsPanel.updateContent();
		variablePanel.updateContent();
		importPanel.updateContent();
	}

	@Override
	public void dispose() {
		generalSettingsPanel.dispose();
		variablePanel.dispose();
		importPanel.dispose();
		customHierarchyPanel.dispose();
	}
}
