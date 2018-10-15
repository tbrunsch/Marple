package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.InspectionLinkIF;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwingInspectionUtils
{
    public static List<Object> getComponentHierarchy(Component component) {
        List<Object> componentHierarchy = new ArrayList<>();
        for (Component curComponent = component; curComponent != null; curComponent = curComponent.getParent()) {
            componentHierarchy.add(0, curComponent);
        }
        return componentHierarchy;
    }


    static JTree createInspectionLinkTree(Iterable<InspectionLinkIF> linearLinkHierarchy) {
        MutableTreeNode root = null;
        MutableTreeNode parent = null;
        for (InspectionLinkIF link : linearLinkHierarchy) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(link);
            if (root == null) {
                root = node;
            } else {
                parent.insert(node, 0);
            }
            parent = node;
        }
        if (root == null) {
            return new JTree();
        }
        JTree tree = new JTree(root);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path == null) {
                return;
            }
            Object node = path.getLastPathComponent();
            if (!(node instanceof DefaultMutableTreeNode)) {
                return;
            }
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObject instanceof InspectionLinkIF) {
                InspectionLinkIF link = (InspectionLinkIF) userObject;
                link.run();
            }
            }
        });

        tree.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            Cursor cursor = path == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            tree.setCursor(cursor);
            }
        });

        return tree;
    }
}
