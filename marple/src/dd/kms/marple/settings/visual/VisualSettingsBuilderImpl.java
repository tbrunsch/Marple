package dd.kms.marple.settings.visual;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.TypedObjectInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

class VisualSettingsBuilderImpl implements VisualSettingsBuilder
{
	private String																						nullDisplayText			= "null";

	/*
	 * Since we want the user to add strategies without having to know which strategies
	 * are already defined, we cannot use ImmutableMap.Builder here. Otherwise, the user would get
	 * an exception if he specified a strategy for the same object class for which there is already
	 * a default strategy defined.
	 */
	private final Map<Class<?>, Function<ObjectInfo, String>>											displayTextFunctions	= new HashMap<>();

	/*
	 * Since we want the user to add strategies without having to know which strategies
	 * are already defined, we cannot use ImmutableMap.Builder here. Otherwise, the user would get
	 * an exception if he specified a strategy for the same object class for which there is already
	 * a default strategy defined.
	 */
	private final Multimap<Class<?>, BiFunction<ObjectInfo, InspectionContext, ObjectView>>				objectViewConstructors	= ArrayListMultimap.create();

	@Override
	public VisualSettingsBuilder nullDisplayText(String nullDisplayText) {
		this.nullDisplayText = nullDisplayText;
		return this;
	}

	@Override
	public <T> VisualSettingsBuilder displayText(Class<T> objectClass, Function<TypedObjectInfo<T>, String> displayTextFunction) {
		displayTextFunctions.put(
			objectClass,
			objectInfo -> objectClass.isInstance(objectInfo.getObject())
				? displayTextFunction.apply(new TypedObjectInfo<>(objectInfo))
				: null
		);
		return this;
	}

	@Override
	public <T> VisualSettingsBuilder objectView(Class<T> objectClass, BiFunction<TypedObjectInfo<T>, InspectionContext, ? extends ObjectView> objectViewConstructor) {
		objectViewConstructors.put(
			objectClass,
			(objectInfo, inspectionContext) -> objectClass.isInstance(objectInfo.getObject())
				? objectViewConstructor.apply(new TypedObjectInfo<>(objectInfo), inspectionContext)
				: null
		);
		return this;
	}

	@Override
	public VisualSettings build() {
		return new VisualSettingsImpl(nullDisplayText, displayTextFunctions, objectViewConstructors);
	}
}
