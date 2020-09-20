package dd.kms.marple.api.settings.visual;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface VisualSettingsBuilder
{
	static VisualSettingsBuilder create() {
		return new dd.kms.marple.impl.settings.visual.VisualSettingsBuilderImpl();
	}

	VisualSettingsBuilder nullDisplayText(String nullDisplayText);
	<T> VisualSettingsBuilder displayText(Class<T> objectClass, Function<T, String> displayTextFunction);

	/**
	 * {@code objectViewConstructor} may check further conditions whether the view is applicable and, if not, return null.
	 */
	VisualSettingsBuilder objectView(Class<?> objectClass, BiFunction<ObjectInfo, InspectionContext, ? extends ObjectView> objectViewConstructor);
	VisualSettings build();
}
