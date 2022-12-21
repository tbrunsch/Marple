package dd.kms.marple.api.settings.visual;

import dd.kms.marple.api.gui.Disposable;

import javax.annotation.Nullable;
import java.awt.*;

public interface ObjectView extends Disposable
{
	String getViewName();
	Component getViewComponent();

	/**
	 * Returns all data that is required to restore the current state of the view
	 * at a later point in time. A view may return {@code null} if it does not have
	 * any settings that have to be restored later.
	 */
	@Nullable Object getViewSettings();

	/**
	 * Configures the current view according to the specified settings. The argument
	 * {@code origin} describes where the settings come from:
	 * <ul>
	 *     <li>
	 *         {@link ViewSettingsOrigin#SAME_CONTEXT}: This mode is used when restoring
	 * 	       a view for a context it had already been created for previously. In this
	 *         case, the whole settings should be applied.
	 *     </li>
	 *     <li>
	 *         {@link ViewSettingsOrigin#OTHER_CONTEXT}: This mode is used when switching
	 *         from one context to another. Here, we do not want to lose general settings.
	 *         Hence, only these parts of the specified settings will be transferred that
	 *         are applicable and reasonable to transfer.
	 *     </li>
	 * </ul>
	 * As a rule of thumb, context-specific settings should only be applied for
	 * {@link ViewSettingsOrigin#SAME_CONTEXT}, whereas general settings should
	 * always be applied.<br/>
	 * <br/>
	 * <b>Example:</b> Assume that the view displays the context in form of a tree.
	 * The information which nodes to expand should only be applied for the same context.
	 * If the view also allows to specify how the tree should be rendered, then these
	 * settings should be transferred in both cases.
	 */
	void applyViewSettings(@Nullable Object settings, ViewSettingsOrigin origin);

	enum ViewSettingsOrigin
	{
		SAME_CONTEXT,
		OTHER_CONTEXT
	}
}
