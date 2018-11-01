package com.AMS.jBEAM.javaParser;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReflectionUtils
{
    /*
     * Type Conversions
     */
    private static final BiMap<Class<?>, Class<?>> 								PRIMITIVE_TO_BOXED_CLASS	= HashBiMap.create();
    private static final Table<Class<?>, Class<?>, Function<Object, Object>>	PRIMITIVE_CONVERSIONS 		= HashBasedTable.create();

    private static <S, T> void addConversion(Class<S> sourceClass, Class<T> targetClass, Function<S, T> conversion) {
    	PRIMITIVE_CONVERSIONS.put(sourceClass, targetClass, value -> conversion.apply((S) value));
	}

	private static <S> void addPrimitiveBoxedConversions(Class<S> primitiveClass, Class<S> boxedClass, Function<S, S> conversion) {
		addConversion(primitiveClass, boxedClass, conversion);
		addConversion(boxedClass, primitiveClass, conversion);
		PRIMITIVE_TO_BOXED_CLASS.put(primitiveClass, boxedClass);
	}

	private static <S, T> void addConversions(Class<S> primitiveSourceClass, Class<S> boxedSourceClass, Class<T> primitiveTargetClass, Class<T> boxedTargetClass, Function<S, T> conversion) {
		addConversion(primitiveSourceClass, primitiveTargetClass, 	conversion);
		addConversion(primitiveSourceClass, boxedTargetClass, 		conversion);
		addConversion(boxedSourceClass, 	primitiveTargetClass, 	conversion);
		addConversion(boxedSourceClass, 	boxedTargetClass, 		conversion);
	}

	public static Class<?> getBoxedClass(Class<?> primitiveClass) {
    	return PRIMITIVE_TO_BOXED_CLASS.get(primitiveClass);
	}

	public static Class<?> getPrimitiveClass(Class<?> boxedClass) {
    	return PRIMITIVE_TO_BOXED_CLASS.inverse().get(boxedClass);
	}

	public static boolean isPrimitiveConvertibleTo(Class<?> sourceClass, Class<?> targetClass) {
    	return PRIMITIVE_CONVERSIONS.contains(sourceClass, targetClass);
	}

	public static <T> T convertTo(Object value, Class<T> targetClass) {
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
		throw new ClassCastException("Cannot cast '" + sourceClass.getSimpleName() + "' to '" + targetClass + "'");
	}

    static {
		addPrimitiveBoxedConversions(byte.class, Byte.class, b -> b.byteValue());
		addConversions(byte.class, Byte.class, short.class, 	Short.class, 	b -> (short) 	b.byteValue());
		addConversions(byte.class, Byte.class, int.class, 		Integer.class, 	b -> (int) 		b.byteValue());
		addConversions(byte.class, Byte.class, long.class, 		Long.class, 	b -> (long) 	b.byteValue());
		addConversions(byte.class, Byte.class, float.class, 	Float.class, 	b -> (float) 	b.byteValue());
		addConversions(byte.class, Byte.class, double.class,	Double.class, 	b -> (double) 	b.byteValue());

		addPrimitiveBoxedConversions(short.class, Short.class, s -> s.shortValue());
		addConversions(short.class, Short.class, int.class,		Integer.class,	s -> (int) 		s.shortValue());
		addConversions(short.class, Short.class, long.class,	Long.class,		s -> (long) 	s.shortValue());
		addConversions(short.class, Short.class, float.class,	Float.class,	s -> (float) 	s.shortValue());
		addConversions(short.class, Short.class, double.class,	Double.class,	s -> (double) 	s.shortValue());

		addPrimitiveBoxedConversions(int.class, Integer.class, i -> i.intValue());
		addConversions(int.class, Integer.class, long.class,	Long.class,		i -> (long) 	i.intValue());
		addConversions(int.class, Integer.class, float.class,	Float.class,	i -> (float) 	i.intValue());
		addConversions(int.class, Integer.class, double.class,	Double.class,	i -> (double) 	i.intValue());

		addPrimitiveBoxedConversions(long.class, Long.class, l -> l.longValue());
		addConversions(long.class, Long.class, float.class,		Float.class,	l -> (float) 	l.longValue());
		addConversions(long.class, Long.class, double.class,	Double.class,	l -> (double) 	l.longValue());

		addPrimitiveBoxedConversions(float.class, Float.class, f -> f.floatValue());
		addConversions(float.class, Float.class, double.class,	Double.class,	f -> (double) 	f.floatValue());

		addPrimitiveBoxedConversions(double.class, Double.class, d -> d.doubleValue());

		addPrimitiveBoxedConversions(boolean.class, Boolean.class, b -> b.booleanValue());

		addPrimitiveBoxedConversions(char.class, Character.class, c -> c.charValue());
		addConversions(char.class, Character.class, int.class, 		Integer.class, 	c -> (int) 		c.charValue());
		addConversions(char.class, Character.class, long.class, 	Long.class, 	c -> (long) 	c.charValue());
		addConversions(char.class, Character.class, float.class, 	Float.class, 	c -> (float) 	c.charValue());
		addConversions(char.class, Character.class, double.class, 	Double.class, 	c -> (double) 	c.charValue());

		addPrimitiveBoxedConversions(void.class, Void.class, v -> null);
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
            List<Field> fields = getFields(instance.getClass());
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

    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> curClazz = clazz; curClazz != null; curClazz = curClazz.getSuperclass()) {
            List<Field> declaredFields = Arrays.stream(curClazz.getDeclaredFields()).filter(field -> !field.getName().startsWith("this$")).collect(Collectors.toList());
            // Sort fields because they are not guaranteed to be in any order
            Collections.sort(declaredFields, Comparator.comparing(field -> field.getName().toLowerCase()));
			fields.addAll(declaredFields);
        }
        return fields;
    }

    /*
     * Methods
     */
    public static List<Method> getMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> curClazz = clazz; curClazz != null; curClazz = curClazz.getSuperclass()) {
            List<Method> declaredMethods = Arrays.asList(curClazz.getDeclaredMethods());
            // Sort methods because they are not guaranteed to be in any order
			Collections.sort(declaredMethods, Comparator.comparing(method -> method.getName().toLowerCase()));
			methods.addAll(declaredMethods);
        }
        return methods;
    }
}
