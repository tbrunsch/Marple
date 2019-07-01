package dd.kms.marple.gui.common;

import com.google.common.base.Strings;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Screenshots
{
	public static final List<ExtensionFileFilter> IMAGE_FILE_FILTERS	= Arrays.stream(ImageIO.getWriterFileSuffixes())
		.map(String::toLowerCase)
		.distinct()
		.sorted()
		.map(ExtensionFileFilter::new)
		.collect(Collectors.toList());

	/*
	 * Adapted from Screen Image by Rob Camick (https://tips4java.wordpress.com/2008/10/13/screen-image/)
	 */
	public static BufferedImage takeScreenshot(JComponent component) {
		if (!component.isDisplayable()) {
			Dimension size = component.getSize();
			if (size.width == 0 || size.height == 0) {
				component.setSize(component.getPreferredSize());
			}
			layoutComponent( component );
		}

		Dimension size = component.getSize();
		BufferedImage screenshot = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = null;
		try {
			g = screenshot.createGraphics();
			if (!component.isOpaque()) {
				g.setColor(component.getBackground());
				g.fillRect(0, 0, size.width, size.height);
			}
			component.print(g);
		} finally {
			if (g != null) {
				g.dispose();
			}
		}
		return screenshot;
	}

	private static void layoutComponent(Component component) {
		synchronized (component.getTreeLock()) {
			component.doLayout();
			if (component instanceof Container) {
				Container container = (Container) component;
				for (Component child : container.getComponents()) {
					layoutComponent(child);
				}
			}
		}
	}

	public static void copyToClipboard(BufferedImage screenshot) {
		TransferableImage transferableImage = new TransferableImage(screenshot);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(transferableImage, null);
	}

	public static void save(BufferedImage screenshot, File file, JComponent parent) {
		String extension = Strings.nullToEmpty(getExtension(file));
		if (!isSupportedImageFile(file)) {
			showExportError("The image extension '" + extension + "' is not supported.", parent);
			return;
		}
		if (file.exists() && !file.isFile()) {
			showExportError("The specified target is no file.", parent);
			return;
		}
		if (file.exists()) {
			int userDecision = JOptionPane.showConfirmDialog(parent,
				"The specified file does already exist. Overwrite?",
				"Image Export",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE
			);
			if (userDecision != JOptionPane.YES_OPTION) {
				return;
			}
		}
		try {
			ImageIO.write(screenshot, extension, file);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parent, "Exception when saving file: " + e.getClass().getSimpleName() + "(" + e.getMessage() + ")");
		}
	}

	public static boolean isSupportedImageFile(File file) {
		return file != null && IMAGE_FILE_FILTERS.stream().anyMatch(filter -> filter.accept(file));
	}

	public static String getExtension(File file) {
		if (file == null) {
			return null;
		}
		String fileName = file.getName();
		int dotPosition = fileName.lastIndexOf('.');
		return dotPosition < 0 ? null : fileName.substring(dotPosition + 1);
	}

	public static String getExtension(FileFilter fileFilter) {
		return fileFilter instanceof ExtensionFileFilter
			? ((ExtensionFileFilter) fileFilter).getExtension()
			: null;
	}

	public static FileFilter getFileFilter(String extension) {
		return IMAGE_FILE_FILTERS.stream().filter(filter -> filter.getExtension().equalsIgnoreCase(extension)).findFirst().orElse(null);
	}

	private static void showExportError(String message, JComponent parent) {
		JOptionPane.showMessageDialog(parent, message, "Export Error", JOptionPane.ERROR_MESSAGE);
	}

	private static class TransferableImage implements Transferable
	{
		private final Image	image;

		private TransferableImage(Image image) {
			this.image = image;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{ DataFlavor.imageFlavor };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (isDataFlavorSupported(flavor) && image != null) {
				return image;
			}
			throw new UnsupportedFlavorException(flavor);
		}
	}

	private static class ExtensionFileFilter extends FileFilter
	{
		private final String	extension;

		private ExtensionFileFilter(String extension) {
			this.extension = extension;
		}

		@Override
		public boolean accept(File file) {
			return file != null
				&& (file.isDirectory() || extension.equalsIgnoreCase(Screenshots.getExtension(file)));
		}

		@Override
		public String getDescription() {
			return extension + " image (*." + extension + ")";
		}

		String getExtension() {
			return extension;
		}
	}
}
