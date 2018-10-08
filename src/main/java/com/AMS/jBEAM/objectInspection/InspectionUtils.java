package com.AMS.jBEAM.objectInspection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Utility class for framework independent object inspection
 */
public class InspectionUtils
{
    /*
     * For Implementing Inspection Strategies
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
            Field[] declaredFields = curClazz.getDeclaredFields();
            for (Field field : declaredFields) {
                int modifiers = field.getModifiers();
                // We are currently not interested in static fields
                if ((modifiers & Modifier.STATIC) == 0) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    public static String formatField(Field field) {
        Class<?> declaringClass = field.getDeclaringClass();
        String declaringClassName = declaringClass.getSimpleName();
        String fieldName = field.getName();
        return declaringClassName + "." + fieldName;
    }
}
