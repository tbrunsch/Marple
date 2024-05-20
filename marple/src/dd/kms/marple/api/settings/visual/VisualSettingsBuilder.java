package dd.kms.marple.api.settings.visual;

import dd.kms.marple.api.InspectionContext;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface VisualSettingsBuilder
{
	static VisualSettingsBuilder create() {
		return new dd.kms.marple.impl.settings.visual.VisualSettingsBuilderImpl();
	}

	/**
	 * Specify the text that should be used for displaying {@code null}.
	 */
	VisualSettingsBuilder nullDisplayText(String nullDisplayText);

	/**
	 * Specify a formatter that is used for displaying objects of type {@code objectClass}.
	 */
	<T> VisualSettingsBuilder displayText(Class<T> objectClass, Function<T, String> displayTextFunction);

	/**
	 * Specify a function that maps objects of type {@code objectClass} to a {@link List}. With the flag
	 * {@code preferredView} you can specify whether this {@code List} view is the preferred view for
	 * such objects or not.
	 */
	<T> VisualSettingsBuilder listView(Class<T> objectClass, Function<T, ? extends List<?>> listViewFunction, boolean preferredView);

	/**
	 * Specify a function that maps objects of type {@code objectClass} to an {@link Iterable}. With the flag
	 * {@code preferredView} you can specify whether this {@code Iterable} view is the preferred view for
	 * such objects or not.
	 */
	<T> VisualSettingsBuilder iterableView(Class<T> objectClass, Function<T, ? extends Iterable<?>> iterableViewFunction, boolean preferredView);

	/**
	 * Specify a function that maps objects of type {@code objectClass} to a {@link Map}. With the flag
	 * {@code preferredView} you can specify whether this {@code Map} view is the preferred view for
	 * such objects or not.
	 */
	<T> VisualSettingsBuilder mapView(Class<T> objectClass, Function<T, ? extends Map<?, ?>> mapViewFunction, boolean preferredView);

	/**
	 * Specify a bi-function that provides an {@link ObjectView} for an object of class {@code objectClass}. An
	 * {@code ObjectView} represents a separate tab in the inspection window that provided a specific view on
	 * objects of a specific type.<br>
	 * <br>
	 * Note that the bi-function {@code objectViewConstructor} may check further conditions whether the view is
	 * applicable and, if not, return {@code null}.
	 */
	VisualSettingsBuilder objectView(Class<?> objectClass, BiFunction<Object, InspectionContext, ? extends ObjectView> objectViewConstructor);

	VisualSettings build();
}
