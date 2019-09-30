package dd.kms.marple;

import javax.swing.*;
import java.awt.*;

class ModalTestDialog extends JDialog
{
	private final JPanel		mainPanel	= new JPanel(new FlowLayout());

	private final JCheckBox		checkBox	= new JCheckBox("Checkbox");
	private final JLabel		label		= new JLabel("Label");
	private final JTextField	textField	= new JTextField("Textfield");
	private final JRadioButton	radioButton	= new JRadioButton("Radiobutton");
	private final JButton		button		= new JButton("Button");
	private final JComboBox<?>	comboBox	= new JComboBox<>();

	ModalTestDialog(Window owner) {
		super(owner, "Modal Dialog", ModalityType.APPLICATION_MODAL);

		getContentPane().add(mainPanel);

		mainPanel.add(checkBox);
		mainPanel.add(label);
		mainPanel.add(textField);
		mainPanel.add(radioButton);
		mainPanel.add(button);
		mainPanel.add(comboBox);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
}
