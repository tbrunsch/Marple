package dd.kms.marple.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.Multimap;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.TypedObjectInfo;
import dd.kms.marple.common.UniformView;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import javax.swing.tree.TreeModel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InspectionTreeNodes
{
	private static final int	COLLECTION_SIZE_THRESHOLD	= 100;

	public static TreeModel createModel(@Nullable String fieldName, ObjectInfo objectInfo, boolean limitTreeSize, InspectionContext inspectionContext) {
		InspectionTreeNode treeNode = create(-1, fieldName, objectInfo, limitTreeSize, inspectionContext);
		return new InspectionTreeModel(treeNode);
	}

	static InspectionTreeNode create(int childIndex, @Nullable String displayKey, ObjectInfo objectInfo, boolean limitTreeSize, InspectionContext inspectionContext) {
		if (UniformView.canViewAsList(objectInfo)) {
			TypedObjectInfo<List<?>> listInfo = UniformView.asList(objectInfo);
			List<?> list = listInfo.getObject();
			return new ListTreeNode(childIndex, displayKey, objectInfo, list, inspectionContext);
		}

		Object object = objectInfo.getObject();
		if (Iterable.class.isInstance(object)) {
			Iterable<?> iterable = Iterable.class.cast(object);
			boolean tooLarge = limitTreeSize && (iterable instanceof Collection<?>) && ((Collection<?>) iterable).size() > COLLECTION_SIZE_THRESHOLD;
			if (!tooLarge) {
				return new IterableBasedObjectContainerTreeNode(childIndex, displayKey, objectInfo, iterable, null, inspectionContext);
			}
		}
		if (Map.class.isInstance(object)) {
			Map<?, ?> map = Map.class.cast(object);
			boolean tooLarge = limitTreeSize && map.size() > COLLECTION_SIZE_THRESHOLD;
			if (!tooLarge) {
				Set<?> keySet = map.keySet();
				return new IterableBasedObjectContainerTreeNode(childIndex, displayKey, objectInfo, keySet, map::get, inspectionContext);
			}
		}
		if (Multimap.class.isInstance(object)) {
			Multimap multimap = Multimap.class.cast(object);
			boolean tooLarge = limitTreeSize && multimap.size() > COLLECTION_SIZE_THRESHOLD;
			if (!tooLarge) {
				Set<?> keySet = multimap.keySet();
				return new IterableBasedObjectContainerTreeNode(childIndex, displayKey, objectInfo, keySet, multimap::get, inspectionContext);
			}
		}
		return new DefaultObjectTreeNode(childIndex, displayKey, objectInfo, inspectionContext);
	}
}
