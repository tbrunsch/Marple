package dd.kms.marple.impl.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Primitives;
import dd.kms.zenodot.api.common.FieldScanner;
import dd.kms.zenodot.api.common.MethodScanner;
import dd.kms.zenodot.api.common.ObjectInfoProvider;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.lang.reflect.Field;
import java.util.*;

public class ReflectionUtils
{
	public static final ObjectInfoProvider OBJECT_INFO_PROVIDER	= new ObjectInfoProvider(true);

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
	 * Returns a multimap mapping each value from the set {@code fieldValues} to all
	 * fields of the specified instance that are currently assigned that value.
	 */
	public static Multimap<Object, Field> findFieldValues(Object instance, Set<Object> fieldValues) {
		Multimap<Object, Field> fieldsByValue = ArrayListMultimap.create();
		if (instance == null) {
			return fieldsByValue;
		}
		List<Field> fields = new FieldScanner().getFields(instance.getClass(), false);
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				Object fieldValue = field.get(instance);
				if (fieldValues.contains(fieldValue)) {
					fieldsByValue.put(fieldValue, field);
				}
			} catch (IllegalAccessException e) {
				/* do nothing */
			}
		}
		return fieldsByValue;
	}

	public static ExecutableInfo getUniqueMethodInfo(TypeInfo type, String methodName) {
		List<ExecutableInfo> methodInfos = InfoProvider.getMethodInfos(type, new MethodScanner().name(methodName));
		return Iterables.getOnlyElement(methodInfos);
	}

	public static TypeInfo getRuntimeTypeInfo(TypeInfo declaredType, Class<?> runtimeClass) {
		try {
			return declaredType.isPrimitive() ? declaredType : declaredType.getSubtype(runtimeClass);
		} catch (Exception e) {
			/*
			 * Sometimes exceptions like
			 *
			 *     javax.swing.plaf.basic.BasicComboBoxRenderer$UIResource does not appear to be a subtype of javax.swing.ListCellRenderer<? super E>
			 *
			 * are thrown. This seems to be incorrect and we handle it by ignoring the declared type.
			 *
			 * We also get an exception if the declared type has unresolved parameters.
			 */
			return InfoProvider.createTypeInfo(runtimeClass);
		}
	}

	public static TypeInfo getRuntimeTypeInfo(ObjectInfo objectInfo) {
		Object object = objectInfo.getObject();
		TypeInfo declaredType = objectInfo.getDeclaredType();
		return object == null ? declaredType : getRuntimeTypeInfo(declaredType, object.getClass());
	}
}
