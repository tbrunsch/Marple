package dd.kms.marple.settings.visual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

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

	private final String																	nullDisplayText;
	private final Map<Class<?>, Function<ObjectInfo, String>>								displayTextFunctions;
	private final Multimap<Class<?>, BiFunction<ObjectInfo, InspectionContext, ObjectView>>	objectViewConstructors;

	VisualSettingsImpl(String nullDisplayText, Map<Class<?>, Function<ObjectInfo, String>> displayTextFunctions, Multimap<Class<?>, BiFunction<ObjectInfo, InspectionContext, ObjectView>> objectViewConstructors) {
		this.nullDisplayText = nullDisplayText;
		this.displayTextFunctions = ImmutableMap.copyOf(displayTextFunctions);
		this.objectViewConstructors = ImmutableMultimap.copyOf(objectViewConstructors);
	}

	@Override
	public String getDisplayText(ObjectInfo objectInfo) {
		Object object = objectInfo.getObject();
		if (object == null) {
			return nullDisplayText;
		}
		if (object.getClass().isArray()) {
			return getArrayDisplayText(objectInfo);
		}
		Class<?> objectClass = ReflectionUtils.getBestMatchingClass(object, displayTextFunctions.keySet());
		return objectClass == null
				? object.toString()
				: displayTextFunctions.get(objectClass).apply(objectInfo);
	}

	private String getArrayDisplayText(ObjectInfo arrayInfo) {
		Object array = arrayInfo.getObject();
		int length = Array.getLength(array);
		int numElementsToDisplay = Math.min(length, MAX_NUM_ARRAY_ELEMENTS_TO_DISPLAY);
		String displayText = "[" + IntStream.range(0, numElementsToDisplay).mapToObj(i -> getDisplayText(ReflectionUtils.OBJECT_INFO_PROVIDER.getArrayElementInfo(arrayInfo, InfoProvider.createObjectInfo(i)))).collect(Collectors.joining(", ")) + "]";
		return numElementsToDisplay < length ? displayText + ", ...]" : displayText;
	}

	@Override
	public List<ObjectView> getInspectionViews(ObjectInfo objectInfo, InspectionContext inspectionContext) {
		Object object = objectInfo.getObject();
		ImmutableList.Builder<ObjectView> viewsBuilder = ImmutableList.builder();
		for (Class<?> objectClass : objectViewConstructors.keySet()) {
			if (objectClass.isInstance(object)) {
				for (BiFunction<ObjectInfo, InspectionContext, ObjectView> objectViewConstructor : objectViewConstructors.get(objectClass)) {
					ObjectView view = objectViewConstructor.apply(objectInfo, inspectionContext);
					if (view != null) {
						viewsBuilder.add(view);
					}
				}
			}
		}
		return viewsBuilder.build();
	}
}
