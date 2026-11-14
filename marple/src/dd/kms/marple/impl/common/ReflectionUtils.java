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
		Set<Class<?>> result = new LinkedHashSet<>();
		for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			result.add(clazz);
		}
		result.addAll(getAllInterfaces(object.getClass()));
		return result;
	}

	/**
	 * Returns all interfaces the specified class implements, directly or indirectly: the interfaces of
	 * the class and of its superclasses as well as the interfaces these interfaces extend. In contrast
	 * to {@link Class#getInterfaces()}, which only returns the directly implemented interfaces, the
	 * result contains each interface only once. The interfaces closer to the class come first.
	 */
	public static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
		Set<Class<?>> interfaces = new LinkedHashSet<>();
		for (Class<?> curClass = clazz; curClass != null; curClass = curClass.getSuperclass()) {
			for (Class<?> implementedInterface : curClass.getInterfaces()) {
				addInterfaceWithSuperInterfaces(implementedInterface, interfaces);
			}
		}
		return interfaces;
	}

	private static void addInterfaceWithSuperInterfaces(Class<?> interfaceClass, Set<Class<?>> interfaces) {
		if (!interfaces.add(interfaceClass)) {
			// already added together with its super interfaces
			return;
		}
		for (Class<?> superInterface : interfaceClass.getInterfaces()) {
			addInterfaceWithSuperInterfaces(superInterface, interfaces);
		}
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
