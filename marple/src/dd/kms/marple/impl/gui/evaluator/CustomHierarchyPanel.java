package dd.kms.marple.impl.gui.evaluator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNode;
import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNodes;
import dd.kms.zenodot.api.settings.ObjectTreeNode;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
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

	CustomHierarchyPanel(InspectionContext context) {
		super(new GridBagLayout());

		TreeNode root = new CustomHierarchyTreeNode(context.getEvaluator().getParserSettings().getCustomHierarchyRoot(), null, context);
		tree = new JTree(root);

		ActionProviderListeners.addMouseListeners(tree);
		ActionProviderTreeNodes.enableFullTextToolTips(tree);

		scrollPane = new JScrollPane(tree);

		add(scrollPane,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.8, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(infoLabel,	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.2, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
	}

	private static class CustomHierarchyTreeNode implements TreeNode, ActionProviderTreeNode
	{
		private final ObjectTreeNode			wrappedNode;
		private final CustomHierarchyTreeNode	parent;
		private final InspectionContext			context;
		private List<TreeNode> 					children			= null;

		CustomHierarchyTreeNode(ObjectTreeNode wrappedNode, CustomHierarchyTreeNode parent, InspectionContext context) {
			this.wrappedNode = wrappedNode;
			this.parent = parent;
			this.context = context;
		}

		@Override
		public ActionProvider getActionProvider() {
			ActionProviderBuilder actionProviderBuilder = new ActionProviderBuilder(toString(), wrappedNode.getUserObject(), context);
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
			return getTrimmedText();
		}

		@Override
		public String getFullText() {
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
			return Lists.newArrayList(Iterables.transform(wrappedNode.getChildNodes(), node -> new CustomHierarchyTreeNode(node, this, context)));
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
