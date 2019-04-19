package dd.kms.marple.gui;

/**
 *
 * @param <C>	GUI component class
 */
public interface ObjectView<C>
{
	String getViewName();
	C getViewComponent();
	Object getViewSettings();
	void applyViewSettings(Object settings);
}
