package dd.kms.marple.impl.aiagent;

import com.google.gson.JsonObject;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.ObjectInspectionFramework;
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
 * Note that this test sends real requests to Claude.
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

		AiSession session = new AiSession(settings, agentFactory, context, permissionHandler, thisValue);
		Object resumeState;
		AgentContext agentContext = session.getAgentContext();
		try {
			ask(session, contextLine, listener, "Which field of which class holds \"this\"? Use the component hierarchy. Then change the text of \"this\" to \"Changed by AI\" at runtime.");
			System.out.println("Label text is now: " + thisValue.getText());
			ask(session, contextLine, listener, "Make that change permanent in the source code (one edit only; if it is rejected, do not retry).");
			resumeState = session.getResumeState();
			System.out.println("Resume state: " + resumeState);
		} finally {
			session.close();
		}

		AiSession resumedSession = new AiSession(settings, agentFactory, context, permissionHandler, thisValue);
		// restore() must be called on the event dispatch thread
		Object finalResumeState = resumeState;
		SwingUtilities.invokeAndWait(() -> resumedSession.restore(finalResumeState, Collections.emptyList(), agentContext));
		try {
			ask(resumedSession, contextLine, listener, "Which text did you set at runtime earlier in this conversation? Answer briefly without using tools.");
		} finally {
			resumedSession.close();
		}
		SwingUtilities.invokeLater(testFrame::dispose);
		// give the process terminator threads time to finish
		Thread.sleep(4000);
		System.out.println("Done");
		System.exit(0);
	}

	private static void ask(AiSession session, String contextLine, AiAgentListener listener, String message) throws Exception {
		System.out.println("USER: " + message);
		long startTime = System.currentTimeMillis();
		String answer = session.sendRequest(session.getConversationId(), message, contextLine, listener);
		System.out.println("AGENT (" + (System.currentTimeMillis() - startTime) + " ms): " + answer);
		System.out.println();
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
