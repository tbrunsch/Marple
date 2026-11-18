package dd.kms.marple.impl.aiagent;

import com.google.gson.JsonObject;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.aiagent.backend.AiAgentFactory;
import dd.kms.marple.api.aiagent.backend.AiAgentListener;
import dd.kms.marple.api.settings.visual.ObjectView;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
	private static final String	TOOL_CALLS_LINK_PREFIX		= "toolcalls:";
	private static final String	TOOL_CALL_ANCHOR_PREFIX		= "toolcall-";
	/**
	 * The content of {@code AiAgentView.css} with placeholders (see {@link #loadTranscriptStyleRules(StyleSheet)})
	 */
	private static final String	TRANSCRIPT_STYLE_RULES		= Resources.loadText("AiAgentView.css");

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
	private final JButton				toolCallsButton			= new JButton();
	private final JEditorPane			transcriptPane			= new JEditorPane();
	private final JTextArea				inputArea				= new JTextArea(4, 40);

	private final Runnable				autoApproveListener		= this::onAutoApproveChanged;
	private final Runnable				agentVariablesListener	= this::updateAgentVariablesButton;

	/**
	 * The variables of the conversation that is currently shown, observed for the button text
	 */
	@Nullable
	private AgentContext				observedAgentContext;
	@Nullable
	private AgentVariablesDialog		agentVariablesDialog;
	@Nullable
	private ToolCallsDialog				toolCallsDialog;

	/**
	 * The number of the tool call that is highlighted in the transcript (1-based) or -1. The highlight is
	 * removed when the next entry is added.
	 */
	private int							highlightedToolCall		= -1;

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

		AiAgentFactory agentFactory;
		try {
			agentFactory = settings.getAgentFactory();
		} catch (IOException e) {
			session = null;
			String reason = e.getMessage() != null ? e.getMessage() : e.toString();
			JLabel unavailableLabel = new JLabel("AI agent not available: " + reason, SwingConstants.CENTER);
			add(unavailableLabel, BorderLayout.CENTER);
			return;
		}
		SwingPermissionHandler permissionHandler = new SwingPermissionHandler(settings, this);
		session = new AiSession(settings, agentFactory, context, permissionHandler, thisValue);
		observeAgentContext(session.getAgentContext());

		createLayout();
		addListeners();
		settings.addAutoApproveListener(autoApproveListener);
		onAutoApproveChanged();
		setState(State.IDLE);
		renderTranscript();
	}

	/**
	 * Loads the rules of the resource {@code AiAgentView.css} after replacing its placeholders by
	 * values derived from the look and feel's label font.
	 */
	private static void loadTranscriptStyleRules(StyleSheet styleSheet) {
		Font font = UIManager.getFont("Label.font");
		String fontFamily = font != null ? font.getFamily() : "SansSerif";
		int fontSize = font != null ? font.getSize() : 12;
		String rules = TRANSCRIPT_STYLE_RULES
			.replace("${fontFamily}", fontFamily)
			.replace("${fontSize}", String.valueOf(fontSize))
			.replace("${smallFontSize}", String.valueOf(Math.max(fontSize - 1, 8)));
		try {
			styleSheet.loadRules(new StringReader(rules), null);
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot load the style rules of the transcript", e);
		}
	}

	private void createLayout() {
		JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		buttonBar.add(clearSessionButton);
		buttonBar.add(stopButton);
		buttonBar.add(agentVariablesButton);
		buttonBar.add(toolCallsButton);
		buttonBar.add(Box.createHorizontalStrut(10));
		buttonBar.add(statusLabel);

		clearSessionButton.setToolTipText("Discards the conversation and the variables the agent has created");
		agentVariablesButton.setToolTipText("Shows the variables the agent has created in this conversation");
		toolCallsButton.setToolTipText("Shows the tools the agent has called in this conversation with their complete parameters");
		autoApproveCheckBox.setToolTipText("Approves all requests of the agent that require approval (e.g., file changes) without asking");

		JPanel toolBar = new JPanel(new BorderLayout());
		toolBar.add(buttonBar, BorderLayout.CENTER);
		toolBar.add(autoApproveCheckBox, BorderLayout.EAST);

		transcriptPane.setEditable(false);
		HTMLEditorKit editorKit = new HTMLEditorKit();
		StyleSheet styleSheet = new StyleSheet();
		styleSheet.addStyleSheet(editorKit.getStyleSheet());
		loadTranscriptStyleRules(styleSheet);
		editorKit.setStyleSheet(styleSheet);
		transcriptPane.setEditorKit(editorKit);
		JScrollPane transcriptScrollPane = new JScrollPane(transcriptPane);

		inputArea.setLineWrap(true);
		inputArea.setWrapStyleWord(true);
		JLabel inputLabel = new JLabel("Ask anything about \"this\" or the application (Enter sends, Shift+Enter inserts a line break):");
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
		toolCallsButton.addActionListener(e -> showToolCalls());
		transcriptPane.addHyperlinkListener(this::onHyperlinkEvent);

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
		if (!canShowAgentVariables()) {
			return;
		}
		AgentVariablesDialog dialog = new AgentVariablesDialog(SwingUtilities.getWindowAncestor(this), context, observedAgentContext);
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
	private void observeAgentContext(@Nullable AgentContext agentContext) {
		if (observedAgentContext != null) {
			observedAgentContext.removeChangeListener(agentVariablesListener);
		}
		observedAgentContext = agentContext;
		if (agentContext != null) {
			agentContext.addChangeListener(agentVariablesListener);
		}
		updateAgentVariablesButton();
	}

	private void updateAgentVariablesButton() {
		int count = observedAgentContext != null ? observedAgentContext.getVariables().size() : 0;
		agentVariablesButton.setText("Show Agent Variables (" + count + ")");
		agentVariablesButton.setEnabled(canShowAgentVariables());
	}

	/**
	 * Returns {@code true} if the conversation has variables and no dialog is open. There is at most one
	 * dialog because it follows the changes anyway.
	 */
	private boolean canShowAgentVariables() {
		return observedAgentContext != null && !observedAgentContext.getVariables().isEmpty() && agentVariablesDialog == null;
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
		setState(State.RUNNING);
		request.execute();
	}

	private void onRequestFinished(Request request) {
		if (request != currentRequest) {
			// the view has been disposed
			return;
		}
		boolean canceled = state == State.CANCELING;
		currentRequest = null;
		setState(State.IDLE);
		if (!request.isDiscarded()) {
			try {
				String answer = request.get();
				addEntry(TranscriptEntry.Role.AGENT, answer);
			} catch (InterruptedException e) {
				addEntry(TranscriptEntry.Role.INFO, "The request has been interrupted.");
			} catch (ExecutionException e) {
				Throwable cause = e.getCause() != null ? e.getCause() : e;
				if (canceled) {
					addEntry(TranscriptEntry.Role.INFO, "The request has been stopped.");
				} else {
					String message = cause.getMessage();
					addEntry(TranscriptEntry.Role.ERROR, message != null ? message : cause.toString());
				}
			}
		}
	}

	private void stop() {
		if (state != State.RUNNING || session == null) {
			return;
		}
		setState(State.CANCELING);
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
			setState(State.CANCELING);
		}
		session.reset();
		observeAgentContext(session.getAgentContext());
		transcript.clear();
		renderTranscript();
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
		// one line: line breaks (\r\n, \n, or \r) and the indentation around them become a single space
		displayText = displayText.replaceAll("\\s*\\R\\s*", " ");
		if (displayText.length() > MAX_CONTEXT_DISPLAY_LENGTH) {
			displayText = displayText.substring(0, MAX_CONTEXT_DISPLAY_LENGTH) + "...";
		}
		String className = thisValue == null ? "null" : thisValue.getClass().getName();
		return "[this: " + className + " - " + displayText + "]";
	}

	/**
	 * Changes the state and updates the controls that depend on it. These controls depend on the state only.
	 */
	private void setState(State state) {
		this.state = state;
		if (session == null) {
			// the agent is not available => the controls have not been created
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
				return "Ready";
		}
	}

	/*
	 * Transcript
	 */
	private void addEntryLater(Request request, TranscriptEntry entry) {
		SwingUtilities.invokeLater(() -> {
			if (request == currentRequest && !request.isDiscarded()) {
				addEntry(entry);
			}
		});
	}

	private void addEntry(TranscriptEntry.Role role, String text) {
		addEntry(new TranscriptEntry(role, text));
	}

	private void addEntry(TranscriptEntry entry) {
		transcript.add(entry);
		highlightedToolCall = -1;
		renderTranscript();
	}

	private void renderTranscript() {
		renderTranscript(true);
	}

	/**
	 * @param scrollToEnd	if {@code true}, then the transcript is scrolled to the last entry
	 */
	private void renderTranscript(boolean scrollToEnd) {
		StringBuilder html = new StringBuilder("<html><body>");
		List<TranscriptEntry> toolEntries = new ArrayList<>();
		int numToolCalls = 0;
		for (int i = 0; i < transcript.size(); i++) {
			TranscriptEntry entry = transcript.get(i);
			if (entry.getToolName() != null) {
				// consecutive tool calls are rendered as one line
				toolEntries.add(entry);
				boolean lastToolInRow = i + 1 == transcript.size() || transcript.get(i + 1).getToolName() == null;
				if (lastToolInRow) {
					appendToolCalls(html, toolEntries, numToolCalls + 1, highlightedToolCall);
					numToolCalls += toolEntries.size();
					toolEntries.clear();
				}
				continue;
			}
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
		if (scrollToEnd) {
			SwingUtilities.invokeLater(() -> transcriptPane.setCaretPosition(transcriptPane.getDocument().getLength()));
		}
		// the transcript is rendered after every change
		updateToolCalls();
	}

	/**
	 * Renders consecutive tool calls as one line with a link to the tool call dialog. Each tool call gets an
	 * anchor, so the dialog can scroll the transcript to it.
	 *
	 * @param firstNumber			the number of the first tool call (1-based)
	 * @param highlightedNumber		the number of the highlighted tool call or -1
	 */
	private static void appendToolCalls(StringBuilder html, List<TranscriptEntry> toolEntries, int firstNumber, int highlightedNumber) {
		int lastNumber = firstNumber + toolEntries.size() - 1;
		boolean highlighted = firstNumber <= highlightedNumber && highlightedNumber <= lastNumber;
		html.append("<div class=\"").append(highlighted ? "highlightedTool" : "tool").append("\">");
		for (int number = firstNumber; number <= lastNumber; number++) {
			html.append("<a name=\"").append(TOOL_CALL_ANCHOR_PREFIX).append(number).append("\"></a>");
		}
		html.append("&rarr; <a href=\"").append(TOOL_CALLS_LINK_PREFIX).append(firstNumber).append('-').append(lastNumber).append("\">");
		if (toolEntries.size() == 1) {
			html.append(MarkdownToHtml.escape(toolEntries.get(0).getText())).append("</a>");
		} else {
			html.append(toolEntries.size()).append(" tool calls</a>: ").append(MarkdownToHtml.escape(summarizeToolNames(toolEntries)));
		}
		html.append("</div>");
	}

	/**
	 * Returns the tool names in the order of their first occurrence, e.g., "get_fields, evaluate_expression ×2".
	 */
	private static String summarizeToolNames(List<TranscriptEntry> toolEntries) {
		Map<String, Integer> countsByName = new LinkedHashMap<>();
		for (TranscriptEntry entry : toolEntries) {
			countsByName.merge(entry.getToolName(), 1, Integer::sum);
		}
		StringBuilder summary = new StringBuilder();
		for (Map.Entry<String, Integer> nameAndCount : countsByName.entrySet()) {
			if (summary.length() > 0) {
				summary.append(", ");
			}
			summary.append(nameAndCount.getKey());
			if (nameAndCount.getValue() > 1) {
				summary.append(" ×").append(nameAndCount.getValue());
			}
		}
		return summary.toString();
	}

	private void onHyperlinkEvent(HyperlinkEvent e) {
		String description = e.getDescription();
		if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED || description == null || !description.startsWith(TOOL_CALLS_LINK_PREFIX)) {
			return;
		}
		String[] range = description.substring(TOOL_CALLS_LINK_PREFIX.length()).split("-");
		try {
			selectToolCalls(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
			// cannot happen for links created by this class
		}
	}

	/**
	 * Highlights the line of the tool call with the specified number (1-based) in the transcript and scrolls to it.
	 */
	private void showToolCallInTranscript(int number) {
		highlightedToolCall = number;
		renderTranscript(false);
		// scroll after the new document has been laid out
		SwingUtilities.invokeLater(() -> transcriptPane.scrollToReference(TOOL_CALL_ANCHOR_PREFIX + number));
	}

	/*
	 * Tool calls
	 */
	private void showToolCalls() {
		if (!canShowToolCalls()) {
			return;
		}
		openToolCallsDialog();
	}

	/**
	 * Returns {@code true} if the conversation has tool calls and no dialog is open. There is at most one
	 * dialog because it follows the changes anyway.
	 */
	private boolean canShowToolCalls() {
		return !getToolEntries().isEmpty() && toolCallsDialog == null;
	}

	/**
	 * Selects the tool calls {@code first} to {@code last} (1-based) in the dialog. Unlike {@link #showToolCalls()},
	 * this also works if the dialog is already open: It is brought to the front. Otherwise, it is opened.
	 */
	private void selectToolCalls(int first, int last) {
		ToolCallsDialog dialog = toolCallsDialog;
		if (dialog == null) {
			dialog = openToolCallsDialog();
		} else {
			dialog.toFront();
		}
		dialog.selectToolCalls(first, last);
	}

	private ToolCallsDialog openToolCallsDialog() {
		ToolCallsDialog dialog = new ToolCallsDialog(SwingUtilities.getWindowAncestor(this), context, thisValue, transcript, this::showToolCallInTranscript);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				toolCallsDialog = null;
				updateToolCalls();
			}
		});
		toolCallsDialog = dialog;
		updateToolCalls();
		dialog.setVisible(true);
		return dialog;
	}

	/**
	 * Updates the button and the dialog. The dialog is closed when there are no tool calls anymore (new conversation).
	 */
	private void updateToolCalls() {
		List<TranscriptEntry> toolEntries = getToolEntries();
		ToolCallsDialog dialog = toolCallsDialog;
		if (dialog != null) {
			if (toolEntries.isEmpty()) {
				dialog.dispose();
			} else {
				dialog.setTranscript(transcript);
			}
		}
		toolCallsButton.setText("Show Tool Calls (" + toolEntries.size() + ")");
		toolCallsButton.setEnabled(canShowToolCalls());
	}

	private List<TranscriptEntry> getToolEntries() {
		return transcript.stream().filter(entry -> entry.getToolName() != null).collect(Collectors.toList());
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
		List<TranscriptEntry> snapshotTranscript = new ArrayList<>(transcript);
		if (state == State.RUNNING || state == State.CANCELING) {
			snapshotTranscript.add(new TranscriptEntry(TranscriptEntry.Role.INFO, "The request has been interrupted because the inspected object has changed."));
		}
		return new AiSessionSnapshot(snapshotTranscript, session.getResumeState(), session.getAgentContext());
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
		session.restore(snapshot.getResumeState(), snapshot.getTranscript(), snapshot.getAgentContext());
		observeAgentContext(session.getAgentContext());
		renderTranscript();
	}

	@Override
	public void dispose() {
		if (state == State.CLOSED) {
			return;
		}
		setState(State.CLOSED);
		currentRequest = null;
		settings.removeAutoApproveListener(autoApproveListener);
		// the agent context is referenced by the history => do not let them reference this view
		observeAgentContext(null);
		if (agentVariablesDialog != null) {
			agentVariablesDialog.dispose();
		}
		if (toolCallsDialog != null) {
			toolCallsDialog.dispose();
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
		private final AiSession		session;
		private final String		message;
		private final String		contextLine;

		/**
		 * The progress and the result of a discarded request are not shown.
		 */
		private volatile boolean	discarded;

		Request(AiSession session, String message, String contextLine) {
			this.session = session;
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
			// order matters: a request is discarded before the session is reset
			int conversationId = session.getConversationId();
			return discarded ? null : session.sendRequest(conversationId, message, contextLine, this);
		}

		@Override
		protected void done() {
			onRequestFinished(this);
		}

		@Override
		public void onProgress(String text) {
			addEntryLater(this, new TranscriptEntry(TranscriptEntry.Role.PROGRESS, text));
		}

		@Override
		public void onToolUse(String toolName, String summary, JsonObject input) {
			// create the entry immediately: it copies the input
			TranscriptEntry entry = TranscriptEntry.createToolEntry(summary.isEmpty() ? toolName : toolName + ": " + summary, toolName, input);
			addEntryLater(this, entry);
		}
	}
}
