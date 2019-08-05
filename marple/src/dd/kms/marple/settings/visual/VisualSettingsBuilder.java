package dd.kms.marple.settings.visual;

import dd.kms.marple.InspectionContext;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface VisualSettingsBuilder
{
	VisualSettingsBuilder nullDisplayText(String nullDisplayText);
	<T> VisualSettingsBuilder displayText(Class<T> objectClass, Function<T, String> displayTextFunction);

	/**
	 * {@code objectViewConstructor} may check further conditions whether the view is applicable and, if not, return null.
	 */
	<T> VisualSettingsBuilder objectView(Class<T> objectClass, BiFunction<T, InspectionContext, ? extends ObjectView> objectViewConstructor);
	VisualSettings build();
}
