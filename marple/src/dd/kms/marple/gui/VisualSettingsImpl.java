package dd.kms.marple.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @param <C>	GUI component class
 * @param <V>	View class (GUI component class plus name)
 */
class VisualSettingsImpl<C, V> implements VisualSettings<C, V>
{
	private final String														nullDisplayText;
	private final Map<Class<?>, Function<Object, String>>						displayTextFunctions;
	private final Map<Class<?>, BiFunction<Object, InspectionContext<C, V>, V>>	objectViewConstructors;

	VisualSettingsImpl(String nullDisplayText, Map<Class<?>, Function<Object, String>> displayTextFunctions, Map<Class<?>, BiFunction<Object, InspectionContext<C, V>, V>> objectViewConstructors) {
		this.nullDisplayText = nullDisplayText;
		this.displayTextFunctions = ImmutableMap.copyOf(displayTextFunctions);
		this.objectViewConstructors = ImmutableMap.copyOf(objectViewConstructors);
	}

	@Override
	public String getDisplayText(Object object) {
		if (object == null) {
			return nullDisplayText;
		}
		Class<?> objectClass = ReflectionUtils.getBestMatchingClass(object, displayTextFunctions.keySet());
		return objectClass == null
				? object.toString()
				: displayTextFunctions.get(objectClass).apply(object);
	}

	@Override
	public List<V> getInspectionViews(Object object, InspectionContext<C, V> inspectionContext) {
		ImmutableList.Builder<V> viewsBuilder = ImmutableList.builder();
		for (Class<?> objectClass : objectViewConstructors.keySet()) {
			if (objectClass.isInstance(object)) {
				V view = objectViewConstructors.get(objectClass).apply(object, inspectionContext);
				viewsBuilder.add(view);
			}
		}
		return viewsBuilder.build();
	}
}
