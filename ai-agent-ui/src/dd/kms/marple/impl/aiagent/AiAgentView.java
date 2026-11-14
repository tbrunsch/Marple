package dd.kms.marple.impl.aiagent;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.aiagent.backend.AiAgentListener;
import dd.kms.marple.api.settings.visual.ObjectView;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The "AI Agent" tab: a chat with an AI agent about the inspected object. A new view (and a new session)
 * is created whenever the inspected object changes. The conversation is restored via the view settings
 * when the user navigates back to an object.
 */
class AiAgentView extends JPanel implements ObjectView
{
	private enum State
	{
		IDLE,
		RUNNING,
		/**
		 * The request has been stopped or the session has been cleared, but the request has not finished yet.
		 */
		CANCELING,
		/**
		 * The view has been disposed. This state is final.
		 */
		CLOSED
	}

	private static final int	MAX_CONTEXT_DISPLAY_LENGTH	= 200;

	private final Object				thisValue;
	private final InspectionContext		context;
	private final AiAgentViewSettings	settings;
	@Nullable
	private final AiSession				session;

	private final List<TranscriptEntry>	transcript				= new ArrayList<>();

	private final JLabel				statusLabel				= new JLabel();
	private final JButton				stopButton				= new JButton("Stop");
	private final JButton				clearSessionButton		= new JButton("Clear Session");
	private final JCheckBox				autoApproveCheckBox		= new JCheckBox("Auto approve");
	private final JButton				agentVariablesButton	= new JButton();
	private final JEditorPane			transcriptPane			= new JEditorPane();
	private final JTextArea				inputArea				= new JTextArea(4, 40);

	private final Runnable				autoApproveListener		= this::onAutoApproveChanged;
	private final Runnable				agentVariablesListener	= this::updateAgentVariablesButton;

	/**
	 * The variables of the conversation that is currently shown, observed for the button text
	 */
	@Nullable
	private AgentVariables				observedAgentVariables;
	@Nullable
	private AgentVariablesDialog		agentVariablesDialog;

	private State						state					= State.IDLE;
	/**
	 * The request that has been sent last or {@code null} if no request is running. It is non-null if
	 * and only if the state is {@link State#RUNNING} or {@link State#CANCELING}.
	 */
	@Nullable
	private Request						currentRequest;

	AiAgentView(Object thisValue, InspectionContext context, AiAgentViewSettings settings) {
		super(new BorderLayout());
		this.thisValue = thisValue;
		this.context = context;
		this.settings = settings;

		if (settings.getAgentFactory() == null) {
			session = null;
			JLabel unavailableLabel = new JLabel(settings.getUnavailableMessage(), SwingConstants.CENTER);
			add(unavailableLabel, BorderLayout.CENTER);
			return;
		}
		SwingPermissionHandler permissionHandler = new SwingPermissionHandler(settings, this);
		session = new AiSession(settings, context, permissionHandler, thisValue);
		observeAgentVariables(session.getAgentVariables());

		createLayout();
		addListeners();
		settings.addAutoApproveListener(autoApproveListener);
		onAutoApproveChanged();
		updateControls();
		renderTranscript();
	}

