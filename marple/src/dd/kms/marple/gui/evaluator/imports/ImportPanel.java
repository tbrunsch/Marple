package dd.kms.marple.gui.evaluator.imports;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

public class ImportPanel extends JPanel
{
	private static final Insets	DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	private final PackageImportPanel	packagesPanel;
	private final ClassImportPanel		classesPanel;

	private final InspectionContext	inspectionContext;

	public ImportPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;

		packagesPanel = new PackageImportPanel(inspectionContext);
		classesPanel = new ClassImportPanel(inspectionContext);

		add(packagesPanel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(classesPanel,	new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
	}

	public void updateContent() {
		packagesPanel.updateContent();
		classesPanel.updateContent();
	}
}
