package dd.kms.marple.gui;

import dd.kms.marple.InspectionContext;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @param <C>	GUI component class
 * @param <V>	View class (GUI component class plus name)
 */
public interface VisualSettingsBuilder<C, V>
{
	VisualSettingsBuilder<C, V> nullDisplayText(String nullDisplayText);
	<T> VisualSettingsBuilder<C, V> displayText(Class<T> objectClass, Function<T, String> displayTextFunction);

	/**
	 * {@code objectViewConstructor} may check further conditions whether the view is applicable and, if not, return null.
	 */
	<T> VisualSettingsBuilder<C, V> objectView(Class<T> objectClass, BiFunction<T, InspectionContext<C, V>, ? extends V> objectViewConstructor);
	VisualSettings<C, V> build();
}
