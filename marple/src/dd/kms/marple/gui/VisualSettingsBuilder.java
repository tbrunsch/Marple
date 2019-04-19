package dd.kms.marple.gui;

import dd.kms.marple.InspectionContext;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @param <C>	GUI component class
 */
public interface VisualSettingsBuilder<C>
{
	VisualSettingsBuilder<C> nullDisplayText(String nullDisplayText);
	<T> VisualSettingsBuilder<C> displayText(Class<T> objectClass, Function<T, String> displayTextFunction);

	/**
	 * {@code objectViewConstructor} may check further conditions whether the view is applicable and, if not, return null.
	 */
	<T> VisualSettingsBuilder<C> objectView(Class<T> objectClass, BiFunction<T, InspectionContext<C>, ? extends ObjectView<C>> objectViewConstructor);
	VisualSettings<C> build();
}
