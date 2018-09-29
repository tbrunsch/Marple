package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.InspectionUtils;
import com.AMS.jBEAM.objectInspection.ObjectInspector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SwingInspectionFrame extends JFrame
{
    private static final Dimension INITIAL_SIZE = new Dimension(400, 300);

    private final JButton   prevButton  = new JButton("<");
    private final JButton   nextButton  = new JButton(">");

    private final JPanel    contentPanel    = new JPanel();

    private final Runnable  onClosed;

    public SwingInspectionFrame(Runnable onClosed) {
        this.onClosed = onClosed;
        SwingUtilities.invokeLater(this::configure);
    }

    private void configure() {
        setTitle("Object Inspection Manager");

        JPanel mainPanel = new JPanel(new GridBagLayout());
        int xPos = 0;
        mainPanel.add(prevButton, new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        mainPanel.add(new JScrollPane(contentPanel), new GridBagConstraints(xPos++, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        mainPanel.add(nextButton, new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

        getContentPane().add(mainPanel);

        prevButton.addActionListener(e -> onPrevButtonClicked());
        nextButton.addActionListener(e -> onNextButtonClicked());

        setSize(INITIAL_SIZE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClosed.run();
            }
        });
    }

    public void addComponent(JComponent component) {
        int y = contentPanel.getComponentCount();
        contentPanel.add(component, new GridBagConstraints(0, y, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    public void removeAllComponents() {
        contentPanel.removeAll();
        updateNavigationButtons();
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
}
