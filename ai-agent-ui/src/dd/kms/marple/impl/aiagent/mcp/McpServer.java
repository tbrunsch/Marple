package dd.kms.marple.impl.aiagent.mcp;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dd.kms.marple.api.aiagent.backend.AiTool;
import dd.kms.marple.api.aiagent.backend.AiToolResult;
import dd.kms.marple.api.aiagent.backend.McpEndpoint;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static dd.kms.marple.impl.aiagent.JsonUtils.getObject;
import static dd.kms.marple.impl.aiagent.JsonUtils.getString;

/**
 * Minimal MCP server with Streamable HTTP transport (JSON responses only, no SSE). It is bound to
 * the loopback interface and every request must contain a randomly generated bearer token
 * because the tools allow executing arbitrary code.
 */
public class McpServer
{
	private static final String			PATH						= "/mcp";
	private static final String			SERVER_NAME					= "marple";
	private static final String			SERVER_VERSION				= "0.6.0";
	private static final String			DEFAULT_PROTOCOL_VERSION	= "2025-06-18";
	private static final AtomicInteger	THREAD_COUNTER				= new AtomicInteger();
	private static final Gson			GSON						= new GsonBuilder().disableHtmlEscaping().create();

	/*
	 * Error codes defined by the JSON-RPC 2.0 specification
	 */
	private static final int			ERROR_PARSE_ERROR			= -32700;
	private static final int			ERROR_INVALID_REQUEST		= -32600;
	private static final int			ERROR_METHOD_NOT_FOUND		= -32601;
	private static final int			ERROR_INVALID_PARAMS		= -32602;

	/*
	 * HTTP status codes
	 */
	private static final int			HTTP_OK						= 200;
	private static final int			HTTP_ACCEPTED				= 202;
	private static final int			HTTP_UNAUTHORIZED			= 401;
	private static final int			HTTP_METHOD_NOT_ALLOWED		= 405;
	private static final int			HTTP_INTERNAL_SERVER_ERROR	= 500;

	private final List<AiTool>		tools			= new CopyOnWriteArrayList<>();
	private final String			bearerToken		= createToken();

	@Nullable
	private HttpServer				server;
	@Nullable
	private ExecutorService			executor;

	public McpServer(List<AiTool> tools) {
		this.tools.addAll(tools);
	}

	public void addTool(AiTool tool) {
		tools.add(tool);
	}

	public List<AiTool> getTools() {
		return tools;
	}

