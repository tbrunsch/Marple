package dd.kms.marple.settings;

public class InspectionSettingsBuilders
{
	/**
	 *
	 * @param <C>	GUI component class
	 * @param <V>	View class (GUI component class plus name)
	 * @param <K>	KeyStroke class
	 * @param <P>	Point class
	 */
	public static <C, V, K, P> InspectionSettingsBuilder<C, V, K, P> create(Class<C> componentClass) {
		return new InspectionSettingsBuilderImpl<>(componentClass);
	}
}
