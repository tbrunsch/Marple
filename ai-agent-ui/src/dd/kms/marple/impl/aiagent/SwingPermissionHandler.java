package dd.kms.marple.impl.aiagent;

import dd.kms.marple.api.aiagent.backend.PermissionDecision;
import dd.kms.marple.api.aiagent.backend.PermissionHandler;
import dd.kms.marple.api.aiagent.backend.PermissionRequest;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

/**
 * Asks the user via a modal dialog whether the agent may perform an action, e.g., change a file.
 * File changes are rejected without asking if the scope of the current request is {@link Scope#RUNNING_APPLICATION}.
 * Otherwise, if "Auto approve" is enabled, all actions are allowed without asking. The settings are checked on the
 * event dispatch thread, so a request that waits for an open dialog is approved if the user enables "Auto approve" there.
 */
class SwingPermissionHandler implements PermissionHandler
{
	private final AiAgentViewSettings	settings;
	/**
	 * The dialog is owned by the window that contains this component when the dialog is shown
	 */
	private final Component				parent;
	/**
	 * Provides the scope of the current request. It is called on the event dispatch thread.
	 */
	private final Supplier<Scope>		scopeSupplier;

	SwingPermissionHandler(AiAgentViewSettings settings, Component parent, Supplier<Scope> scopeSupplier) {
		this.settings = settings;
		this.parent = parent;
		this.scopeSupplier = scopeSupplier;
	}

	@Override
	public PermissionDecision requestPermission(PermissionRequest request) {
		try {
			return EventDispatchThreadUtils.callAndWait(() -> decide(request));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return PermissionDecision.deny("Interrupted while waiting for the user's decision");
		} catch (InvocationTargetException e) {
			return PermissionDecision.deny("Error while asking the user: " + e.getCause());
		}
	}

	private PermissionDecision decide(PermissionRequest request) {
		if (request.isFileChange() && scopeSupplier.get() == Scope.RUNNING_APPLICATION) {
			return PermissionDecision.deny("The scope of the user's request is \"" + Scope.RUNNING_APPLICATION + "\": "
				+ "Do not change files. Apply changes to the running application, or describe the required file change "
				+ "and tell the user to select the scope \"" + Scope.FILES + "\" if the files should be changed.");
		}
		return settings.isAutoApprove() ? PermissionDecision.allow() : showDialog(request);
	}

	private PermissionDecision showDialog(PermissionRequest request) {
		Window owner = SwingUtilities.getWindowAncestor(parent);
		ApprovalDialog dialog = new ApprovalDialog(owner, request);
		dialog.setVisible(true);	// blocks because the dialog is modal

		if (dialog.isApproved()) {
			settings.setAutoApprove(dialog.isAutoApproveSelected());
			return PermissionDecision.allow();
		}
		String reason = dialog.getRejectionReason();
		return PermissionDecision.deny(reason.isEmpty()
			? "The user rejected the change."
			: "The user rejected the change: " + reason);
	}

	private static class ApprovalDialog extends JDialog
	{
		private final JCheckBox		autoApproveCheckBox	= new JCheckBox("Auto approve further requests (until the application is restarted)");
		private final JTextField	reasonTextField		= new JTextField();
		private boolean				approved			= false;

		ApprovalDialog(Window owner, PermissionRequest request) {
			super(owner, "AI agent requests permission", ModalityType.APPLICATION_MODAL);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);

			String filePath = request.getFilePath();
			String description = "<html>The AI agent wants to use <b>" + MarkdownToHtml.escape(request.getToolName()) + "</b>"
				+ (filePath != null ? " on<br><b>" + MarkdownToHtml.escape(filePath) + "</b>" : "")
				+ "</html>";

			JTextArea previewArea = new JTextArea(request.getPreview());
			previewArea.setEditable(false);
			previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, previewArea.getFont().getSize()));
			previewArea.setCaretPosition(0);
			JScrollPane previewScrollPane = new JScrollPane(previewArea);
			previewScrollPane.setPreferredSize(new Dimension(750, 400));

			JButton approveButton = new JButton("Approve");
			JButton rejectButton = new JButton("Reject");
			approveButton.addActionListener(e -> {
				approved = true;
				dispose();
			});
			rejectButton.addActionListener(e -> {
				approved = false;
				dispose();
			});

			JPanel reasonPanel = new JPanel(new BorderLayout(5, 0));
			reasonPanel.add(new JLabel("Reason for rejection (optional):"), BorderLayout.WEST);
			reasonPanel.add(reasonTextField, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(approveButton);
			buttonPanel.add(rejectButton);

			JPanel southPanel = new JPanel(new GridLayout(0, 1, 0, 3));
			southPanel.add(reasonPanel);
			southPanel.add(autoApproveCheckBox);
			southPanel.add(buttonPanel);

			JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
			contentPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
			contentPanel.add(new JLabel(description), BorderLayout.NORTH);
			contentPanel.add(previewScrollPane, BorderLayout.CENTER);
			contentPanel.add(southPanel, BorderLayout.SOUTH);
			setContentPane(contentPanel);

			getRootPane().setDefaultButton(approveButton);
			pack();
			setLocationRelativeTo(owner);
		}

		boolean isApproved() {
			return approved;
		}

		boolean isAutoApproveSelected() {
			return autoApproveCheckBox.isSelected();
		}

		String getRejectionReason() {
			return reasonTextField.getText().trim();
		}
	}
}
