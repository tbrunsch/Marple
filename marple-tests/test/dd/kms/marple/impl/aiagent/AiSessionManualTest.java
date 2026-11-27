package dd.kms.marple.impl.aiagent;

import com.google.gson.JsonObject;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.ObjectInspectionFramework;
import dd.kms.marple.api.aiagent.backend.AiAgent;
import dd.kms.marple.api.aiagent.backend.AiAgentFactory;
import dd.kms.marple.api.aiagent.backend.AiAgentListener;
import dd.kms.marple.api.aiagent.backend.PermissionDecision;
import dd.kms.marple.api.aiagent.backend.PermissionHandler;
import dd.kms.marple.api.aiagent.claude.ClaudeCodeAgentFactoryBuilder;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.impl.InspectionContextImpl;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Manual end-to-end test of {@link AiSession} with Claude Code (requires {@code claude} on the PATH).
 * It checks tool calls, the permission prompt tool (file edits are denied) and resuming a conversation.
 * Note that this test sends real requests to Claude.<br>
 * <br>
 * Like in the AI view, the session is only accessed on the event dispatch thread, whereas the requests
 * are sent to the agent in the main thread.
 */
public class AiSessionManualTest
{
	public static void main(String[] args) throws Exception {
		DemoFrame testFrame = new DemoFrame();
		SwingUtilities.invokeAndWait(() -> {
			testFrame.pack();
			testFrame.setVisible(true);
		});
		JLabel thisValue = findFirstLabel(testFrame);
		Path repoRoot = findRepositoryRoot();
		System.out.println("Repository root: " + repoRoot);

		InspectionSettings inspectionSettings = ObjectInspectionFramework.createInspectionSettingsBuilder().build();
		InspectionContext context = new InspectionContextImpl(inspectionSettings);
		AiAgentFactory agentFactory = ClaudeCodeAgentFactoryBuilder.create(repoRoot).build();
		AiAgentViewSettings settings = new AiAgentViewSettings(() -> agentFactory, "");

		PermissionHandler permissionHandler = request -> {
			System.out.println("PERMISSION REQUEST: tool=" + request.getToolName() + ", file=" + request.getFilePath());
			System.out.println(request.getPreview());
			return PermissionDecision.deny("This is a test. File changes are not allowed.");
		};
		AiAgentListener listener = new AiAgentListener() {
			@Override
			public void onProgress(String text) {
				System.out.println("  PROGRESS: " + text);
			}

			@Override
			public void onToolUse(String toolName, String summary, JsonObject input) {
				System.out.println("  TOOL: " + toolName + ": " + summary);
			}
		};
		String contextLine = "[this: " + thisValue.getClass().getName() + " - " + context.getDisplayText(thisValue) + "]";

		AiSession session = onEdt(() -> new AiSession(settings, agentFactory, context, permissionHandler, thisValue));
		Object resumeState;
		AgentContext agentContext = onEdt(session::getAgentContext);
		try {
			ask(session, contextLine, Scope.RUNNING_APPLICATION, listener, "Which field of which class holds \"this\"? Use the component hierarchy. Then change the text of \"this\" to \"Changed by AI\" at runtime.");
			System.out.println("Label text is now: " + thisValue.getText());
			ask(session, contextLine, Scope.FILES, listener, "Make that change permanent in the source code (one edit only; if it is rejected, do not retry).");
			resumeState = onEdt(session::getResumeState);
			System.out.println("Resume state: " + resumeState);
		} finally {
			SwingUtilities.invokeAndWait(session::close);
		}

		AiSession resumedSession = onEdt(() -> new AiSession(settings, agentFactory, context, permissionHandler, thisValue));
		Object finalResumeState = resumeState;
		SwingUtilities.invokeAndWait(() -> resumedSession.restore(finalResumeState, Collections.emptyList(), agentContext));
		try {
			ask(resumedSession, contextLine, Scope.RUNNING_APPLICATION, listener, "Which text did you set at runtime earlier in this conversation? Answer briefly without using tools.");
		} finally {
			SwingUtilities.invokeAndWait(resumedSession::close);
		}
		SwingUtilities.invokeLater(testFrame::dispose);
		// give the process terminator threads time to finish
		Thread.sleep(4000);
		System.out.println("Done");
		System.exit(0);
	}

	private static void ask(AiSession session, String contextLine, Scope scope, AiAgentListener listener, String message) throws Exception {
		System.out.println("USER: " + message);
		long startTime = System.currentTimeMillis();
		AiAgent agent = onEdt(session::getOrCreateAgent);
		String fullMessage = onEdt(() -> session.createFullMessage(message, contextLine + "\n" + scope.getMessageLine()));
		String answer = agent.sendRequest(fullMessage, listener);
		System.out.println("AGENT (" + (System.currentTimeMillis() - startTime) + " ms): " + answer);
		System.out.println();
	}

	/**
	 * Executes the supplier on the event dispatch thread and returns its result
	 */
	private static <T> T onEdt(EdtSupplier<T> supplier) throws Exception {
		Object[] result = new Object[1];
		Exception[] exception = new Exception[1];
		SwingUtilities.invokeAndWait(() -> {
			try {
				result[0] = supplier.get();
			} catch (Exception e) {
				exception[0] = e;
			}
		});
		if (exception[0] != null) {
			throw exception[0];
		}
		@SuppressWarnings("unchecked")
		T typedResult = (T) result[0];
		return typedResult;
	}

	@FunctionalInterface
	private interface EdtSupplier<T>
	{
		T get() throws Exception;
	}

	private static Path findRepositoryRoot() throws Exception {
		Path location = Paths.get(AiSessionManualTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		for (Path directory = location; directory != null; directory = directory.getParent()) {
			if (Files.isDirectory(directory.resolve("marple")) && Files.isDirectory(directory.resolve("marple-tests"))) {
				return directory;
			}
		}
		throw new IllegalStateException("Cannot find repository root");
	}

	private static class DemoFrame extends JFrame
	{
		private final JPanel	statusPanel	= new JPanel(new FlowLayout());
		private final JLabel	statusLabel	= new JLabel("Status: idle");

		DemoFrame() {
			super("AI session test");
			statusPanel.add(statusLabel);
			getContentPane().add(statusPanel);
		}
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
