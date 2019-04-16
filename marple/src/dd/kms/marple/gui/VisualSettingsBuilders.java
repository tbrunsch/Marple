package dd.kms.marple.gui;

public class VisualSettingsBuilders
{
	public static <C, V> VisualSettingsBuilder<C, V> createBuilder() {
		return new VisualSettingsBuilderImpl<>();
	}
}
