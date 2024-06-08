package dd.kms.marple.impl.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Primitives;
import dd.kms.zenodot.api.common.AccessDeniedException;
import dd.kms.zenodot.api.common.FieldScannerBuilder;
import dd.kms.zenodot.api.common.GeneralizedField;
import dd.kms.zenodot.api.common.StaticMode;

import java.util.*;

public class ReflectionUtils
{
	public static final Object	NOT_ACCESSIBLE							= "Access denied";
	private static final int	MAX_NUM_ITERABLE_ELEMENTS_TO_CONSIDER	= 1000;

	/**
	 * We consider an object worth being inspected if it is
	 * <ul>
	 *     <li>neither null</li>
	 *     <li>nor (wrapped) primitive</li>
	 *     <li>nor a String.</li>
	 * </ul>
	 */
	public static boolean isObjectInspectable(Object object) {
		if (object == null) {
			return false;
		}
		Class<?> clazz = object.getClass();
		return !Primitives.unwrap(clazz).isPrimitive() && clazz != String.class;
	}

	public static <T> Class<? extends T> getBestMatchingClass(Object object, Iterable<Class<? extends T>> classes) {
		if (object == null) {
			return null;
		}
		Class<? extends T> bestClass = null;
		for (Class<? extends T> clazz : classes) {
			if (!clazz.isInstance(object)) {
				continue;
			}
			if (bestClass == null || bestClass.isAssignableFrom(clazz)) {
				bestClass = clazz;
			}
		}
		return bestClass;
	}

	public static Class<?> getCommonSuperClass(Iterable<?> elements) {
		elements = Iterables.limit(elements, MAX_NUM_ITERABLE_ELEMENTS_TO_CONSIDER);
		Set<Class<?>> commonClassCandidates = null;
		Object firstNonNullElement = null;
		for (Object element : elements) {
			if (element == null) {
				continue;
			}
			if (firstNonNullElement == null) {
				firstNonNullElement = element;
				commonClassCandidates = getSuperClassesAndInterfaces(element);
			} else {
				commonClassCandidates.removeIf(clazz -> !clazz.isInstance(element));
			}
		}
		return commonClassCandidates == null ? Object.class : getBestMatchingClass(firstNonNullElement, commonClassCandidates);
	}

	private static Set<Class<?>> getSuperClassesAndInterfaces(Object object) {
		List<Class<?>> superClasses = new ArrayList<>();
		Set<Class<?>> superInterfaces = new LinkedHashSet<>();
		for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			superClasses.add(clazz);
			superInterfaces.addAll(Arrays.asList(clazz.getInterfaces()));
		}
		Set<Class<?>> result = new LinkedHashSet<>();
		result.addAll(superClasses);
		result.addAll(superInterfaces);
		return result;
	}

	/**
	 * @return The value of the field or {@link #NOT_ACCESSIBLE} if the field is not accessible.
	 */
	public static Object getFieldValue(GeneralizedField field, Object object) {
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (Exception e) {
			return NOT_ACCESSIBLE;
		}
	}

	/**
	 * Returns a multimap mapping each value from the set {@code fieldValues} to all
	 * fields of the specified instance that are currently assigned that value.
	 */
	public static Multimap<Object, GeneralizedField> findFieldValues(Object instance, Set<Object> fieldValues) {
		Multimap<Object, GeneralizedField> fieldsByValue = ArrayListMultimap.create();
		if (instance == null) {
			return fieldsByValue;
		}
		List<GeneralizedField> fields = FieldScannerBuilder.create()
			.ignoreShadowedFields(false)
			.staticMode(StaticMode.NON_STATIC)
			.build()
			.getFields(instance.getClass());
		for (GeneralizedField field : fields) {
			try {
				field.setAccessible(true);
				Object fieldValue = field.get(instance);
				if (fieldValues.contains(fieldValue)) {
					fieldsByValue.put(fieldValue, field);
				}
			} catch (AccessDeniedException | IllegalAccessException e) {
				/* do nothing */
			}
		}
		return fieldsByValue;
	}
}
