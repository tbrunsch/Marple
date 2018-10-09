package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.swing.SwingObjectInspector;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class SwingSubComponentHierarchyStrategies
{
    public static void register() {
        SwingObjectInspector inspector = SwingObjectInspector.getInspector();
        inspector.addSubcomponentHierarchyStrategyFor(JTree.class, SwingSubComponentHierarchyStrategies::getJTreeSubComponentHierarchy);
        // TODO: Add more
    }

    private static List<Object> getJTreeSubComponentHierarchy(JTree tree, Point point) {
        TreePath path = tree.getPathForLocation(point.x, point.y);
        return path == null ? Collections.emptyList() : Collections.singletonList(path.getLastPathComponent());
    }
}
