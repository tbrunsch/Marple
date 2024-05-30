package dd.kms.marple.impl.settings.visual;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.api.settings.visual.UniformView;
import dd.kms.marple.api.settings.visual.VisualSettings;
import dd.kms.marple.api.settings.visual.VisualSettingsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class VisualSettingsBuilderImpl implements VisualSettingsBuilder
{
	private String																			nullDisplayText					= "null";
	private final Map<Class<?>, Function<Object, String>>									displayTextFunctions			= new HashMap<>();
	private final List<UniformViewImpl.UniformViewData<List<?>>>							listViews						= new ArrayList<>();
	private final List<UniformViewImpl.UniformViewData<Iterable<?>>>						iterableViews					= new ArrayList<>();
	private final List<UniformViewImpl.UniformViewData<Map<?, ?>>>							mapViews						= new ArrayList<>();
	private final Multimap<Class<?>, BiFunction<Object, InspectionContext, ObjectView>>		objectViewConstructors			= ArrayListMultimap.create();

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
	public <T> VisualSettingsBuilder listView(Class<T> objectClass, Function<T, ? extends List<?>> listViewFunction, boolean preferredView) {
		listViews.add(new UniformViewImpl.UniformViewData<>(objectClass, listViewFunction, preferredView));
		return this;
	}

	@Override
	public <T> VisualSettingsBuilder iterableView(Class<T> objectClass, Function<T, ? extends Iterable<?>> iterableViewFunction, boolean preferredView) {
		iterableViews.add(new UniformViewImpl.UniformViewData<>(objectClass, iterableViewFunction, preferredView));
		return this;
	}

	@Override
	public <T> VisualSettingsBuilder mapView(Class<T> objectClass, Function<T, ? extends Map<?, ?>> mapViewFunction, boolean preferredView) {
		mapViews.add(new UniformViewImpl.UniformViewData<>(objectClass, mapViewFunction, preferredView));
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
		UniformView uniformView = new UniformViewImpl(listViews, iterableViews, mapViews);
		return new VisualSettingsImpl(nullDisplayText, displayTextFunctions, uniformView, objectViewConstructors);
	}
}
