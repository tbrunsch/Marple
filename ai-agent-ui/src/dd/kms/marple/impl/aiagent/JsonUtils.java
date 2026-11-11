package dd.kms.marple.impl.aiagent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Access to members of JSON objects received from the agent or an MCP client. A missing member or
 * a JSON {@code null} yields a fallback value. A member of an unexpected type violates the protocol
 * and results in a {@link JsonParseException}.
 * <p>
 * The fallback objects and arrays are new instances because Gson's JSON elements are mutable.
 */
public class JsonUtils
{
	private static final int	MAX_ELEMENT_LENGTH_IN_MESSAGE	= 100;

	/**
	 * @return the object or a new empty object if the member does not exist
	 */
	public static JsonObject getObject(JsonObject object, String key) {
		JsonElement element = get(object, key);
		if (element == null) {
			return new JsonObject();
		}
		if (!element.isJsonObject()) {
			throw createTypeException(key, "an object", element);
		}
		return element.getAsJsonObject();
	}

	/**
	 * @return the elements of the array, which must be objects, or an empty list if the member does not exist
	 */
	public static List<JsonObject> getObjects(JsonObject object, String key) {
		JsonElement element = get(object, key);
		if (element == null) {
			return new ArrayList<>();
		}
		if (!element.isJsonArray()) {
			throw createTypeException(key, "an array", element);
		}
		JsonArray array = element.getAsJsonArray();
		List<JsonObject> objects = new ArrayList<>(array.size());
		for (JsonElement arrayElement : array) {
			if (!arrayElement.isJsonObject()) {
				throw createTypeException(key, "an array of objects", element);
			}
			objects.add(arrayElement.getAsJsonObject());
		}
		return objects;
	}

	/**
	 * @return the string representation of the primitive value or {@code null} if the member does not exist
	 */
	@Nullable
	public static String getString(JsonObject object, String key) {
		JsonElement element = get(object, key);
		if (element == null) {
			return null;
		}
		if (!element.isJsonPrimitive()) {
			throw createTypeException(key, "a string", element);
		}
		return element.getAsString();
	}

	public static boolean getBoolean(JsonObject object, String key, boolean defaultValue) {
		JsonElement element = get(object, key);
		if (element == null) {
			return defaultValue;
		}
		if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
			throw createTypeException(key, "a boolean", element);
		}
		return element.getAsBoolean();
	}

	@Nullable
	private static JsonElement get(JsonObject object, String key) {
		JsonElement element = object.get(key);
		return element == null || element.isJsonNull() ? null : element;
	}

	private static JsonParseException createTypeException(String key, String expectedType, JsonElement element) {
		String elementText = element.toString();
		if (elementText.length() > MAX_ELEMENT_LENGTH_IN_MESSAGE) {
			elementText = elementText.substring(0, MAX_ELEMENT_LENGTH_IN_MESSAGE) + "...";
		}
		return new JsonParseException("Expected " + expectedType + " for '" + key + "', but found " + elementText);
	}
}
