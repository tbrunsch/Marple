package dd.kms.marple.swing.gui.actionprovidertree.inspectiontree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

public class InspectionTreeNodes
{
	private static final int	COLLECTION_SIZE_THRESHOLD	= 100;

	private static final ImmutableList.Builder<ListReflectionData>	LIST_REFLECTION_DATA_BUILDER = ImmutableList.builder();

	static {
		/*
		 * Apache Commons Collections Primitives Lists
		 */
		String sizeGetterName = "size";
		String elementAccessorName = "get";
		for (String dataTypeName : Arrays.asList("Byte", "Short", "Char", "Int", "Long", "Float", "Double")) {
			String className = "org.apache.commons.collections.primitives." + dataTypeName + "List";
			ListReflectionData listReflectionData = new ListReflectionData(className, sizeGetterName, elementAccessorName);
			LIST_REFLECTION_DATA_BUILDER.add(listReflectionData);
		}
	}

	private static final List<ListReflectionData>					LIST_REFLECTION_DATA = LIST_REFLECTION_DATA_BUILDER.build();

	public static TreeModel createModel(@Nullable String fieldName, Object object, InspectionContext<Component> inspectionContext) {
		TypeInfo typeInfo = object == null ? TypeInfo.NONE : TypeInfo.of(object.getClass());
		InspectionTreeNode treeNode = create(-1, fieldName, object, typeInfo, inspectionContext);
		return new InspectionTreeModel(treeNode);
	}

	static InspectionTreeNode create(int childIndex, @Nullable String displayKey, Object object, TypeInfo typeInfo, InspectionContext<Component> inspectionContext) {
		if (object instanceof List<?>) {
			List<?> list = (List<?>) object;
			return new IndexedObjectContainerTreeNode(childIndex, displayKey, object, typeInfo, list.size(), list::get, inspectionContext);
		}
		if (object != null && object.getClass().isArray()) {
			return new IndexedObjectContainerTreeNode(childIndex, displayKey, object, typeInfo, Array.getLength(object), i -> Array.get(object, i), inspectionContext);
		}
		Optional<ListReflectionData> optionalReflectionData = LIST_REFLECTION_DATA.stream()
			.filter(data -> data.isApplicable(object))
			.findFirst();
		if (optionalReflectionData.isPresent()) {
			ListReflectionData reflectionData = optionalReflectionData.get();
			return new IndexedObjectContainerTreeNode(childIndex, displayKey, object, typeInfo, reflectionData.getSize(object), i -> reflectionData.getElement(object, i), inspectionContext);
		}
		if (object instanceof Set<?>) {
			Set<?> set = (Set<?>) object;
			if (set.size() <= COLLECTION_SIZE_THRESHOLD) {
				return new SetBasedObjectContainerTreeNode(childIndex, displayKey, object, typeInfo, set, null, inspectionContext);
			}
		}
		if (object instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) object;
			if (map.size() <= COLLECTION_SIZE_THRESHOLD) {
				return new SetBasedObjectContainerTreeNode(childIndex, displayKey, object, typeInfo, map.keySet(), map::get, inspectionContext);
			}
		}
		if (object instanceof Multimap) {
			Multimap multimap = (Multimap) object;
			if (multimap.size() <= COLLECTION_SIZE_THRESHOLD) {
				return new SetBasedObjectContainerTreeNode(childIndex, displayKey, object, typeInfo, multimap.keySet(), multimap::get, inspectionContext);
			}
		}
		return new DefaultObjectTreeNode(childIndex, displayKey, object, typeInfo, inspectionContext);
	}

	private static class ListReflectionData
	{
		private final String	className;
		private final String	sizeGetterName;
		private final String	elementAccessorName;

		private ListReflectionData(String className, String sizeGetterName, String elementAccessorName) {
			this.className = className;
			this.sizeGetterName = sizeGetterName;
			this.elementAccessorName = elementAccessorName;
		}

		boolean isApplicable(Object object) {
			return object != null && Arrays.stream(object.getClass().getInterfaces())
				.anyMatch(i -> Objects.equals(i.getName(), className));
		}

		int getSize(Object object) {
			if (object == null) {
				return 0;
			}
			Method sizeGetterMethod = null;
			try {
				sizeGetterMethod = object.getClass().getMethod(sizeGetterName, new Class<?>[0]);
				return (Integer) sizeGetterMethod.invoke(object, new Object[0]);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				return 0;
			}
		}

		Object getElement(Object object, int index) {
			if (object == null) {
				return null;
			}
			Method elementAccessorMethod = null;
			try {
				elementAccessorMethod = object.getClass().getMethod(elementAccessorName, new Class<?>[] { int.class });
				return elementAccessorMethod.invoke(object, new Object[] { index });
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				return null;
			}
		}
	}
}
