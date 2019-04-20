package dd.kms.marple.swing.gui.actionprovidertree.inspectiontree;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.List;

public class InspectionTreeNodes
{
	public static TreeModel createModel(@Nullable String fieldName, Object object, InspectionContext<Component> inspectionContext) {
		TypeInfo typeInfo = object == null ? TypeInfo.NONE : TypeInfo.of(object.getClass());
		InspectionTreeNode treeNode = create(-1, fieldName, object, typeInfo, inspectionContext);
		return new InspectionTreeModel(treeNode);
	}

	static InspectionTreeNode create(int childIndex, @Nullable String fieldName, Object object, TypeInfo typeInfo, InspectionContext<Component> inspectionContext) {
		if (object instanceof List) {
			List<?> list = (List<?>) object;
			return new IndexedObjectContainerTreeNode(childIndex, fieldName, object, typeInfo, list::get, list.size(), inspectionContext);
		}
		if (object != null && object.getClass().isArray()) {
			return new IndexedObjectContainerTreeNode(childIndex, fieldName, object, typeInfo, i -> Array.get(object, i), Array.getLength(object), inspectionContext);
		}
		return new DefaultObjectTreeNode(childIndex, fieldName, object, typeInfo, inspectionContext);
	}
}
