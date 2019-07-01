package dd.kms.marple.actions.component;

import dd.kms.marple.gui.common.Screenshots;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.awt.GridBagConstraints.*;

class ScreenshotPanel extends JPanel
{
	private static final Insets				DEFAULT_INSETS		= new Insets(3, 3, 3, 3);

	private static final KeyStroke			CTRL_C				= KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK);
	private static final KeyStroke			CTRL_S				= KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);

	private static File			lastSelectedFile	= null;

	private final ImagePanel	imagePanel		= new ImagePanel();

	private final JButton		saveButton		= new JButton("Save");
	private final JButton		copyButton		= new JButton("Copy to clipboard");

	private BufferedImage		screenshot;

	ScreenshotPanel() {
		super(new GridBagLayout());

		add(imagePanel,	new GridBagConstraints(0, 0, REMAINDER, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		add(saveButton,	new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));
		add(copyButton,	new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));

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

	void setScreenshot(BufferedImage screenshot) {
		this.screenshot = screenshot;
		imagePanel.setImage(screenshot);
	}

	private void copyImageToClipboard() {
		Screenshots.copyToClipboard(screenshot);
	}

	private void saveImage() {
		File directory = suggestDirectoryForSaving();
		JFileChooser fileChooser = new JFileChooser(directory);
		fileChooser.setDialogTitle("Save Screenshot");
		if (directory != null) {
			fileChooser.setCurrentDirectory(directory);
		}
		for (FileFilter fileFilter : Screenshots.IMAGE_FILE_FILTERS) {
			fileChooser.addChoosableFileFilter(fileFilter);
		}

		final FileFilter fileFilter;
		if (lastSelectedFile == null) {
			fileFilter = Screenshots.getFileFilter("png");
		} else {
			fileChooser.setSelectedFile(lastSelectedFile);
			fileFilter = Screenshots.getFileFilter(Screenshots.getExtension(lastSelectedFile));
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
		if (!Screenshots.isSupportedImageFile(file)) {
			FileFilter selectedFileFilter = fileChooser.getFileFilter();
			String extension = Screenshots.getExtension(selectedFileFilter);
			if (extension != null) {
				file = new File(file.getPath() + "." + extension);
			}
		}
		lastSelectedFile = file;
		Screenshots.save(screenshot, file, this);
	}

	private @Nullable File suggestDirectoryForSaving() {
		if (lastSelectedFile == null) {
			return null;
		}
		File directory = lastSelectedFile.getParentFile();
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

			imageScrollPane.getViewport().setBackground(Color.WHITE);
		}

		void setImage(Image image) {
			imageLabel.setIcon(new ImageIcon(image));
		}
	}
}
