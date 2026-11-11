package dd.kms.marple.impl.aiagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * The system prompt for the agent. Its text is stored in the resource file {@code SystemPrompt.md}.
 */
final class SystemPrompt
{
	private static final String	RESOURCE_NAME	= "SystemPrompt.md";

	static final String			TEXT			= loadText();

	private SystemPrompt() {}

	/**
	 * A missing resource is a packaging error. It is not ignored because the agent must not run without
	 * its instructions.
	 */
	private static String loadText() {
		InputStream stream = SystemPrompt.class.getResourceAsStream(RESOURCE_NAME);
		if (stream == null) {
			throw new IllegalStateException("Resource '" + RESOURCE_NAME + "' not found");
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			// lines() removes line separators => line endings of the file do not matter
			return reader.lines().collect(Collectors.joining("\n")).trim();
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot read resource '" + RESOURCE_NAME + "'", e);
		} catch (UncheckedIOException e) {
			throw new UncheckedIOException("Cannot read resource '" + RESOURCE_NAME + "'", e.getCause());
		}
	}
}
