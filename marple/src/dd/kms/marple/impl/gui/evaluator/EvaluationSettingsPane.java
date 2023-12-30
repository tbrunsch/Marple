package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.api.settings.evaluation.AdditionalEvaluationSettings;
import dd.kms.marple.impl.gui.evaluator.imports.ImportPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class EvaluationSettingsPane extends JTabbedPane implements Disposable
{
	private final GeneralSettingsPanel	generalSettingsPanel;
	private final VariablePanel			variablePanel;
	private final ImportPanel			importPanel;

	public EvaluationSettingsPane(InspectionContext context) {
		generalSettingsPanel = new GeneralSettingsPanel(context);
		variablePanel = new VariablePanel(context);
		importPanel = new ImportPanel(context);

		addTab("General Settings",	generalSettingsPanel);
		addTab("Variables",			variablePanel);
		addTab("Imports",			importPanel);

		Map<String, AdditionalEvaluationSettings> additionalEvaluationSettings = context.getSettings().getEvaluationSettings().getAdditionalSettings();
		for (Map.Entry<String, AdditionalEvaluationSettings> entry : additionalEvaluationSettings.entrySet()) {
			String title = entry.getKey();
			AdditionalEvaluationSettings additionalEvalSettings = entry.getValue();
			JComponent additionalSettingsComponent = additionalEvalSettings.createSettingsComponent(context);
			addTab(title, additionalSettingsComponent);
		}

		setPreferredSize(new Dimension(600, 500));
	}

	public void updateContent() {
		generalSettingsPanel.updateContent();
		variablePanel.updateContent();
		importPanel.updateContent();
	}

	@Override
	public void dispose() {
		int tabCount = getTabCount();
		for (int i = 0; i < tabCount; i++) {
			Component panel = getComponentAt(i);
			if (panel instanceof Disposable) {
				((Disposable) panel).dispose();
			}
		}
	}
}
