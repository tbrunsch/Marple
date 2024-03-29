package dd.kms.marple.impl.settings.visual;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.api.settings.visual.VisualSettings;
import dd.kms.marple.api.settings.visual.VisualSettingsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class VisualSettingsBuilderImpl implements VisualSettingsBuilder
{
	private String																			nullDisplayText			= "null";
	private final Map<Class<?>, Function<Object, String>>									displayTextFunctions	= new HashMap<>();
	private final Multimap<Class<?>, BiFunction<Object, InspectionContext, ObjectView>>		objectViewConstructors	= ArrayListMultimap.create();

	@Override
	public VisualSettingsBuilder nullDisplayText(String nullDisplayText) {
		this.nullDisplayText = nullDisplayText;
		return this;
	}

	@Override
	public <T> VisualSettingsBuilder displayText(Class<T> objectClass, Function<T, String> displayTextFunction) {
		displayTextFunctions.put(
			objectClass,
			object -> objectClass.isInstance(object)
				? displayTextFunction.apply(objectClass.cast(object))
				: null
		);
		return this;
	}

	@Override
	public VisualSettingsBuilder objectView(Class<?> objectClass, BiFunction<Object, InspectionContext, ? extends ObjectView> objectViewConstructor) {
		objectViewConstructors.put(
			objectClass,
			(object, context) -> objectClass.isInstance(object)
				? objectViewConstructor.apply(object, context)
				: null
		);
		return this;
	}

	@Override
	public VisualSettings build() {
		return new VisualSettingsImpl(nullDisplayText, displayTextFunctions, objectViewConstructors);
	}
}
