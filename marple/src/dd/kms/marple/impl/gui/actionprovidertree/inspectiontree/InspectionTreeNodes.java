package dd.kms.marple.impl.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.Multimap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.common.TypedObjectInfo;
import dd.kms.marple.impl.common.UniformView;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import javax.swing.tree.TreeModel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InspectionTreeNodes
{
	private static final int	COLLECTION_SIZE_THRESHOLD	= 100;

	public static TreeModel createModel(@Nullable String fieldName, ObjectInfo objectInfo, boolean limitTreeSize, InspectionContext context) {
		InspectionTreeNode treeNode = create(-1, fieldName, objectInfo, limitTreeSize, context);
		return new InspectionTreeModel(treeNode);
	}

	static InspectionTreeNode create(int childIndex, @Nullable String displayKey, ObjectInfo objectInfo, boolean limitTreeSize, InspectionContext context) {
		if (UniformView.canViewAsList(objectInfo)) {
			TypedObjectInfo<List<?>> listInfo = UniformView.asList(objectInfo);
			List<?> list = listInfo.getObject();
			return new ListTreeNode(childIndex, displayKey, objectInfo, list, context);
		}

		Object object = objectInfo.getObject();
		if (Iterable.class.isInstance(object)) {
			Iterable<?> iterable = Iterable.class.cast(object);
			boolean tooLarge = limitTreeSize && (iterable instanceof Collection<?>) && ((Collection<?>) iterable).size() > COLLECTION_SIZE_THRESHOLD;
			if (!tooLarge) {
				return new IterableBasedObjectContainerTreeNode(childIndex, displayKey, objectInfo, iterable, null, context);
			}
		}
		if (Map.class.isInstance(object)) {
			Map<?, ?> map = Map.class.cast(object);
			boolean tooLarge = limitTreeSize && map.size() > COLLECTION_SIZE_THRESHOLD;
			if (!tooLarge) {
				Set<?> keySet = map.keySet();
				return new IterableBasedObjectContainerTreeNode(childIndex, displayKey, objectInfo, keySet, map::get, context);
			}
		}
		if (Multimap.class.isInstance(object)) {
			Multimap multimap = Multimap.class.cast(object);
			boolean tooLarge = limitTreeSize && multimap.size() > COLLECTION_SIZE_THRESHOLD;
			if (!tooLarge) {
				Set<?> keySet = multimap.keySet();
				return new IterableBasedObjectContainerTreeNode(childIndex, displayKey, objectInfo, keySet, multimap::get, context);
			}
		}
		return new DefaultObjectTreeNode(childIndex, displayKey, objectInfo, context);
	}
}
