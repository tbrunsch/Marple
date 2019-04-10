package dd.kms.marple.settings;

public class InspectionSettingsBuilders
{
	/**
	 *
	 * @param <C>	GUI component class
	 * @param <K>	KeyStroke class
	 * @param <P>	Point class
	 */
	public static <C, K, P> InspectionSettingsBuilder<C, K, P> create(Class<C> componentClass) {
		return new InspectionSettingsBuilderImpl<C, K, P>(componentClass);
	}
}
