package dd.kms.marple.api.settings.evaluation;

import dd.kms.marple.api.InspectionContext;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.swing.*;
import java.io.IOException;

public interface AdditionalEvaluationSettings
{
	/**
	 * If the settings are configurable and should be stored in the preferences, then this method should
	 * return the name of the XML element in which it stores its settings within the preferences file.
	 * If nothing has to be serialized, then this method should return {@code null}. Note that different
	 * implementations of this method must return different values (or {@code null}), so the return value
	 * should not be too generic.
	 */
	@Nullable
	String getXmlElementName();

	/**
	 * Writes the settings into the specified XML element of the preferences file. If no settings should
	 * be stored in the preferences file (in which case the method {@link #getXmlElementName()} should
	 * return {@code null}), then this method can be implemented empty or throw an {@link UnsupportedOperationException}.
	 */
	void writeSettings(Element settingsElement);

	/**
	 * Loads the stored settings from the specified XML element of the preferences file. If no settings
	 * are stored in the preferences file (in which case the method {@link #getXmlElementName()} should
	 * return {@code null}), then this method can be implemented empty or throw an {@link UnsupportedOperationException}.
	 */
	void readSettings(Element settingsElement) throws IOException;

	/**
	 * Applies the settings to the current context. This is necessary if an implementation of this interface
	 * explicitly holds the settings for displaying and changing them in the UI (see {@link #createSettingsPanel(InspectionContext)}),
	 * but not as part of the {@link InspectionContext}. This method triggers the transfer of these explicit settings
	 * to {@code InspectionContext}.
	 */
	void applySettings(InspectionContext context);

	/**
	 * Creates a panel in which the settings can be configured by the user.
	 */
	JPanel createSettingsPanel(InspectionContext context);
}
