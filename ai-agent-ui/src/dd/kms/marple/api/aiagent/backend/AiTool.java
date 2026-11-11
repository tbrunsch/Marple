package dd.kms.marple.api.aiagent.backend;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dd.kms.marple.impl.aiagent.EventDispatchThreadUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * A tool the agent can call (via the MCP server or directly, see {@link AiAgentEnvironment})
 */
public final class AiTool
{
	/**
	 * @param implementation	executes the tool (see {@link #call(JsonObject)})
	 */
	public static Builder builder(String name, String description, Implementation implementation) {
		return new Builder(name, description, implementation);
	}

	private static final int		MAX_ERROR_LENGTH	= 1000;

	private final String			name;
	private final String			description;
	private final JsonObject		inputSchema;
	private final Implementation	implementation;
	private final boolean			runInEventDispatchThread;

	AiTool(String name, String description, JsonObject inputSchema, Implementation implementation, boolean runInEventDispatchThread) {
		this.name = name;
		this.description = description;
		this.inputSchema = inputSchema;
		this.implementation = implementation;
		this.runInEventDispatchThread = runInEventDispatchThread;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * JSON schema of the tool's arguments
	 */
	public JsonObject getInputSchema() {
		return inputSchema.deepCopy();
	}

	/**
	 * Executes the tool. This method may be called from any thread. If the tool has been configured to
	 * run in the event dispatch thread (see {@link Builder#runInEventDispatchThread(boolean)}), then the
	 * implementation is executed there and this method blocks until it has finished.<br>
	 * <br>
	 * This method does not throw exceptions: Exceptions thrown by the implementation are returned as
	 * error result.
	 */
	public AiToolResult call(JsonObject arguments) {
		if (!runInEventDispatchThread) {
			return callSafely(arguments);
		}
		try {
			return EventDispatchThreadUtils.callAndWait(() -> callSafely(arguments));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return AiToolResult.error("Interrupted");
		} catch (InvocationTargetException e) {
			// should not happen because callSafely() does not throw exceptions
			return createErrorResult(e.getCause());
		}
	}

	private AiToolResult callSafely(JsonObject arguments) {
		try {
			return implementation.call(arguments);
		} catch (Throwable t) {
			return createErrorResult(t);
		}
	}

	/**
	 * Returns the exception's class and message, which help the agent to correct its call. The text is
	 * truncated because some messages contain large object representations.
	 */
	private static AiToolResult createErrorResult(Throwable t) {
		String text = String.valueOf(t);
		if (text.length() > MAX_ERROR_LENGTH) {
			text = text.substring(0, MAX_ERROR_LENGTH) + "... (truncated)";
		}
		return AiToolResult.error(text);
	}

	/**
	 * Executes a tool
	 */
	@FunctionalInterface
	public interface Implementation
	{
		/**
		 * @param arguments	the arguments as specified by the tool's input schema
		 * @return the result of the tool
		 * @throws Exception if the tool fails. The exception is reported to the agent as error result.
		 */
		AiToolResult call(JsonObject arguments) throws Exception;
	}

	public static final class Builder
	{
		private final String			name;
		private final String			description;
		private final Implementation	implementation;
		private final JsonObject		properties					= new JsonObject();
		private final JsonArray			required					= new JsonArray();
		private boolean					runInEventDispatchThread	= false;

		Builder(String name, String description, Implementation implementation) {
			this.name = name;
			this.description = description;
			this.implementation = implementation;
		}

		public Builder parameter(String parameterName, String type, String parameterDescription, boolean isRequired) {
			JsonObject propertySchema = new JsonObject();
			propertySchema.addProperty("type", type);
			propertySchema.addProperty("description", parameterDescription);
			return parameter(parameterName, propertySchema, isRequired);
		}

		/**
		 * Adds a string parameter whose allowed values are the names of the enum constants (see {@link Enum#name()}).
		 */
		public <E extends Enum<E>> Builder parameter(String parameterName, Class<E> enumClass, String parameterDescription, boolean isRequired) {
			JsonArray values = new JsonArray();
			for (E constant : enumClass.getEnumConstants()) {
				values.add(constant.name());
			}
			JsonObject propertySchema = new JsonObject();
			propertySchema.addProperty("type", "string");
			propertySchema.add("enum", values);
			propertySchema.addProperty("description", parameterDescription);
			return parameter(parameterName, propertySchema, isRequired);
		}

		public Builder parameter(String parameterName, JsonObject propertySchema, boolean isRequired) {
			properties.add(parameterName, propertySchema);
			if (isRequired) {
				required.add(parameterName);
			}
			return this;
		}

		/**
		 * Specifies whether the implementation must be executed in the event dispatch thread, e.g., because
		 * it accesses Swing components. Default: {@code false}<br>
		 * <br>
		 * Note: Only use this for implementations that need the event dispatch thread. Implementations that
		 * take a long time (e.g., waiting for a process or a network response) would block the UI.
		 */
		public Builder runInEventDispatchThread(boolean runInEventDispatchThread) {
			this.runInEventDispatchThread = runInEventDispatchThread;
			return this;
		}

		public AiTool build() {
			JsonObject schema = new JsonObject();
			schema.addProperty("type", "object");
			schema.add("properties", properties.deepCopy());
			if (!required.isEmpty()) {
				schema.add("required", required.deepCopy());
			}
			return new AiTool(name, description, schema, implementation, runInEventDispatchThread);
		}
	}
}
