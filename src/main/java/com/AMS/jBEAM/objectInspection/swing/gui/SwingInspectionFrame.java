package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.InspectionUtils;
import com.AMS.jBEAM.objectInspection.ObjectInspector;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SwingInspectionFrame extends JFrame
{
    private static final Dimension INITIAL_SIZE = new Dimension(400, 300);

    private final JButton       prevButton          = new JButton("<");
    private final JButton       nextButton          = new JButton(">");

    private final JPanel        contentPanel        = new JPanel(new BorderLayout());

    private final JPanel        objectOverviewPanel = new JPanel(new GridBagLayout());
    private final JLabel        classInfoLabel      = new JLabel();
    private final JLabel        toStringLabel       = new JLabel();

    private final JTabbedPane   tabbedPane          = new JTabbedPane();

    private final Runnable      onClosed;

    private boolean             initializing;
    private String              lastSelectedTabTitle;

    public SwingInspectionFrame(Runnable onClosed) {
        this.onClosed = onClosed;
        configure();
    }

    private void configure() {
        setTitle("Object Inspection Manager");

        JPanel mainPanel = new JPanel(new GridBagLayout());
        getContentPane().add(mainPanel);

        int xPos = 0;
        mainPanel.add(prevButton,   new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        mainPanel.add(contentPanel, new GridBagConstraints(xPos++, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        mainPanel.add(nextButton,   new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

        contentPanel.add(objectOverviewPanel, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(tabbedPane), BorderLayout.CENTER);

        objectOverviewPanel.setBorder(BorderFactory.createEtchedBorder());
        objectOverviewPanel.add(toStringLabel,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        objectOverviewPanel.add(classInfoLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

        setSize(INITIAL_SIZE);

        prevButton.addActionListener(e -> onPrevButtonClicked());
        nextButton.addActionListener(e -> onNextButtonClicked());
        tabbedPane.addChangeListener(e -> onTabChanged());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClosed.run();
            }
        });
    }

    public void beginInspection(Object object) {
        initializing = true;
        toStringLabel.setText('"' + object.toString() + '"');
        classInfoLabel.setText(object.getClass().toString());
    }

    public void addComponent(String title, JComponent component) {
        tabbedPane.add(component, title);
    }

    public void removeAllComponents() {
        tabbedPane.removeAll();
        updateNavigationButtons();
    }

    public void endInspection() {
        if (lastSelectedTabTitle != null) {
            int indexOfLastSelectedTab = tabbedPane.indexOfTab(lastSelectedTabTitle);
            if (indexOfLastSelectedTab >= 0) {
                tabbedPane.setSelectedIndex(indexOfLastSelectedTab);
            }
        }
        setVisible(true);
        initializing = false;
    }

    private void updateNavigationButtons() {
        ObjectInspector inspector = ObjectInspector.getInspector();
        inspector.runLater(() -> {
            prevButton.setEnabled(inspector.canInspectPrevious());
            nextButton.setEnabled(inspector.canInspectNext());
        });
    }

    /*
     * Event Handling
     */
    private void onPrevButtonClicked() {
        ObjectInspector.getInspector().inspectPrevious();
        updateNavigationButtons();
    }

    private void onNextButtonClicked() {
        ObjectInspector.getInspector().inspectNext();
        updateNavigationButtons();
    }

    private void onTabChanged() {
        if (initializing) {
            return;
        }
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex >= 0) {
            lastSelectedTabTitle = tabbedPane.getTitleAt(selectedIndex);
        }
    }
}
