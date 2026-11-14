package dd.kms.marple;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.ObjectInspectionFramework;
import dd.kms.marple.api.aiagent.backend.McpEndpoint;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.impl.InspectionContextImpl;
import dd.kms.marple.impl.aiagent.AgentVariables;
import dd.kms.marple.impl.aiagent.mcp.MarpleTools;
import dd.kms.marple.impl.aiagent.mcp.McpServer;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Starts the MCP server of the AI view with a label of the test frame as "this", sends some requests
 * to it and prints the responses. Afterwards, the server keeps running (until the test frame is closed),
 * so it can be tested manually, e.g., with curl:
 * <pre>
 * curl -s -X POST -H "Authorization: Bearer &lt;token&gt;" -H "Content-Type: application/json" \
 *      -d "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}" &lt;url&gt;
 * </pre>
 */
public class McpServerManualTest
{
	public static void main(String[] args) throws Exception {
		TestFrame testFrame = new TestFrame();
		SwingUtilities.invokeAndWait(() -> {
			testFrame.pack();
			testFrame.setVisible(true);
		});
		JLabel thisValue = findFirstLabel(testFrame);

		InspectionSettings settings = ObjectInspectionFramework.createInspectionSettingsBuilder().build();
		InspectionContext context = new InspectionContextImpl(settings);
		McpServer server = new McpServer(new MarpleTools(context, thisValue, new AgentVariables(context.getEvaluator())).createTools());
		McpEndpoint endpoint = server.start();
		testFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				server.stop();
			}
		});

		System.out.println("URL:   " + endpoint.getUrl());
		System.out.println("Token: " + endpoint.getBearerToken());
		System.out.println();

		print("Without token (expected: 401)", post(endpoint.getUrl(), null, "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}"));
		String token = endpoint.getBearerToken();
		print("initialize", post(endpoint.getUrl(), token, "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1\"}}}"));
		print("notifications/initialized (expected: 202)", post(endpoint.getUrl(), token, "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}"));
		print("tools/list", post(endpoint.getUrl(), token, "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}"));
		print("get_object_info", callTool(endpoint, "get_object_info", "{}"));
		print("get_component_hierarchy", callTool(endpoint, "get_component_hierarchy", "{}"));
		print("evaluate_expression", callTool(endpoint, "evaluate_expression", "{\"expression\":\"getText()\",\"variableName\":\"labelText\"}"));
		print("evaluate_expression (parse error)", callTool(endpoint, "evaluate_expression", "{\"expression\":\"getTxt()\"}"));
		print("list_variables", callTool(endpoint, "list_variables", "{}"));
		print("delete_variable", callTool(endpoint, "delete_variable", "{\"name\":\"labelText\"}"));
		print("get_fields", callTool(endpoint, "get_fields", "{\"expression\":\"getParent()\"}"));
		System.out.println("The server keeps running until the test frame is closed.");
	}

	private static String callTool(McpEndpoint endpoint, String toolName, String arguments) throws IOException {
		String request = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"" + toolName + "\",\"arguments\":" + arguments + "}}";
		return post(endpoint.getUrl(), endpoint.getBearerToken(), request);
	}

	private static String post(String url, @Nullable String token, String body) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Accept", "application/json, text/event-stream");
		if (token != null) {
			connection.setRequestProperty("Authorization", "Bearer " + token);
		}
		try (OutputStream stream = connection.getOutputStream()) {
			stream.write(body.getBytes(StandardCharsets.UTF_8));
		}
		int status = connection.getResponseCode();
		InputStream responseStream = status < 400 ? connection.getInputStream() : connection.getErrorStream();
		String response = responseStream == null ? "" : readAll(responseStream);
		return "HTTP " + status + (response.isEmpty() ? "" : "\n" + response);
	}

	private static String readAll(InputStream stream) throws IOException {
		try (InputStream in = stream) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] chunk = new byte[8192];
			int numBytes;
			while ((numBytes = in.read(chunk)) >= 0) {
				buffer.write(chunk, 0, numBytes);
			}
			return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	private static void print(String title, String response) {
		System.out.println("=== " + title + " ===");
		System.out.println(response);
		System.out.println();
	}

	private static JLabel findFirstLabel(Container container) {
		for (Component component : container.getComponents()) {
			if (component instanceof JLabel) {
				return (JLabel) component;
			}
			if (component instanceof Container) {
				JLabel label = findFirstLabel((Container) component);
				if (label != null) {
					return label;
				}
			}
		}
		return null;
	}
}
