package com.AMS.jBEAM.common;

import com.google.common.collect.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReflectionUtils
{
	/*
	 * Type Conversions
	 */
	private static final BiMap<Class<?>, Class<?>>								PRIMITIVE_TO_BOXED_CLASS		= HashBiMap.create();
	private static final Table<Class<?>, Class<?>, Function<Object, Object>>	PRIMITIVE_CONVERSIONS 			= HashBasedTable.create();
	private static final Table<Class<?>, Class<?>, Function<Object, Object>>	PRIMITIVE_NARROWING_CONVERSIONS	= HashBasedTable.create();

	private static <S, T> void addConversion(Class<S> sourceClass, Class<T> targetClass, Function<S, T> conversion) {
		PRIMITIVE_CONVERSIONS.put(sourceClass, targetClass, value -> conversion.apply((S) value));
	}

	private static <T, S> void addNarrowingConversion(Class<T> sourceClass, Class<S> targetClass, Function<T, S> narrowingConversion) {
		PRIMITIVE_NARROWING_CONVERSIONS.put(sourceClass, targetClass, value -> narrowingConversion.apply((T) value));
	}

	private static <S> void addPrimitiveBoxedConversions(Class<S> primitiveClass, Class<S> boxedClass, Function<S, S> conversion) {
		addConversion(primitiveClass, boxedClass, conversion);
		addConversion(boxedClass, primitiveClass, conversion);
		PRIMITIVE_TO_BOXED_CLASS.put(primitiveClass, boxedClass);
	}

	private static <S, T> void addConversions(Class<S> primitiveSourceClass, Class<T> primitiveTargetClass, Function<S, T> conversion, Function<T, S> narrowingConversion) {
		Class<S> boxedSourceClass = (Class<S>) getBoxedClass(primitiveSourceClass);
		Class<T> boxedTargetClass = (Class<T>) getBoxedClass(primitiveTargetClass);
		addConversion(primitiveSourceClass, primitiveTargetClass, 	conversion);
		addConversion(primitiveSourceClass, boxedTargetClass, 		conversion);
		addConversion(boxedSourceClass, 	primitiveTargetClass, 	conversion);
		addConversion(boxedSourceClass, 	boxedTargetClass, 		conversion);
		addNarrowingConversion(primitiveTargetClass, 	primitiveSourceClass,	narrowingConversion);
		addNarrowingConversion(boxedTargetClass, 		primitiveSourceClass,	narrowingConversion);
		addNarrowingConversion(primitiveTargetClass, 	boxedSourceClass,		narrowingConversion);
		addNarrowingConversion(boxedTargetClass, 		boxedSourceClass,		narrowingConversion);
	}

	public static Class<?> getBoxedClass(Class<?> primitiveClass) {
		return PRIMITIVE_TO_BOXED_CLASS.get(primitiveClass);
	}

	public static Class<?> getPrimitiveClass(Class<?> boxedClass) {
		return PRIMITIVE_TO_BOXED_CLASS.inverse().get(boxedClass);
	}

	public static Set<Class<?>> getPrimitiveClasses() {
		return PRIMITIVE_TO_BOXED_CLASS.keySet();
	}

	public static boolean isPrimitiveConvertibleTo(Class<?> sourceClass, Class<?> targetClass, boolean allowNarrowing) {
		return PRIMITIVE_CONVERSIONS.contains(sourceClass, targetClass)
				|| (allowNarrowing && PRIMITIVE_NARROWING_CONVERSIONS.contains(sourceClass, targetClass));
	}

	public static <T> T convertTo(Object value, Class<T> targetClass, boolean allowNarrowing) {
		if (value == null) {
			return null;
		}
		if (targetClass.isInstance(value)) {
			return targetClass.cast(value);
		}
		Class<?> sourceClass = value.getClass();
		if (PRIMITIVE_CONVERSIONS.contains(sourceClass, targetClass)) {
			Function<Object, Object> conversion = PRIMITIVE_CONVERSIONS.get(sourceClass, targetClass);
			return (T) conversion.apply(value);
		}
		if (allowNarrowing && PRIMITIVE_NARROWING_CONVERSIONS.contains(sourceClass, targetClass)) {
			Function<Object, Object> conversion = PRIMITIVE_NARROWING_CONVERSIONS.get(sourceClass, targetClass);
			return (T) conversion.apply(value);
		}
		throw new ClassCastException("Cannot cast '" + sourceClass.getSimpleName() + "' to '" + targetClass + "'");
	}

	static {
		addPrimitiveBoxedConversions(byte.class,	Byte.class,			b -> b.byteValue());
		addPrimitiveBoxedConversions(short.class,	Short.class,		s -> s.shortValue());
		addPrimitiveBoxedConversions(int.class,		Integer.class,		i -> i.intValue());
		addPrimitiveBoxedConversions(long.class,	Long.class,			l -> l.longValue());
		addPrimitiveBoxedConversions(float.class,	Float.class,		f -> f.floatValue());
		addPrimitiveBoxedConversions(double.class,	Double.class,		d -> d.doubleValue());
		addPrimitiveBoxedConversions(boolean.class,	Boolean.class,		b -> b.booleanValue());
		addPrimitiveBoxedConversions(char.class,	Character.class,	c -> c.charValue());
		addPrimitiveBoxedConversions(void.class,	Void.class,			v -> null);

		addConversions(byte.class,	short.class, 	b -> b.shortValue(),	s -> s.byteValue());
		addConversions(byte.class,	int.class, 		b -> b.intValue(),		i -> i.byteValue());
		addConversions(byte.class,	long.class, 	b -> b.longValue(),		l -> l.byteValue());
		addConversions(byte.class,	float.class, 	b -> b.floatValue(),	f -> f.byteValue());
		addConversions(byte.class,	double.class,	b -> b.doubleValue(),	d -> d.byteValue());

		addConversions(short.class,	int.class,		s -> s.intValue(),		i -> i.shortValue());
		addConversions(short.class,	long.class,		s -> s.longValue(),		l -> l.shortValue());
		addConversions(short.class,	float.class,	s -> s.floatValue(),	f -> f.shortValue());
		addConversions(short.class,	double.class,	s -> s.doubleValue(),	d -> d.shortValue());

		addConversions(int.class,	long.class,		i -> i.longValue(),		l -> l.intValue());
		addConversions(int.class,	float.class,	i -> i.floatValue(),	f -> f.intValue());
		addConversions(int.class,	double.class,	i -> i.doubleValue(),	d -> d.intValue());

		addConversions(long.class,	float.class,	l -> l.floatValue(),	f -> f.longValue());
		addConversions(long.class,	double.class,	l -> l.doubleValue(),	d -> d.longValue());

		addConversions(float.class,	double.class,	f -> f.doubleValue(),	d -> d.floatValue());

		addConversions(char.class,	int.class, 		c -> (int) 		c.charValue(),	i -> (char) i.intValue());
		addConversions(char.class,	long.class, 	c -> (long) 	c.charValue(),	l -> (char) l.longValue());
		addConversions(char.class,	float.class, 	c -> (float) 	c.charValue(),	f -> (char) f.floatValue());
		addConversions(char.class,	double.class, 	c -> (double) 	c.charValue(),	d -> (char) d.doubleValue());
	}

	/*
	 * Classes
	 */
	public static Collection<Class<?>> getImplementedClasses(Object object, boolean includeInterfaces) {
		if (object == null) {
			return Collections.emptyList();
		}
		List<Class<?>> classesToExamine = new ArrayList<>();
		classesToExamine.add(object.getClass());
		Set<Class<?>> implementedClasses = new LinkedHashSet<>();
		while (!classesToExamine.isEmpty()) {
			Class<?> clazz = classesToExamine.get(0);
			classesToExamine.remove(0);
			if (implementedClasses.contains(clazz)) {
				continue;
			}
			implementedClasses.add(clazz);
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null) {
				classesToExamine.add(superClass);
			}
			if (includeInterfaces) {
				classesToExamine.addAll(Arrays.asList(clazz.getInterfaces()));
			}
		}
		return implementedClasses;
	}

	/*
	 * Fields
	 */

	/**
	 * Scans the class hierarchy of instance's class to find all fields
	 * whose values (for this instance) are one of the specified values.
	 * Returns a map field value -> all fields with this value.
	 */
	public static <T> Map<T, List<Field>> findFieldValues(Object instance, Set<T> fieldValues) {
		Map<T, List<Field>> fieldsByValue = new HashMap<>();
		if (instance != null) {
			List<Field> fields = getFields(instance.getClass(), false);
			for (Field field : fields) {
				try {
					field.setAccessible(true);
					Object fieldValue = field.get(instance);
					if (fieldValues.contains(fieldValue)) {
						if (!fieldsByValue.containsKey(fieldValue)) {
							fieldsByValue.put((T) fieldValue, new ArrayList<>());
						}
						fieldsByValue.get(fieldValue).add(field);
					}
				} catch (IllegalAccessException e) {
					/* do nothing */
				}
			}
		}
		return fieldsByValue;
	}

	public static List<Field> getFields(Class<?> clazz, boolean filterShadowedFields) {
		return getFields(clazz, filterShadowedFields, modifiers -> true);
	}

	public static List<Field> getFields(Class<?> clazz, boolean filterShadowedFields, Predicate<Integer> modifierFilter) {
		Set<String> encounteredFieldNames = new HashSet<>();
		List<Field> fields = new ArrayList<>();
		for (Class<?> curClazz = clazz; curClazz != null; curClazz = curClazz.getSuperclass()) {
			List<Field> declaredFields = Arrays.stream(curClazz.getDeclaredFields())
											.filter(field -> !field.getName().startsWith("this$"))
											.collect(Collectors.toList());
			// Sort fields because they are not guaranteed to be in any order
			Collections.sort(declaredFields, Comparator.comparing(field -> field.getName().toLowerCase()));

			for (Field field : Iterables.filter(declaredFields, field -> modifierFilter.test(field.getModifiers()))) {
				if (filterShadowedFields) {
					String fieldName = field.getName();
					if (encounteredFieldNames.contains(fieldName)) {
						continue;
					}
					encounteredFieldNames.add(fieldName);
				}
				fields.add(field);
			}
		}
		return fields;
	}

	/*
	 * Methods
	 */
	public static List<Method> getMethods(Class<?> clazz) {
		return getMethods(clazz, modifiers -> true);
	}

	public static List<Method> getMethods(Class<?> clazz, Predicate<Integer> modifierFilter) {
		Multimap<String, Class<?>[]> encounteredSignatures = ArrayListMultimap.create();

		List<Method> methods = new ArrayList<>();
		for (Class<?> curClazz = clazz; curClazz != null; curClazz = curClazz.getSuperclass()) {
			List<Method> declaredMethods = Arrays.asList(curClazz.getDeclaredMethods());
			// Sort methods because they are not guaranteed to be in any order
			Collections.sort(declaredMethods, Comparator.comparing(method -> method.getName().toLowerCase()));

			for (Method method : Iterables.filter(declaredMethods, method -> modifierFilter.test(method.getModifiers()))) {
				String methodName = method.getName();
				Collection<Class<?>[]> encounteredArgumentTypeCombinations = encounteredSignatures.get(methodName);
				Class<?>[] argumentTypes = method.getParameterTypes();
				boolean isOverriden = encounteredArgumentTypeCombinations.stream().anyMatch(types -> Arrays.equals(argumentTypes, types));
				if (isOverriden) {
					continue;
				}
				encounteredSignatures.put(methodName, argumentTypes);
				methods.add(method);
			}
		}
		return methods;
	}
}
