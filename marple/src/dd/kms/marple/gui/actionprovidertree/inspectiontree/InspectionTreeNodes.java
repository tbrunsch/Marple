package dd.kms.marple.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.Multimap;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.UniformView;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import javax.swing.tree.TreeModel;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class InspectionTreeNodes
{
	private static final int	COLLECTION_SIZE_THRESHOLD	= 100;

	public static TreeModel createModel(@Nullable String fieldName, Object object, InspectionContext inspectionContext) {
		TypeInfo typeInfo = object == null ? InfoProvider.NO_TYPE : InfoProvider.createTypeInfo(object.getClass());
		InspectionTreeNode treeNode = create(-1, fieldName, object, typeInfo, inspectionContext);
		return new InspectionTreeModel(treeNode);
	}

	static InspectionTreeNode create(int childIndex, @Nullable String displayKey, Object object, TypeInfo typeInfo, InspectionContext inspectionContext) {
		if (UniformView.canViewAsList(object)) {
			List<?> list = UniformView.asList(object);
			return new ListTreeNode(childIndex, displayKey, object, typeInfo, list, inspectionContext);
		}

		if (object instanceof Iterable<?>) {
			Iterable<?> iterable = (Iterable<?>) object;
			boolean tooLarge = (iterable instanceof Collection<?>) && ((Collection<?>) iterable).size() > COLLECTION_SIZE_THRESHOLD;
			if (!tooLarge) {
				return new IterableBasedObjectContainerTreeNode(childIndex, displayKey, object, typeInfo, iterable, null, inspectionContext);
			}
		}
		if (object instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) object;
			if (map.size() <= COLLECTION_SIZE_THRESHOLD) {
				return new IterableBasedObjectContainerTreeNode(childIndex, displayKey, object, typeInfo, map.keySet(), map::get, inspectionContext);
			}
		}
		if (object instanceof Multimap) {
			Multimap multimap = (Multimap) object;
			if (multimap.size() <= COLLECTION_SIZE_THRESHOLD) {
				return new IterableBasedObjectContainerTreeNode(childIndex, displayKey, object, typeInfo, multimap.keySet(), multimap::get, inspectionContext);
			}
		}
		return new DefaultObjectTreeNode(childIndex, displayKey, object, typeInfo, inspectionContext);
	}
}
