package dd.kms.marple.settings.visual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.ReflectionUtils;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class VisualSettingsImpl implements VisualSettings
{
	private static final int	MAX_NUM_ARRAY_ELEMENTS_TO_DISPLAY	= 10;

	private final String																nullDisplayText;
	private final Map<Class<?>, Function<Object, String>>								displayTextFunctions;
	private final Multimap<Class<?>, BiFunction<Object, InspectionContext, ObjectView>>	objectViewConstructors;

	VisualSettingsImpl(String nullDisplayText, Map<Class<?>, Function<Object, String>> displayTextFunctions, Multimap<Class<?>, BiFunction<Object, InspectionContext, ObjectView>> objectViewConstructors) {
		this.nullDisplayText = nullDisplayText;
		this.displayTextFunctions = ImmutableMap.copyOf(displayTextFunctions);
		this.objectViewConstructors = ImmutableMultimap.copyOf(objectViewConstructors);
	}

	@Override
	public String getDisplayText(Object object) {
		if (object == null) {
			return nullDisplayText;
		}
		if (object.getClass().isArray()) {
			return getArrayDisplayText(object);
		}
		Class<?> objectClass = ReflectionUtils.getBestMatchingClass(object, displayTextFunctions.keySet());
		return objectClass == null
				? object.toString()
				: displayTextFunctions.get(objectClass).apply(object);
	}

	private String getArrayDisplayText(Object array) {
		int length = Array.getLength(array);
		if (length <= MAX_NUM_ARRAY_ELEMENTS_TO_DISPLAY) {
			return "[" + IntStream.range(0, length).mapToObj(i -> getDisplayText(Array.get(array, i))).collect(Collectors.joining(", ")) + "]";
		}
		return "[" + IntStream.range(0, MAX_NUM_ARRAY_ELEMENTS_TO_DISPLAY).mapToObj(i -> getDisplayText(Array.get(array, i))).collect(Collectors.joining(", ")) + ", ...]";
	}

	@Override
	public List<ObjectView> getInspectionViews(Object object, InspectionContext inspectionContext) {
		ImmutableList.Builder<ObjectView> viewsBuilder = ImmutableList.builder();
		for (Class<?> objectClass : objectViewConstructors.keySet()) {
			if (objectClass.isInstance(object)) {
				for (BiFunction<Object, InspectionContext, ObjectView> objectViewConstructor : objectViewConstructors.get(objectClass)) {
					ObjectView view = objectViewConstructor.apply(object, inspectionContext);
					if (view != null) {
						viewsBuilder.add(view);
					}
				}
			}
		}
		return viewsBuilder.build();
	}
}