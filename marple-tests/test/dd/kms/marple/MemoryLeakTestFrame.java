package dd.kms.marple;

import dd.kms.marple.instancesearch.InstancePath;
import dd.kms.marple.instancesearch.InstancePathFinder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

class MemoryLeakTestFrame extends JFrame
{
	private final InstancePathFinder	instancePathFinder;

	MemoryLeakTestFrame() {
		super("Memory Leak Test Frame");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		instancePathFinder = new InstancePathFinder(this::onNewPathDetected);

		JPanel panel = new JPanel(new FlowLayout());

		JButton scanButton = new JButton("Scan");
		scanButton.setEnabled(false);
		scanButton.addActionListener(e -> instancePathFinder.search(new InstancePath(this, "this", null), ChildDialog.class, ChildDialog.class::isInstance, true));

		JButton createDialogButton = new JButton("Create child dialog");
		createDialogButton.addActionListener(e -> {
			openChildDialog();
			scanButton.setEnabled(true);
		});

		panel.add(createDialogButton);
		panel.add(scanButton);
		getContentPane().add(panel);
	}

	private void onNewPathDetected(InstancePath path) {
		System.out.println("Status: " + instancePathFinder.getStatusText());
		System.out.println(path);
	}

	private void openChildDialog() {
		ChildDialog dialog = new ChildDialog(this);
		dialog.pack();
		dialog.setVisible(true);
	}
}

class ChildDialog extends JDialog implements WindowFocusListener
{
	static ChildDialog	FIRST_INSTANCE	= null;

	ChildDialog(MemoryLeakTestFrame owner) {
		if (FIRST_INSTANCE == null) {
			FIRST_INSTANCE = this;
		}

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		getContentPane().add(new JLabel("<html><p>The dialog remains registered as listener of the owner frame after disposal</p></html>"));

		// register as listener which will not be removed on disposal => memory leak
		owner.addWindowFocusListener(this);
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
		/* do nothing */
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
		/* do nothing */
	}
}