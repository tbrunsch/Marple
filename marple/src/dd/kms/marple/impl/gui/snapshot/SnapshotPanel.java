package dd.kms.marple.impl.gui.snapshot;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.common.CurrentObjectPanel;
import dd.kms.zenodot.api.wrappers.InfoProvider;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.function.Function;

import static java.awt.GridBagConstraints.*;

public class SnapshotPanel extends JPanel
{
	private static final Insets		DEFAULT_INSETS				= new Insets(3, 3, 3, 3);

	private static final KeyStroke	CTRL_C						= KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK);
	private static final KeyStroke	CTRL_S						= KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);

	private static final long		LIVE_SCREENSHOT_INTERVAL_MS	= 500;

	private static File				LAST_SELECTED_FILE			= null;

	private final CurrentObjectPanel	currentObjectPanel;

	private final ImagePanel			imagePanel			= new ImagePanel();
	private final JCheckBox				livePreviewCB 		= new JCheckBox("live preview");

	private final JButton				saveButton			= new JButton("Save");
	private final JButton				copyButton			= new JButton("Copy to clipboard");

	private BufferedImage				screenshot;

	private WeakReference<Object>		lastSnapshotTarget	= new WeakReference<>(null);
	private long						lastSnapshotTimeMs	= 0;

	public SnapshotPanel(InspectionContext context) {
		super(new GridBagLayout());

		currentObjectPanel = new CurrentObjectPanel(context);

		int yPos = 0;
		add(currentObjectPanel,	new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		add(imagePanel,			new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		add(livePreviewCB,		new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
		add(saveButton,			new GridBagConstraints(1, yPos,   1, 1, 1.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));
		add(copyButton,			new GridBagConstraints(2, yPos++, 1, 1, 0.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));

		livePreviewCB.setToolTipText("Select this option if you want to take a screenshot of components when hovering over component objects in the inspector");

		setPreferredSize(new Dimension(400, 400));

		addListeners();
	}

	private void addListeners() {
		mapInput(CTRL_C, this::copyImageToClipboard);
		mapInput(CTRL_S, this::saveImage);

		saveButton.addActionListener(e -> saveImage());
		copyButton.addActionListener(e -> copyImageToClipboard());
	}

	private void mapInput(KeyStroke keyStroke, Runnable runnable) {
		ActionMap actionMap = getActionMap();
		InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(keyStroke, runnable);
		actionMap.put(runnable, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runnable.run();
			}
		});
	}

	public <T> void takeSnapshot(T snapshotTarget, Function<T, BufferedImage> snapshotFunction) {
		lastSnapshotTarget = new WeakReference<>(snapshotTarget);
		lastSnapshotTimeMs = System.currentTimeMillis();
		this.screenshot = snapshotFunction.apply(snapshotTarget);
		currentObjectPanel.setCurrentObject(InfoProvider.createObjectInfo(snapshotTarget));
		imagePanel.setImage(screenshot);
	}

	public <T> void takeLiveSnapshot(T snapshotTarget, Function<T, BufferedImage> snapshotFunction) {
		if (!livePreviewCB.isSelected()) {
			return;
		}
		if (snapshotTarget == lastSnapshotTarget.get() && System.currentTimeMillis() < lastSnapshotTimeMs + LIVE_SCREENSHOT_INTERVAL_MS) {
			return;
		}
		takeSnapshot(snapshotTarget, snapshotFunction);
	}

	private void copyImageToClipboard() {
		Snapshots.copyToClipboard(screenshot);
	}

	private void saveImage() {
		File directory = suggestDirectoryForSaving();
		JFileChooser fileChooser = new JFileChooser(directory);
		fileChooser.setDialogTitle("Save Screenshot");
		if (directory != null) {
			fileChooser.setCurrentDirectory(directory);
		}
		for (FileFilter fileFilter : Snapshots.IMAGE_FILE_FILTERS) {
			fileChooser.addChoosableFileFilter(fileFilter);
		}

		final FileFilter fileFilter;
		if (LAST_SELECTED_FILE == null) {
			fileFilter = Snapshots.getFileFilter("png");
		} else {
			fileChooser.setSelectedFile(LAST_SELECTED_FILE);
			fileFilter = Snapshots.getFileFilter(Snapshots.getExtension(LAST_SELECTED_FILE));
		}
		if (fileFilter != null) {
			fileChooser.setFileFilter(fileFilter);
		}
		if (fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(this)) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = fileChooser.getSelectedFile();
		if (file == null) {
			return;
		}
		if (!Snapshots.isSupportedImageFile(file)) {
			FileFilter selectedFileFilter = fileChooser.getFileFilter();
			String extension = Snapshots.getExtension(selectedFileFilter);
			if (extension != null) {
				file = new File(file.getPath() + "." + extension);
			}
		}
		LAST_SELECTED_FILE = file;
		Snapshots.save(screenshot, file, this);
	}

	private @Nullable File suggestDirectoryForSaving() {
		if (LAST_SELECTED_FILE == null) {
			return null;
		}
		File directory = LAST_SELECTED_FILE.getParentFile();
		return directory != null && directory.exists() ? directory : null;
	}

	private static class ImagePanel extends JPanel
	{
		private final JLabel		imageLabel		= new JLabel();
		private final JScrollPane	imageScrollPane	= new JScrollPane(imageLabel);

		ImagePanel() {
			super(new GridBagLayout());

			setBorder(BorderFactory.createTitledBorder("Preview"));

			add(imageScrollPane,	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

			imageLabel.setVerticalAlignment(JLabel.TOP);
			imageScrollPane.getViewport().setBackground(Color.WHITE);
		}

		void setImage(Image image) {
			imageLabel.setIcon(new ImageIcon(image));
		}

		int getImageAreaWidth() {
			return imageLabel.getWidth();
		}

		int getImageAreaHeight() {
			return imageLabel.getHeight();
		}
	}
}
