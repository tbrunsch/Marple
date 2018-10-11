package com.AMS.jBEAM.objectInspection.swing.gui;

import com.AMS.jBEAM.objectInspection.InspectionLink;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

class SwingInspectionUtils
{
    static JTree createInspectionLinkTree(Iterable<InspectionLink> linearLinkHierarchy) {
        MutableTreeNode root = null;
        MutableTreeNode parent = null;
        for (InspectionLink link : linearLinkHierarchy) {
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
                if (userObject instanceof InspectionLink) {
                    InspectionLink link = (InspectionLink) userObject;
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

    static Object getTreeNode(JTree tree, Point point) {
        TreePath path = tree.getPathForLocation((int) point.getX(), (int) point.getY());
        return path == null ? null : path.getLastPathComponent();
    }
}
