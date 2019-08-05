package dd.kms.marple.gui.inspector.views;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.components.ComponentHierarchyModels;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.marple.actions.Actions;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeMouseListener;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeMouseMotionListener;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ComponentHierarchyView extends JPanel implements ObjectView
{
	private static final String	NAME	= "Component Hierarchy";

	private final JScrollPane		scrollPane;
	private final JTree				tree;

	private final InspectionContext	inspectionContext;

	public ComponentHierarchyView(List<Component> componentHierarchy, List<?> subcomponentHierarchy, InspectionContext inspectionContext) {
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
		Object hierarchyLeaf = ComponentHierarchyModels.getHierarchyLeaf(componentHierarchy, subcomponentHierarchy);
		String displayText = createActionProviderDisplayText(hierarchyLeaf, detectedFields);
		return new ActionProviderBuilder(displayText, componentHierarchy, subcomponentHierarchy, inspectionContext).build();
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

		tree.addMouseListener(new ActionProviderTreeMouseListener());
		tree.addMouseMotionListener(new ActionProviderTreeMouseMotionListener());

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
