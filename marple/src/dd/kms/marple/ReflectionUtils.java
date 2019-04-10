package dd.kms.marple;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.reflect.Field;
import java.util.*;

public class ReflectionUtils
{
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
	 * Returns a multimap mapping each value from the set {@code fieldValues} to all
	 * fields of the specified instance that are currently assigned that value.
	 */
	public static Multimap<Object, Field> findFieldValues(Object instance, Set<Object> fieldValues) {
		Multimap<Object, Field> fieldsByValue = ArrayListMultimap.create();
		if (instance == null) {
			return fieldsByValue;
		}
		List<Field> fields = dd.kms.zenodot.common.ReflectionUtils.getFields(instance.getClass(), false);
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
}
