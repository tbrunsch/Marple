package dd.kms.marple.swing.gui;

import javax.swing.*;

public class SwingInspectionViewData
{
	private final String		title;
	private final JComponent	component;

	public SwingInspectionViewData(String title, JComponent component) {
		this.title = title;
		this.component = component;
	}

	public String getTitle() {
		return title;
	}

	public JComponent getComponent() {
		return component;
	}
}