	private void createLayout() {
		JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		clearSessionButton.setToolTipText("Discards the conversation and the variables the agent has created");
		buttonBar.add(clearSessionButton);
		buttonBar.add(stopButton);
		agentVariablesButton.setToolTipText("Shows the variables the agent has created in this conversation");
		buttonBar.add(agentVariablesButton);
		buttonBar.add(Box.createHorizontalStrut(10));
		buttonBar.add(statusLabel);
		autoApproveCheckBox.setToolTipText("Approves all requests of the agent that require approval (e.g., file changes) without asking");
		JPanel toolBar = new JPanel(new BorderLayout());
		toolBar.add(buttonBar, BorderLayout.CENTER);
		toolBar.add(autoApproveCheckBox, BorderLayout.EAST);

		transcriptPane.setEditable(false);
		HTMLEditorKit editorKit = new HTMLEditorKit();
		StyleSheet styleSheet = new StyleSheet();
		styleSheet.addStyleSheet(editorKit.getStyleSheet());
		Font font = UIManager.getFont("Label.font");
		String fontFamily = font != null ? font.getFamily() : "SansSerif";
		int fontSize = font != null ? font.getSize() : 12;
		styleSheet.addRule("body { font-family: " + fontFamily + "; font-size: " + fontSize + "pt; margin: 4px; }");
		styleSheet.addRule(".user { background-color: #e8f0fe; padding: 4px; margin-top: 6px; }");
		styleSheet.addRule(".agent { padding: 2px; margin-top: 4px; }");
		styleSheet.addRule(".progress { color: #505050; padding: 2px; margin-top: 2px; }");
		styleSheet.addRule(".tool { color: #707070; font-size: " + Math.max(fontSize - 1, 8) + "pt; margin-top: 1px; }");
		styleSheet.addRule(".info { color: #707070; font-style: italic; margin-top: 4px; }");
		styleSheet.addRule(".error { color: #b00020; margin-top: 4px; }");
		styleSheet.addRule("pre { font-family: Monospaced; background-color: #f4f4f4; padding: 4px; }");
		styleSheet.addRule("code { font-family: Monospaced; background-color: #f4f4f4; }");
		styleSheet.addRule("h2, h3, h4 { margin-top: 6px; margin-bottom: 2px; }");
		styleSheet.addRule("p { margin-top: 2px; margin-bottom: 4px; }");
		editorKit.setStyleSheet(styleSheet);
		transcriptPane.setEditorKit(editorKit);
		JScrollPane transcriptScrollPane = new JScrollPane(transcriptPane);

		inputArea.setLineWrap(true);
		inputArea.setWrapStyleWord(true);
		JLabel inputLabel = new JLabel("Your message (Enter sends, Shift+Enter inserts a line break):");
		inputLabel.setLabelFor(inputArea);
		JPanel inputPanel = new JPanel(new BorderLayout(0, 2));
		inputPanel.add(inputLabel, BorderLayout.NORTH);
		inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, transcriptScrollPane, inputPanel);
		splitPane.setResizeWeight(1.0);
		splitPane.setPreferredSize(new Dimension(500, 400));

		add(toolBar, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
	}

