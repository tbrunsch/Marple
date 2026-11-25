package dd.kms.marple.impl.aiagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Loads text resources of this package.
 */
class Resources
{
	private Resources() {}

	/**
	 * Returns the text of the resource with line separators {@code "\n"} and without leading and trailing
	 * whitespace. A missing resource is a packaging error and results in an exception.
	 */
	static String loadText(String resourceName) {
		InputStream stream = Resources.class.getResourceAsStream(resourceName);
		if (stream == null) {
			throw new IllegalStateException("Resource '" + resourceName + "' not found");
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			// lines() removes line separators => line endings of the file do not matter
			return reader.lines().collect(Collectors.joining("\n")).trim();
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot read resource '" + resourceName + "'", e);
		} catch (UncheckedIOException e) {
			throw new UncheckedIOException("Cannot read resource '" + resourceName + "'", e.getCause());
		}
	}
}
