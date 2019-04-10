package dd.kms.marple.swing;

import sun.misc.Unsafe;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.WEST;

public class TestFrame extends JFrame
{
    private static final Insets DEFAULT_INSETS  = new Insets(3, 3, 3, 3);

    private final JPanel                mainPanel           = new JPanel(new GridBagLayout());

    private final JPanel                labelPanel          = new JPanel(new FlowLayout());
    private final JLabel                label1              = new JLabel("Label 1");
    private final JLabel                label2              = new JLabel("Label 2");
    private final JLabel                label3              = new JLabel("Label 3");

    private final JPanel                checkBoxPanel       = new JPanel(new FlowLayout());
    private final JCheckBox             checkBox1           = new JCheckBox("CheckBox 1");
    private final JCheckBox             checkBox2           = new JCheckBox("CheckBox 2");
    private final JCheckBox             checkBox3           = new JCheckBox("CheckBox 3");

    private final JPanel                radioButtonPanel    = new JPanel(new FlowLayout());
    private final JRadioButton          radioButton1        = new JRadioButton("RadioButton 1");
    private final JRadioButton          radioButton2        = new JRadioButton("RadioButton 2");
    private final JRadioButton          radioButton3        = new JRadioButton("RadioButton 3");

    private final JTabbedPane           tabbedPane          = new JTabbedPane();
    private final JPanel                tabbedPanel1        = new JPanel(new FlowLayout());
    private final JPanel                tabbedPanel2        = new JPanel(new FlowLayout());
    private final JTextField            radioButtonTab      = new JTextField("RadioButtons");

    private final JList                 list                = new JList<ListItem>();

    private final JScrollPane           treeScrollPane      = new JScrollPane();
    private final JTree                 tree                = new JTree();

    private final JScrollPane           tableScrollPane     = new JScrollPane();
    private final JTable                table               = new JTable();

    private final JSplitPane            splitPane           = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JLabel                leftLabel           = new JLabel("left label");
    private final JLabel                rightLabel          = new JLabel("right label");

    private final JComboBox<Object>     comboBox            = new JComboBox<>();

    private final JPopupMenuButton      popupMenuButton     = new JPopupMenuButton("I create a popup");

    private final JButton               closeButton         = new JButton("Close");

    TestFrame() {
        super("Test Frame");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().add(mainPanel);

        int yPos = 0;

        mainPanel.add(labelPanel,       new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(checkBoxPanel,    new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(radioButtonPanel, new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(tabbedPane,       new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(list,             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(treeScrollPane,   new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(tableScrollPane,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(splitPane,        new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(comboBox,         new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(popupMenuButton,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
        mainPanel.add(closeButton,      new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

        add(labelPanel, label1, label2, label3);
        add(checkBoxPanel, checkBox1, checkBox2, checkBox3);
        add(radioButtonPanel, radioButton1, radioButton2, radioButton3);

        tabbedPane.addTab("CheckBoxes", tabbedPanel1);
        tabbedPane.addTab("RadioButtons", tabbedPanel2);
        tabbedPane.setTabComponentAt(1, radioButtonTab);
        add(tabbedPanel1, new JCheckBox("A check box"), new JCheckBox("Another check box"));
        add(tabbedPanel2, new JRadioButton("A radio button"), new JRadioButton("Another radio button"));
        radioButtonTab.setEditable(false);

        DefaultListModel<ListItem> listModel = new DefaultListModel<>();
        listModel.addElement(new ListItem("Key 1", "Value 1"));
        listModel.addElement(new ListItem("Key 2", "Value 2"));
        listModel.addElement(new ListItem("Key 3", "Value 3"));
        list.setModel(listModel);

        treeScrollPane.setViewportView(tree);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode child = new DefaultMutableTreeNode("Child");
        root.insert(child, 0);
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        tree.setModel(treeModel);
        treeScrollPane.setPreferredSize(new Dimension(300, 100));

        tableScrollPane.setViewportView(table);
        TableModel tableModel = new TestTableModel(
            new TestTableModelRow("Key 4", 4, 4.0),
            new TestTableModelRow("Key 5", 5, 5.0),
            new TestTableModelRow("Key 6", 6, 6.0),
            new TestTableModelRow("Key 7", 7, 7.0)
        );
        table.setModel(tableModel);
        tableScrollPane.setPreferredSize(new Dimension(300, 100));

        splitPane.setLeftComponent(leftLabel);
        splitPane.setRightComponent(rightLabel);

        DefaultComboBoxModel<Object> comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxModel.addElement(new ListItem("Key 8", "Eight"));
        comboBoxModel.addElement(new TestTableModelRow("Key 9", 9, 9.0));
        comboBox.setModel(comboBoxModel);

        closeButton.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
    }

    private void add(JPanel panel, JComponent... components) {
        for (JComponent component : components) {
            panel.add(component);
        }
    }

    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
        singleoneInstanceField.setAccessible(true);
        return (Unsafe) singleoneInstanceField.get(null);
    }

    private static class ListItem
    {
        private final String key;
        private final String value;

        private ListItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + ": " + value;
        }
    }

    private static class TestTableModelRow
    {
        private final String    key;
        private final int       intValue;
        private final double    doubleValue;

        private TestTableModelRow(String key, int intValue, double doubleValue) {
            this.key = key;
            this.intValue = intValue;
            this.doubleValue = doubleValue;
        }

        String getKey() {
            return key;
        }

        int getIntValue() {
            return intValue;
        }

        double getDoubleValue() {
            return doubleValue;
        }

        @Override
        public String toString() {
            return key + ": (" + intValue + ", " + doubleValue + ")";
        }
    }

    private static class TestTableModel implements TableModel
    {
        private final TestTableModelRow[]   rows;

        private TestTableModel(TestTableModelRow... rows) {
            this.rows = rows;
        }

        @Override
        public int getRowCount() {
            return rows.length;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Key";
                case 1:
                    return "Integer Value";
                case 2:
                    return "Double Value";
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return int.class;
                case 2:
                    return double.class;
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TestTableModelRow row = rows[rowIndex];
            switch (columnIndex) {
                case 0:
                    return row.getKey();
                case 1:
                    return row.getIntValue();
                case 2:
                    return row.getDoubleValue();
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            /* do nothing */
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            /* do nothing */
        }
    }

    private static class JPopupMenuButton extends JButton
    {
        private final JMenuItem menuItem1   = new JMenuItem("Item 1");
        private final Action    action2     = new AbstractAction("Action 2") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(JPopupMenuButton.this.getParent(), "Nothing interesting happening here...");
            }
        };
        private final JButton   button3     = new JButton("Button 3");

        JPopupMenuButton(String text) {
            super(text);
        }

        @Override
        public JPopupMenu getComponentPopupMenu() {
            JPopupMenu popup = new JPopupMenu();
            popup.add(menuItem1);
            popup.add(action2);
            popup.add(button3);
            return popup;
        }
    }
}
