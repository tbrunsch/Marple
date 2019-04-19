package dd.kms.marple.gui;

public class VisualSettingsBuilders
{
	public static <C> VisualSettingsBuilder<C> createBuilder() {
		return new VisualSettingsBuilderImpl<>();
	}
}
