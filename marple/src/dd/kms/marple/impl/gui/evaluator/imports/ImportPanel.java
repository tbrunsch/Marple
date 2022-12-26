package dd.kms.marple.impl.gui.evaluator.imports;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

public class ImportPanel extends JPanel implements Disposable
{
	private final PackageImportPanel	packagesPanel;
	private final ClassImportPanel		classesPanel;

	public ImportPanel(InspectionContext context) {
		super(new GridBagLayout());

		packagesPanel = new PackageImportPanel(context);
		classesPanel = new ClassImportPanel(context);

		add(packagesPanel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(classesPanel,	new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
	}

	public void updateContent() {
		packagesPanel.updateContent();
		classesPanel.updateContent();
	}

	@Override
	public void dispose() {
		/* Currently there is nothing to do */
	}
}
