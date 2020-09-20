package dd.kms.marple.api.settings.components;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Collections;
import java.util.function.BiFunction;

public class ComponentHierarchyModels
{
	public static <C extends Component> SubcomponentHierarchyStrategy<C> createSingleSubcomponentStrategy(BiFunction<C, Point, Object> subcomponentDetectionStrategy) {
		return (component, point) -> {
			Object subcomponent = subcomponentDetectionStrategy.apply(component, point);
			return subcomponent == null ? Collections.emptyList() : Collections.singletonList(subcomponent);
		};
	}

	public static void addDefaultSubcomponentStrategies(ComponentHierarchyModelBuilder builder) {
		builder
			.subcomponentHierarchyStrategy(JTree.class,		createSingleSubcomponentStrategy(ComponentHierarchyModels::getTreeNode))
			.subcomponentHierarchyStrategy(JList.class,		createSingleSubcomponentStrategy(ComponentHierarchyModels::getListItem))
			.subcomponentHierarchyStrategy(JTable.class,	createSingleSubcomponentStrategy(ComponentHierarchyModels::getCell));
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
