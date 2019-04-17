package dd.kms.marple.gui;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.marple.InspectionContext;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @param <V>	View class (GUI component class plus name)
 */
class VisualSettingsBuilderImpl<C, V> implements VisualSettingsBuilder<C, V>
{
	private String																		nullDisplayText			= "null";

	/*
	 * Since we want the user to add strategies without having to know which strategies
	 * are already defined, we cannot use ImmutableMap.Builder here. Otherwise, the user would get
	 * an exception if he specified a strategy for the same object class for which there is already
	 * a default strategy defined.
	 */
	private final Map<Class<?>, Function<Object, String>>								displayTextFunctions	= new HashMap<>();

	/*
	 * Since we want the user to add strategies without having to know which strategies
	 * are already defined, we cannot use ImmutableMap.Builder here. Otherwise, the user would get
	 * an exception if he specified a strategy for the same object class for which there is already
	 * a default strategy defined.
	 */
	private final Multimap<Class<?>, BiFunction<Object, InspectionContext<C, V>, V>>	objectViewConstructors	= ArrayListMultimap.create();

	@Override
	public VisualSettingsBuilder<C, V> nullDisplayText(String nullDisplayText) {
		this.nullDisplayText = nullDisplayText;
		return this;
	}

	@Override
	public <T> VisualSettingsBuilder<C, V> displayText(Class<T> objectClass, Function<T, String> displayTextFunction) {
		displayTextFunctions.put(
			objectClass,
			object -> objectClass.isInstance(object)
				? displayTextFunction.apply(objectClass.cast(object))
				: null
		);
		return this;
	}

	@Override
	public <T> VisualSettingsBuilder<C, V> objectView(Class<T> objectClass, BiFunction<T, InspectionContext<C, V>, ? extends V> objectViewConstructor) {
		objectViewConstructors.put(
			objectClass,
			(object, inspectionContext) -> objectClass.isInstance(object)
				? objectViewConstructor.apply(objectClass.cast(object), inspectionContext)
				: null
		);
		return this;
	}

	@Override
	public VisualSettings<C, V> build() {
		return new VisualSettingsImpl<>(nullDisplayText, displayTextFunctions, objectViewConstructors);
	}
}
