package dd.kms.marple.impl.common;

import com.google.common.collect.ImmutableList;

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

	public static boolean canViewAsList(Object object) {
		if (object == null) {
			return false;
		}
		return object instanceof List<?>
			|| object.getClass().isArray()
			|| getListReflectionData(object).isPresent();
	}

	public static List<?> asList(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Null cannot be converted to a list.");
		}

		if (object instanceof List<?>) {
			return (List<?>) object;
		}

		if (object.getClass().isArray()) {
			return getListViewOfArray(object);
		}

		Optional<ListReflectionData> listReflectionData = getListReflectionData(object);
		if (listReflectionData.isPresent()) {
			ListReflectionData data = listReflectionData.get();
			return getListViewOfListReflectionData(object, data);
		}

		throw new IllegalArgumentException("Object '" + object + "' cannot be converted to a List.");
	}

	private static List<?> getListViewOfArray(Object object) {
		return new AbstractList<Object>() {
			@Override
			public Object get(int index) {
				return Array.get(object, index);
			}

			@Override
			public int size() {
				return Array.getLength(object);
			}
		};
	}

	private static List<?> getListViewOfListReflectionData(Object object, ListReflectionData data) {
		return new AbstractList<Object>() {
			@Override
			public Object get(int index) {
				return data.getElement(object, index);
			}

			@Override
			public int size() {
				return data.getSize(object);
			}
		};
	}

	public static boolean canViewAsIterable(Object object) {
		return object instanceof Iterable<?> || canViewAsList(object);
	}

	public static Iterable<?> asIterable(Object object) {
		if (object instanceof Iterable<?>) {
			return (Iterable<?>) object;
		}
		if (canViewAsList(object)) {
			return asList(object);
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
			if (object == null) {
				return false;
			}
			for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
				for (Class<?> interf : clazz.getInterfaces()) {
					if (Objects.equals(interf.getName(), className)) {
						return true;
					}
				}
			}
			return false;
		}

		int getSize(Object object) {
			if (object == null) {
				return 0;
			}
			Method sizeGetterMethod;
			try {
				sizeGetterMethod = object.getClass().getMethod(sizeGetterName, new Class<?>[0]);
				sizeGetterMethod.setAccessible(true);
				return (Integer) sizeGetterMethod.invoke(object, new Object[0]);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				return 0;
			}
		}

		Object getElement(Object object, int index) {
			if (object == null) {
				return null;
			}
			Method elementAccessorMethod;
			try {
				elementAccessorMethod = object.getClass().getMethod(elementAccessorName, new Class<?>[] { int.class });
				elementAccessorMethod.setAccessible(true);
				return elementAccessorMethod.invoke(object, new Object[] { index });
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				return null;
			}
		}
	}
}
