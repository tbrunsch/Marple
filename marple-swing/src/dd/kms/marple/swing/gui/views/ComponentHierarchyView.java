package dd.kms.marple.swing.gui.views;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import dd.kms.marple.ReflectionUtils;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.swing.gui.Actions;
import dd.kms.marple.swing.inspector.SwingObjectInspector;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ComponentHierarchyView extends JPanel
{
	private static final String	NAME	= "Component Hierarchy";

	private final JScrollPane					scrollPane;
	private final JTree							tree;

	private final InspectionContext<Component>	inspectionContext;

	public ComponentHierarchyView(InspectionContext<Component> inspectionContext, List<Component> componentHierarchy, List<?> subcomponentHierarchy) {
		super(new GridBagLayout());
		this.inspectionContext = inspectionContext;

		setName(NAME);

		List<ActionProvider> actionProviderHierarchy = createActionProviderHierarchy(componentHierarchy, subcomponentHierarchy);
		tree = createLinearActionProviderTree(actionProviderHierarchy);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		scrollPane  = new JScrollPane(tree);
		add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
	}

	private List<ActionProvider> createActionProviderHierarchy(List<Component> componentHierarchy, List<?> subcomponentHierarchy) {
		Multimap<Object, Field> fieldsByObject = ArrayListMultimap.create();
		Set<Object> objectsToFind = new HashSet<>();

		List<Object> hierarchy = new ArrayList<>();
		hierarchy.addAll(componentHierarchy);
		hierarchy.addAll(subcomponentHierarchy);

		/*
		 * Traverse hierarchy from child to parent because usually children are members of parents and not vice versa
		 */
		for (Object hierarchyNode : Lists.reverse(hierarchy)) {
			objectsToFind.add(hierarchyNode);
			fieldsByObject.putAll(ReflectionUtils.findFieldValues(hierarchyNode, objectsToFind));
		}

		List<ActionProvider> actionProviderHierarchy = new ArrayList<>(hierarchy.size());
		for (int i = 0; i < componentHierarchy.size(); i++) {
			Component component = componentHierarchy.get(i);
			Collection<Field> fieldsForComponent = fieldsByObject.get(component);
			ActionProvider actionProvider = createActionProvider(componentHierarchy.subList(0, i+1), Collections.emptyList(), fieldsForComponent);
			actionProviderHierarchy.add(actionProvider);
		}
		for (int j = 0; j < subcomponentHierarchy.size(); j++) {
			Object subcomponent = subcomponentHierarchy.get(j);
			Collection<Field> fieldsForSubcomponent = fieldsByObject.get(subcomponent);
			ActionProvider actionProvider = createActionProvider(componentHierarchy, subcomponentHierarchy.subList(0, j+1), fieldsForSubcomponent);
			actionProviderHierarchy.add(actionProvider);
		}
		return actionProviderHierarchy;
	}

	/*
	 * Action Providers
	 */
	private ActionProvider createActionProvider(List<Component> componentHierarchy, List<?> subcomponentHierarchy, Collection<Field> detectedFields) {
		Object hierarchyLeaf = SwingObjectInspector.getHierarchyLeaf(componentHierarchy, subcomponentHierarchy);
		String displayText = createActionProviderDisplayText(hierarchyLeaf, detectedFields);
		InspectionAction inspectComponentAction = inspectionContext.createInspectComponentAction(componentHierarchy, subcomponentHierarchy);
		InspectionAction evaluateAsThisAction = inspectionContext.createEvaluateAsThisAction(hierarchyLeaf);
		return ActionProvider.of(displayText, inspectComponentAction, evaluateAsThisAction);
	}

	private String createActionProviderDisplayText(Object object, Collection<Field> fields) {
		String displayText = object.getClass().getSimpleName();
		if (displayText.isEmpty()) {
			displayText = object.getClass().getName();
		}
		if (fields.isEmpty()) {
			return displayText;
		}
		String fieldText = fields.stream()
			.map(field -> field.getDeclaringClass().getSimpleName() + "." + field.getName())
			.collect(Collectors.joining(", "));
		return Actions.trimName(displayText + " (" + fieldText + ")");
	}

	/*
	 * Tree View
	 */
	private JTree createLinearActionProviderTree(Iterable<ActionProvider> actionProviderHierarchy) {
		MutableTreeNode root = null;
		MutableTreeNode parent = null;
		for (ActionProvider actionProvider : actionProviderHierarchy) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(actionProvider);
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

		tree.addMouseListener(new ActionProviderTreeMouseListener(tree));
		tree.addMouseMotionListener(new ActionProviderTreeMouseMotionListener(tree));

		return tree;
	}

	private static class ActionProviderTreeMouseListener extends MouseAdapter
	{
		private final JTree	tree;

		private ActionProviderTreeMouseListener(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			assert e.getComponent() == tree;
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			ActionProvider actionProvider = getActionProvider(path);
			if (actionProvider == null) {
				return;
			}
			if (SwingUtilities.isLeftMouseButton(e)) {
				Actions.runDefaultAction(actionProvider);
			} else if (SwingUtilities.isRightMouseButton(e)) {
				Actions.showActionPopup(tree, actionProvider, e);
			}
		}

		private ActionProvider getActionProvider(TreePath path) {
			if (path == null) {
				return null;
			}
			Object node = path.getLastPathComponent();
			if (!(node instanceof DefaultMutableTreeNode)) {
				return null;
			}
			Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
			if (!(userObject instanceof ActionProvider)) {
				return null;
			}
			return (ActionProvider) userObject;
		}
	}

	private static class ActionProviderTreeMouseMotionListener extends MouseMotionAdapter
	{
		private final JTree	tree;

		private ActionProviderTreeMouseMotionListener(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			Cursor cursor = path == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			tree.setCursor(cursor);
		}
	}
}
