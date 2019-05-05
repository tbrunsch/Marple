package dd.kms.marple.components;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

public class SubcomponentHierarchyStrategies
{
	public static void addDefaultSubcomponentStrategies(ComponentHierarchyModelBuilder builder) {
		builder
			.subcomponentHierarchyStrategy(JTree.class,			ComponentHierarchyModels.createSingleSubcomponentStrategy(SubcomponentHierarchyStrategies::getTreeNode))
			.subcomponentHierarchyStrategy(JList.class,			ComponentHierarchyModels.createSingleSubcomponentStrategy(SubcomponentHierarchyStrategies::getListItem))
			.subcomponentHierarchyStrategy(JTable.class,		ComponentHierarchyModels.createSingleSubcomponentStrategy(SubcomponentHierarchyStrategies::getCell));
	}

	private static Object getTreeNode(JTree tree, Point point) {
		TreePath path = tree.getPathForLocation(point.x, point.y);
		return path == null ? null : path.getLastPathComponent();
	}

	private static Object getListItem(JList list, Point point) {
		ListModel model = list.getModel();
		if (model == null) {
			return null;
		}
		int index = list.locationToIndex(point);
		return index < 0 ? null : model.getElementAt(index);
	}

	private static Object getCell(JTable table, Point point) {
		int row = table.rowAtPoint(point);
		int col = table.columnAtPoint(point);
		return table.getModel().getValueAt(row, col);
	}
}
