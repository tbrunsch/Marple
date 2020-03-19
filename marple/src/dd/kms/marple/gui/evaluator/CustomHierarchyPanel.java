package dd.kms.marple.gui.evaluator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.ActionProviderBuilder;
import dd.kms.marple.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeNode;
import dd.kms.zenodot.settings.ObjectTreeNode;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

class CustomHierarchyPanel extends JPanel
{
	private static final String	INFO_TEXT		= "<html><p>To access a node with the path root -> node1 -> node2 -> ... -> node" +
		"during evaluation, write<br/>" +
		"<br/>" +
		"{node1#node2#...#node}" +
		"</p></html>";

	private final JScrollPane		scrollPane;
	private final JTree				tree;
	private final JLabel			infoLabel	= new JLabel(INFO_TEXT);

	CustomHierarchyPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		TreeNode root = new CustomHierarchyTreeNode(inspectionContext.getEvaluator().getParserSettings().getCustomHierarchyRoot(), null, inspectionContext);
		tree = new JTree(root);
		ActionProviderListeners.addMouseListeners(tree);

		scrollPane = new JScrollPane(tree);

		add(scrollPane,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.8, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(infoLabel,	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.2, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
	}

	private static class CustomHierarchyTreeNode implements TreeNode, ActionProviderTreeNode
	{
		private final ObjectTreeNode			wrappedNode;
		private final CustomHierarchyTreeNode	parent;
		private final InspectionContext			inspectionContext;
		private List<TreeNode> 					children			= null;

		CustomHierarchyTreeNode(ObjectTreeNode wrappedNode, CustomHierarchyTreeNode parent, InspectionContext inspectionContext) {
			this.wrappedNode = wrappedNode;
			this.parent = parent;
			this.inspectionContext = inspectionContext;
		}

		@Override
		public ActionProvider getActionProvider() {
			ActionProviderBuilder actionProviderBuilder = new ActionProviderBuilder(toString(), wrappedNode.getUserObject(), inspectionContext);
			if (!isRoot()) {
				actionProviderBuilder.evaluateAs(createExpression());
			}
			return actionProviderBuilder.build();
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return getChildren().get(childIndex);
		}

		@Override
		public int getChildCount() {
			return getChildren().size();
		}

		@Override
		public CustomHierarchyTreeNode getParent() {
			return parent;
		}

		@Override
		public int getIndex(TreeNode node) {
			return getChildren().indexOf(node);
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		public boolean isLeaf() {
			return getChildCount() == 0;
		}

		@Override
		public Enumeration children() {
			return Iterators.asEnumeration(getChildren().iterator());
		}

		@Override
		public String toString() {
			String name = wrappedNode.getName();
			return name == null ? "<no name>" : name;
		}

		private boolean isRoot() {
			return getParent() == null;
		}

		private List<TreeNode> getChildren() {
			if (children == null) {
				children = createChildren();
			}
			return children;
		}

		private List<TreeNode> createChildren() {
			return Lists.newArrayList(Iterables.transform(wrappedNode.getChildNodes(), node -> new CustomHierarchyTreeNode(node, this, inspectionContext)));
		}

		private String createExpression() {
			List<CustomHierarchyTreeNode> path = getPathToNode();
			return "{"
				+ path.stream()
					.map(CustomHierarchyTreeNode::toString)
					.collect(Collectors.joining("#"))
				+ "}";
		}

		private List<CustomHierarchyTreeNode> getPathToNode() {
			if (isRoot()) {
				return new ArrayList<>();
			} else {
				List<CustomHierarchyTreeNode> path = getParent().getPathToNode();	// mutable list by construction
				path.add(this);
				return path;
			}
		}
	}
}
