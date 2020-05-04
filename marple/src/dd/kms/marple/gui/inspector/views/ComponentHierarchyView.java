package dd.kms.marple.gui.inspector.views;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import dd.kms.marple.ComponentHierarchy;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.actions.Actions;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeNode;
import dd.kms.marple.settings.visual.ObjectView;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;

public class ComponentHierarchyView extends JPanel implements ObjectView
{
	private static final String	NAME	= "Component Hierarchy";

	private final JScrollPane		scrollPane;
	private final JTree				tree;

	private final InspectionContext	inspectionContext;

	public ComponentHierarchyView(ComponentHierarchy componentHierarchy, InspectionContext inspectionContext) {
		super(new GridBagLayout());
		this.inspectionContext = inspectionContext;

		setName(NAME);

		List<ActionProvider> actionProviderHierarchy = createActionProviderHierarchy(componentHierarchy);
		tree = createLinearActionProviderTree(actionProviderHierarchy);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
		int selectedRow = componentHierarchy.getSelectedIndex();
		tree.setSelectionInterval(selectedRow, selectedRow);

		scrollPane  = new JScrollPane(tree);
		add(scrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));
	}

	@Override
	public String getViewName() {
		return NAME;
	}

	@Override
	public JPanel getViewComponent() {
		return this;
	}

	@Override
	public Object getViewSettings() {
		/* currently there are no settings */
		return null;
	}

	@Override
	public void applyViewSettings(Object settings) {
		/* currently there are no settings */
	}

	private List<ActionProvider> createActionProviderHierarchy(ComponentHierarchy componentHierarchy) {
		Multimap<Object, Field> fieldsByObject = ArrayListMultimap.create();
		Set<Object> objectsToFind = new HashSet<>();

		List<Object> components = new ArrayList<>(componentHierarchy.getComponents());

		/*
		 * Traverse components from child to parent because usually children are members of parents and not vice versa
		 */
		for (Object hierarchyNode : Lists.reverse(components)) {
			objectsToFind.add(hierarchyNode);
			fieldsByObject.putAll(ReflectionUtils.findFieldValues(hierarchyNode, objectsToFind));
		}

		List<ActionProvider> actionProviderHierarchy = new ArrayList<>(components.size());
		for (int j = 0; j < components.size(); j++) {
			Object component = components.get(j);
			Collection<Field> fieldsForComponent = fieldsByObject.get(component);
			ActionProvider actionProvider = createActionProvider(new ComponentHierarchy(components, j), fieldsForComponent);
			actionProviderHierarchy.add(actionProvider);
		}
		return actionProviderHierarchy;
	}

	/*
	 * Action Providers
	 */
	private ActionProvider createActionProvider(ComponentHierarchy componentHierarchy, Collection<Field> detectedFields) {
		String displayText = createActionProviderDisplayText(componentHierarchy.getSelectedComponent(), detectedFields);
		return new ActionProviderBuilder(displayText, componentHierarchy, inspectionContext).build();
	}

	private String createActionProviderDisplayText(Object object, Collection<Field> fields) {
		String displayText = object.getClass().getSimpleName();
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
			MutableTreeNode node = new ComponentHierarchyTreeNode(actionProvider);
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

		ActionProviderListeners.addMouseListeners(tree);

		return tree;
	}

	private static class ComponentHierarchyTreeNode extends DefaultMutableTreeNode implements ActionProviderTreeNode
	{
		ComponentHierarchyTreeNode(ActionProvider actionProvider) {
			super(actionProvider);
		}

		@Override
		public ActionProvider getActionProvider() {
			return (ActionProvider) getUserObject();
		}
	}
}
