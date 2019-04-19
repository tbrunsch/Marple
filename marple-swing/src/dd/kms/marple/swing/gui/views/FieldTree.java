package dd.kms.marple.swing.gui.views;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.InspectionAction;
import dd.kms.marple.swing.gui.Actions;
import dd.kms.marple.swing.gui.actionprovidertree.ActionProviderTreeMouseListener;
import dd.kms.marple.swing.gui.actionprovidertree.ActionProviderTreeMouseMotionListener;
import dd.kms.marple.swing.gui.actionprovidertree.ActionProviderTreeNode;
import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class FieldTree extends JPanel
{
	private final JTree			tree			= new JTree();
	private final JScrollPane	treeScrollPane	= new JScrollPane(tree);

	public FieldTree(Object object, InspectionContext<Component> inspectionContext) {
		super(new GridBagLayout());

		TreeModel model = new FieldTreeModel(object, inspectionContext);
		tree.setModel(model);

		add(treeScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		tree.addMouseListener(new ActionProviderTreeMouseListener());
		tree.addMouseMotionListener(new ActionProviderTreeMouseMotionListener());
	}

	private static class FieldTreeModel implements TreeModel
	{
		private final Object										rootObject;
		private final InspectionContext<Component>					inspectionContext;
		private final LoadingCache<Object, List<ObjectTreeNode>>	childCache;

		private FieldTreeModel(Object rootObject, InspectionContext<Component> inspectionContext) {
			this.rootObject = rootObject;
			this.inspectionContext = inspectionContext;
			this.childCache = CacheBuilder.newBuilder()
				.maximumSize(100)
				.build(CacheLoader.from(this::createChildList));
		}

		@Override
		public Object getRoot() {
			TypeInfo typeInfo = rootObject == null ? null : TypeInfo.of(rootObject.getClass());
			return createNode(null, rootObject, typeInfo, 0);
		}

		@Override
		public Object getChild(Object parent, int index) {
			List<ObjectTreeNode> children = getChildren(parent);
			return 0 <= index && index < children.size() ? children.get(index) : null;
		}

		@Override
		public int getChildCount(Object parent) {
			return getChildren(parent).size();
		}

		@Override
		public boolean isLeaf(Object node) {
			return getChildCount(node) == 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			return child instanceof ObjectTreeNode ? ((ObjectTreeNode) child).getChildIndex() : -1;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			/* do nothing */
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			/* do nothing */
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			/* do nothing */
		}

		private ObjectTreeNode createNode(String key, Object object, TypeInfo typeInfo, int childIndex) {
			return new ObjectTreeNode(key, object, typeInfo, childIndex, inspectionContext);
		}

		private List<ObjectTreeNode> getChildren(Object parent) {
			return childCache.getUnchecked(parent);
		}

		private List<ObjectTreeNode> createChildList(Object parent) {
			if (parent instanceof ObjectTreeNode) {
				ObjectTreeNode parentNode = (ObjectTreeNode) parent;
				Object object = parentNode.getUserObject();
				TypeInfo typeInfo = parentNode.getTypeInfo();
				if (object == null || typeInfo.isPrimitive()) {
					return Collections.emptyList();
				}
				List<Field> fields = ReflectionUtils.getFields(object.getClass(), false);
				ImmutableList.Builder<ObjectTreeNode> childBuilder = ImmutableList.builder();
				int childIndex = 0;
				for (int i = 0; i < fields.size(); i++) {
					Field field = fields.get(i);
					field.setAccessible(true);
					Object fieldValue;
					try {
						fieldValue = field.get(object);
					} catch (IllegalAccessException e) {
						System.err.println(e.getMessage());
						fieldValue = null;
					}
					TypeInfo childTypeInfo = typeInfo.resolveType(field.getType());
					ObjectTreeNode child = createNode(field.getName(), fieldValue, childTypeInfo, childIndex++);
					childBuilder.add(child);
				}
				return childBuilder.build();
			}
			return Collections.emptyList();
		}
	}

	private static class ObjectTreeNode extends DefaultMutableTreeNode implements ActionProviderTreeNode
	{
		private final String						key;
		private final TypeInfo						typeInfo;
		private final int							childIndex;
		private final InspectionContext<Component>	inspectionContext;

		private ObjectTreeNode(String key, Object value, TypeInfo typeInfo, int childIndex, InspectionContext<Component> inspectionContext) {
			super(value);
			this.key = key;
			this.typeInfo = typeInfo;
			this.childIndex = childIndex;
			this.inspectionContext = inspectionContext;
		}

		TypeInfo getTypeInfo() {
			return typeInfo;
		}

		int getChildIndex() {
			return childIndex;
		}

		@Override
		public ActionProvider getActionProvider() {
			Object object = getUserObject();
			if (object == null) {
				return null;
			}
			InspectionAction inspectObjectAction = inspectionContext.createInspectObjectAction(object);
			InspectionAction highlightComponentAction = object instanceof Component
														? inspectionContext.createHighlightComponentAction((Component) object)
														: null;
			InspectionAction evaluateAsThisAction = inspectionContext.createEvaluateAsThisAction(object);
			return ActionProvider.of(toString(), inspectObjectAction, highlightComponentAction, evaluateAsThisAction);
		}

		@Override
		public String toString() {
			String valueDisplayText = inspectionContext.getDisplayText(getUserObject());
			String fullNodeDisplayText = key == null
					? valueDisplayText + " (" + typeInfo.getType() + ")"
					: key + " = " + valueDisplayText;
			return Actions.trimName(fullNodeDisplayText);
		}
	}
}