	private void addListeners() {
		stopButton.addActionListener(e -> stop());
		clearSessionButton.addActionListener(e -> clearSession());
		autoApproveCheckBox.addActionListener(e -> settings.setAutoApprove(autoApproveCheckBox.isSelected()));
		agentVariablesButton.addActionListener(e -> showAgentVariables());

		// Enter sends the message, Shift+Enter inserts a line break (as in common chat applications)
		InputMap inputMap = inputArea.getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK), DefaultEditorKit.insertBreakAction);
		inputArea.getActionMap().put("send", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
	}

	/*
	 * Agent variables
	 */
	private void showAgentVariables() {
		if (session == null) {
			return;
		}
		AgentVariables agentVariables = session.getAgentVariables();
		// the user might have deleted variables in Marple
		updateAgentVariablesButton();
		if (agentVariables.getVariables().isEmpty()) {
			return;
		}
		AgentVariablesDialog dialog = new AgentVariablesDialog(SwingUtilities.getWindowAncestor(this), context, agentVariables);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				agentVariablesDialog = null;
				updateAgentVariablesButton();
			}
		});
		agentVariablesDialog = dialog;
		updateAgentVariablesButton();
		dialog.setVisible(true);
	}

	/**
	 * Must be called whenever the session switches to another conversation.
	 */
	private void observeAgentVariables(@Nullable AgentVariables agentVariables) {
		if (observedAgentVariables != null) {
			observedAgentVariables.removeChangeListener(agentVariablesListener);
		}
		observedAgentVariables = agentVariables;
		if (agentVariables != null) {
			agentVariables.addChangeListener(agentVariablesListener);
		}
		updateAgentVariablesButton();
	}

	private void updateAgentVariablesButton() {
		int count = observedAgentVariables != null ? observedAgentVariables.getVariables().size() : 0;
		agentVariablesButton.setText("Show Agent Variables (" + count + ")");
		// at most one dialog: it follows the changes anyway
		agentVariablesButton.setEnabled(count > 0 && agentVariablesDialog == null);
	}

	private void onAutoApproveChanged() {
		EventDispatchThreadUtils.runNowOrLater(() -> autoApproveCheckBox.setSelected(settings.isAutoApprove()));
	}

	/*
	 * Requests
	 */
	private void send() {
		String message = inputArea.getText().trim();
		if (state != State.IDLE || message.isEmpty() || session == null) {
			return;
		}
		inputArea.setText("");
		addEntry(TranscriptEntry.Role.USER, message);

		Request request = new Request(session, message, createContextLine());
		currentRequest = request;
		state = State.RUNNING;
		updateControls();
		request.execute();
	}

	private void onRequestFinished(Request request) {
		if (request != currentRequest) {
			// the view has been disposed
			return;
		}
		boolean canceled = state == State.CANCELING;
		currentRequest = null;
		state = State.IDLE;
		if (!request.isDiscarded()) {
			try {
				String answer = request.get();
				addEntry(TranscriptEntry.Role.AGENT, answer);
			} catch (InterruptedException e) {
				addEntry(TranscriptEntry.Role.INFO, "The request has been interrupted.");
			} catch (ExecutionException e) {
				Throwable cause = e.getCause() != null ? e.getCause() : e;
				if (canceled || cause instanceof InterruptedIOException) {
					addEntry(TranscriptEntry.Role.INFO, "The request has been stopped.");
				} else {
					String message = cause.getMessage();
					addEntry(TranscriptEntry.Role.ERROR, message != null ? message : cause.toString());
				}
			}
		}
		updateAgentVariablesButton();
		updateControls();
	}

	private void stop() {
		if (state != State.RUNNING || session == null) {
			return;
		}
		state = State.CANCELING;
		updateControls();
		session.cancelRequest();
	}

	private void clearSession() {
		if (session == null || state == State.CLOSED) {
			return;
		}
		if (currentRequest != null) {
			/*
			 * The request belongs to the old conversation: Its progress and its result must not appear in the
			 * new transcript. Resetting the session closes the agent, which makes the request terminate soon.
			 */
			currentRequest.discard();
			state = State.CANCELING;
		}
		session.reset();
		observeAgentVariables(session.getAgentVariables());
		transcript.clear();
		renderTranscript();
		updateControls();
	}

	private String createContextLine() {
		String displayText;
		try {
			displayText = context.getDisplayText(thisValue);
		} catch (Throwable t) {
			displayText = "?";
		}
		if (displayText == null) {
			displayText = "null";
		}
		displayText = displayText.replace('\n', ' ').replace('\r', ' ');
		if (displayText.length() > MAX_CONTEXT_DISPLAY_LENGTH) {
			displayText = displayText.substring(0, MAX_CONTEXT_DISPLAY_LENGTH) + "...";
		}
		String className = thisValue == null ? "null" : thisValue.getClass().getName();
		return "[this: " + className + " - " + displayText + "]";
	}

	private void updateControls() {
		if (session == null) {
			return;
		}
		stopButton.setEnabled(state == State.RUNNING);
		statusLabel.setText(getStatusText());
	}

	private String getStatusText() {
		switch (state) {
			case RUNNING:
				return "Agent is working...";
			case CANCELING:
				return "Stopping...";
			default:
				return transcript.isEmpty() ? "Ask anything about \"this\" or the application" : "Ready";
		}
	}

	/*
	 * Transcript
	 */
	private void addEntryLater(Request request, TranscriptEntry.Role role, String text) {
		SwingUtilities.invokeLater(() -> {
			if (request == currentRequest && !request.isDiscarded()) {
				addEntry(role, text);
			}
		});
	}

	private void addEntry(TranscriptEntry.Role role, String text) {
		transcript.add(new TranscriptEntry(role, text));
		renderTranscript();
	}

	private void renderTranscript() {
		StringBuilder html = new StringBuilder("<html><body>");
		for (TranscriptEntry entry : transcript) {
			String text = entry.getText();
			switch (entry.getRole()) {
				case USER:
					html.append("<div class=\"user\"><b>You:</b> ").append(MarkdownToHtml.escape(text).replace("\n", "<br>")).append("</div>");
					break;
				case AGENT:
					html.append("<div class=\"agent\">").append(MarkdownToHtml.convert(text)).append("</div>");
					break;
				case PROGRESS:
					html.append("<div class=\"progress\">").append(MarkdownToHtml.convert(text)).append("</div>");
					break;
				case TOOL:
					html.append("<div class=\"tool\">&rarr; ").append(MarkdownToHtml.escape(text)).append("</div>");
					break;
				case INFO:
					html.append("<div class=\"info\">").append(MarkdownToHtml.escape(text)).append("</div>");
					break;
				case ERROR:
					html.append("<div class=\"error\">").append(MarkdownToHtml.escape(text).replace("\n", "<br>")).append("</div>");
					break;
			}
		}
		html.append("</body></html>");
		transcriptPane.setText(html.toString());
		SwingUtilities.invokeLater(() -> transcriptPane.setCaretPosition(transcriptPane.getDocument().getLength()));
	}

	/*
	 * ObjectView
	 */
	@Override
	public String getViewName() {
		return "AI Agent";
	}

	@Override
	public Component getViewComponent() {
		return this;
	}

	@Nullable
	@Override
	public Object getViewSettings() {
		if (session == null) {
			return null;
		}
		return new AiSessionSnapshot(transcript, session.getResumeState(), session.getAgentVariables());
	}

	@Override
	public void applyViewSettings(@Nullable Object settings, ViewSettingsOrigin origin) {
		// the conversation belongs to the inspected object => only restore it for the same context
		if (origin != ViewSettingsOrigin.SAME_CONTEXT || !(settings instanceof AiSessionSnapshot) || session == null || state != State.IDLE) {
			return;
		}
		AiSessionSnapshot snapshot = (AiSessionSnapshot) settings;
		transcript.clear();
		transcript.addAll(snapshot.getTranscript());
		session.restore(snapshot.getResumeState(), snapshot.getTranscript(), snapshot.getAgentVariables());
		observeAgentVariables(session.getAgentVariables());
		renderTranscript();
		updateControls();
	}

	@Override
	public void dispose() {
		if (state == State.CLOSED) {
			return;
		}
		state = State.CLOSED;
		currentRequest = null;
		settings.removeAutoApproveListener(autoApproveListener);
		// the agent variables are referenced by the history => do not let them reference this view
		observeAgentVariables(null);
		if (agentVariablesDialog != null) {
			agentVariablesDialog.dispose();
		}
		if (session != null) {
			session.close();
		}
	}

	/**
	 * Sends a request in the background. The listener methods may be called from any thread, all
	 * other methods must be called on the event dispatch thread.
	 */
	private class Request extends SwingWorker<String, Void> implements AiAgentListener
	{
		private final AiSession	session;
		private final int		conversationId;
		private final String	message;
		private final String	contextLine;

		/**
		 * The progress and the result of a discarded request are not shown.
		 */
		private boolean			discarded;

		Request(AiSession session, String message, String contextLine) {
			this.session = session;
			// determined now: the session may be reset before the request is sent in the background
			this.conversationId = session.getConversationId();
			this.message = message;
			this.contextLine = contextLine;
		}

		boolean isDiscarded() {
			return discarded;
		}

		void discard() {
			discarded = true;
		}

		@Override
		protected String doInBackground() throws Exception {
			return session.sendRequest(conversationId, message, contextLine, this);
		}

		@Override
		protected void done() {
			onRequestFinished(this);
		}

		@Override
		public void onProgress(String text) {
			addEntryLater(this, TranscriptEntry.Role.PROGRESS, text);
		}

		@Override
		public void onToolUse(String toolName, String summary) {
			addEntryLater(this, TranscriptEntry.Role.TOOL, summary.isEmpty() ? toolName : toolName + ": " + summary);
		}
	}
}
