package dd.kms.marple.gui.common;

import dd.kms.marple.InspectionContext;

import javax.swing.*;
import java.awt.*;

public class CurrentObjectPanel extends JPanel
{
	private final JLabel			classInfoLabel		= new JLabel();
	private final JLabel			toStringLabel		= new JLabel();

	private final InspectionContext	inspectionContext;

	public CurrentObjectPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());
		this.inspectionContext = inspectionContext;

		add(toStringLabel,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		add(classInfoLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		setBorder(BorderFactory.createEtchedBorder());
	}

	public void setCurrentObject(Object currentObject) {
		toStringLabel.setText(inspectionContext.getDisplayText(currentObject));
		classInfoLabel.setText(currentObject == null ? null : currentObject.getClass().toString());
	}
}
