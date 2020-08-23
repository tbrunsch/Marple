package dd.kms.marple.common;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class UniformView
{
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

	private static final List<ListReflectionData> LIST_REFLECTION_DATA = LIST_REFLECTION_DATA_BUILDER.build();

	public static boolean canViewAsList(ObjectInfo objectInfo) {
		Object object = objectInfo.getObject();
		if (object == null) {
			return false;
		}
		return object instanceof List<?>
			|| object.getClass().isArray()
			|| getListReflectionData(object).isPresent();
	}

	public static TypedObjectInfo<List<?>> asList(ObjectInfo objectInfo) {
		Object object = objectInfo.getObject();
		if (object == null) {
			throw new IllegalArgumentException("Null cannot be converted to a list.");
		}

		if (object instanceof List<?>) {
			return new TypedObjectInfo<>(objectInfo);
		}

		if (object.getClass().isArray()) {
			return getListViewOfArray(objectInfo);
		}

		Optional<ListReflectionData> listReflectionData = getListReflectionData(object);
		if (listReflectionData.isPresent()) {
			ListReflectionData data = listReflectionData.get();
			return getListViewOfListReflectionData(objectInfo, data);
		}

		throw new IllegalArgumentException("Object '" + object + "' cannot be converted to a List.");
	}

	private static TypedObjectInfo<List<?>> getListViewOfArray(ObjectInfo objectInfo) {
		Object object = objectInfo.getObject();
		AbstractList<Object> listView = new AbstractList<Object>() {
			@Override
			public Object get(int index) {
				return Array.get(object, index);
			}

			@Override
			public int size() {
				return Array.getLength(object);
			}
		};
		return new TypedObjectInfo<>(InfoProvider.createObjectInfo(listView));
	}

	private static TypedObjectInfo<List<?>> getListViewOfListReflectionData(ObjectInfo objectInfo, ListReflectionData data) {
		Object object = objectInfo.getObject();
		List<Object> listView = new AbstractList<Object>() {
			@Override
			public Object get(int index) {
				return data.getElement(object, index);
			}

			@Override
			public int size() {
				return data.getSize(object);
			}
		};
		return new TypedObjectInfo<>(InfoProvider.createObjectInfo(listView));
	}

	public static boolean canViewAsIterable(ObjectInfo objectInfo) {
		Object object = objectInfo.getObject();
		return object instanceof Iterable<?> || canViewAsList(objectInfo);
	}

	public static TypedObjectInfo<? extends Iterable<?>> asIterable(ObjectInfo objectInfo) {
		Object object = objectInfo.getObject();
		if (object instanceof Iterable<?>) {
			return new TypedObjectInfo<>(objectInfo);
		}
		if (canViewAsList(objectInfo)) {
			return asList(objectInfo);
		}
		throw new IllegalArgumentException("Object '" + object + "' cannot be converted to an Iterable.");
	}

	private static Optional<ListReflectionData> getListReflectionData(Object object) {
		if (object == null) {
			return Optional.empty();
		}
		return LIST_REFLECTION_DATA.stream()
			.filter(data -> data.isApplicable(object))
			.findFirst();
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