	public synchronized McpEndpoint start() throws IOException {
		if (server == null) {
			HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
			ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
				Thread thread = new Thread(runnable, "Marple MCP server " + THREAD_COUNTER.incrementAndGet());
				thread.setDaemon(true);
				return thread;
			});
			server.setExecutor(executor);
			server.createContext(PATH, this::handle);
			server.start();
			this.server = server;
			this.executor = executor;
		}
		return getEndpoint();
	}

	public synchronized McpEndpoint getEndpoint() {
		if (server == null) {
			throw new IllegalStateException("MCP server has not been started");
		}
		String url = "http://127.0.0.1:" + server.getAddress().getPort() + PATH;
		return new McpEndpoint(url, bearerToken);
	}

	public synchronized void stop() {
		if (server != null) {
			server.stop(0);
			server = null;
		}
		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}
	}

	private void handle(HttpExchange exchange) throws IOException {
		try {
			if (!isAuthorized(exchange)) {
				sendEmpty(exchange, HTTP_UNAUTHORIZED);
				return;
			}
			String method = exchange.getRequestMethod();
			if ("DELETE".equals(method)) {
				sendEmpty(exchange, HTTP_OK);
				return;
			}
			if (!"POST".equals(method)) {
				exchange.getResponseHeaders().add("Allow", "POST");
				sendEmpty(exchange, HTTP_METHOD_NOT_ALLOWED);
				return;
			}
			String body = readBody(exchange.getRequestBody());
			JsonElement request;
			try {
				request = JsonParser.parseString(body);
			} catch (JsonParseException e) {
				sendJson(exchange, createError(JsonNull.INSTANCE, ERROR_PARSE_ERROR, "Parse error: " + e.getMessage()));
				return;
			}
			if (!request.isJsonObject()) {
				sendJson(exchange, createError(JsonNull.INSTANCE, ERROR_INVALID_REQUEST, "Invalid request: Batches are not supported"));
				return;
			}
			JsonObject response = handleRequest(request.getAsJsonObject());
			if (response == null) {
				sendEmpty(exchange, HTTP_ACCEPTED);
			} else {
				sendJson(exchange, response);
			}
		} catch (Throwable t) {
			sendEmpty(exchange, HTTP_INTERNAL_SERVER_ERROR);
		} finally {
			exchange.close();
		}
	}

	private boolean isAuthorized(HttpExchange exchange) {
		String authorization = exchange.getRequestHeaders().getFirst("Authorization");
		if (authorization == null) {
			return false;
		}
		byte[] expected = ("Bearer " + bearerToken).getBytes(StandardCharsets.UTF_8);
		byte[] actual = authorization.trim().getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(expected, actual);
	}

	/**
	 * @return the response or {@code null} if the request is a notification or a response
	 */
	@Nullable
	JsonObject handleRequest(JsonObject request) {
		JsonElement id = request.get("id");
		if (id == null) {
			// notification
			return null;
		}
		try {
			String method = getString(request, "method");
			if (method == null) {
				// response from the client
				return null;
			}
			JsonObject params = getObject(request, "params");
			switch (method) {
				case "initialize":
					return createResult(id, handleInitialize(params));
				case "ping":
					return createResult(id, new JsonObject());
				case "tools/list":
					return createResult(id, handleToolsList());
				case "tools/call":
					return handleToolsCall(id, params);
				default:
					return createError(id, ERROR_METHOD_NOT_FOUND, "Method not found: " + method);
			}
		} catch (JsonParseException e) {
			return createError(id, ERROR_INVALID_REQUEST, "Invalid request: " + e.getMessage());
		}
	}

	private JsonObject handleInitialize(JsonObject params) {
		String protocolVersion = getString(params, "protocolVersion");
		if (protocolVersion == null) {
			protocolVersion = DEFAULT_PROTOCOL_VERSION;
		}

		JsonObject toolsCapability = new JsonObject();
		toolsCapability.addProperty("listChanged", false);
		JsonObject capabilities = new JsonObject();
		capabilities.add("tools", toolsCapability);

		JsonObject serverInfo = new JsonObject();
		serverInfo.addProperty("name", SERVER_NAME);
		serverInfo.addProperty("version", SERVER_VERSION);

		JsonObject result = new JsonObject();
		result.addProperty("protocolVersion", protocolVersion);
		result.add("capabilities", capabilities);
		result.add("serverInfo", serverInfo);
		return result;
	}

	private JsonObject handleToolsList() {
		JsonArray toolArray = new JsonArray();
		for (AiTool tool : tools) {
			JsonObject toolObject = new JsonObject();
			toolObject.addProperty("name", tool.getName());
			toolObject.addProperty("description", tool.getDescription());
			toolObject.add("inputSchema", tool.getInputSchema());
			toolArray.add(toolObject);
		}
		JsonObject result = new JsonObject();
		result.add("tools", toolArray);
		return result;
	}

	private JsonObject handleToolsCall(JsonElement id, JsonObject params) {
		String name;
		JsonObject arguments;
		try {
			name = getString(params, "name");
			arguments = getObject(params, "arguments");
		} catch (JsonParseException e) {
			return createError(id, ERROR_INVALID_PARAMS, "Invalid params: " + e.getMessage());
		}
		AiTool tool = tools.stream().filter(t -> t.getName().equals(name)).findFirst().orElse(null);
		if (tool == null) {
			return createError(id, ERROR_INVALID_PARAMS, "Unknown tool: " + name);
		}
		// does not throw exceptions: failures are returned as error result
		AiToolResult toolResult = tool.call(arguments);
		JsonObject textContent = new JsonObject();
		textContent.addProperty("type", "text");
		textContent.addProperty("text", toolResult.getText());
		JsonArray content = new JsonArray();
		content.add(textContent);

		JsonObject result = new JsonObject();
		result.add("content", content);
		result.addProperty("isError", toolResult.isError());
		return createResult(id, result);
	}

	private static JsonObject createResult(JsonElement id, JsonObject result) {
		JsonObject response = new JsonObject();
		response.addProperty("jsonrpc", "2.0");
		response.add("id", id);
		response.add("result", result);
		return response;
	}

	private static JsonObject createError(JsonElement id, int code, String message) {
		JsonObject error = new JsonObject();
		error.addProperty("code", code);
		error.addProperty("message", message);
		JsonObject response = new JsonObject();
		response.addProperty("jsonrpc", "2.0");
		response.add("id", id);
		response.add("error", error);
		return response;
	}

	private void sendJson(HttpExchange exchange, JsonObject response) throws IOException {
		byte[] bytes = GSON.toJson(response).getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
		exchange.sendResponseHeaders(HTTP_OK, bytes.length);
		try (OutputStream stream = exchange.getResponseBody()) {
			stream.write(bytes);
		}
	}

	private static void sendEmpty(HttpExchange exchange, int statusCode) throws IOException {
		exchange.sendResponseHeaders(statusCode, -1);
	}

	private static String readBody(InputStream stream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] chunk = new byte[8192];
		int numBytes;
		while ((numBytes = stream.read(chunk)) >= 0) {
			buffer.write(chunk, 0, numBytes);
		}
		return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
	}

	private static String createToken() {
		byte[] bytes = new byte[32];
		new SecureRandom().nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
