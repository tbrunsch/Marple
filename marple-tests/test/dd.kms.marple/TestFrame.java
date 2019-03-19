package dd.kms.marple;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.WindowEvent;

public class TestFrame extends JFrame
{
    private final JPanel mainPanel       = new JPanel(new BorderLayout());

    private final JPanel        panel1          = new JPanel(new FlowLayout());
    private final JPanel        panel2          = new JPanel(new FlowLayout());

    private final JLabel        label1          = new JLabel("Label 1");
    private final JCheckBox     checkBox1       = new JCheckBox("Checkbox 1");
    private final JRadioButton  radioButton1    = new JRadioButton("Radiobutton 1");

    private final JLabel        label2          = new JLabel("Label 2");
    private final JCheckBox     checkBox2       = new JCheckBox("Checkbox 2");
    private final JRadioButton  radioButton2    = new JRadioButton("Radiobutton 2");

    private final JScrollPane   scrollPane      = new JScrollPane();
    private final JTree         tree;

    private final JButton       closeButton     = new JButton("Close");

    TestFrame() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode child = new DefaultMutableTreeNode("Child");
        root.insert(child, 0);
        tree = new JTree(root);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().add(mainPanel);

        mainPanel.add(panel1, BorderLayout.NORTH);
        mainPanel.add(panel2, BorderLayout.SOUTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(closeButton, BorderLayout.EAST);

        panel1.add(label1);
        panel1.add(checkBox1);
        panel1.add(radioButton1);

        panel2.add(label2);
        panel2.add(checkBox2);
        panel2.add(radioButton2);

        scrollPane.setViewportView(tree);

        closeButton.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
    }
}
