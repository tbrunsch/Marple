package dd.kms.marple.gui.common;

import com.google.common.util.concurrent.Runnables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WindowManager
{
	private static final Map<Object, Window>	MANAGED_WINDOWS	= new LinkedHashMap<>();
	private static final Object					LOCK			= new Object();

	public static <T extends Component> void showInFrame(String title, Supplier<T> componentSupplier, Consumer<T> componentConfigurator, Consumer<T> contentUpdater) {
		Supplier<JFrame> frameConstructor = () -> createComponentFrame(title, componentSupplier, contentUpdater);
		JFrame window = getWindow(title, frameConstructor, Runnables.doNothing());
		if (window == null) {
			return;
		}
		window.requestFocus();
		configureComponent(window, componentConfigurator);
	}

	public static <T extends Component> void configureComponent(JFrame window, Consumer<T> componentConfigurator) {
		if (window == null) {
			return;
		}
		Component[] components = window.getContentPane().getComponents();
		if (components.length == 0) {
			return;
		}
		T component = (T) components[0];
		componentConfigurator.accept(component);
	}

	public static void updateFrameOnFocusGained(JFrame frame, Runnable contentUpdater) {
		frame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				contentUpdater.run();
			}
		});
	}

	public static <T extends Window> T getWindow(Object identifier, Supplier<T> windowCreator, Runnable windowDestructor) {
		synchronized (LOCK) {
			final T window;
			if (MANAGED_WINDOWS.containsKey(identifier)) {
				window = (T) MANAGED_WINDOWS.get(identifier);
			} else {
				window = windowCreator.get();
				if (window == null) {
					return null;
				}
				window.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				window.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						synchronized (LOCK) {
							MANAGED_WINDOWS.remove(identifier);
							windowDestructor.run();
						}
					}
				});
				window.pack();
				Point location = GuiCommons.getMousePositionOnScreen();
				MANAGED_WINDOWS.put(identifier, window);
				SwingUtilities.invokeLater(() -> {
					window.setMinimumSize(window.getSize());
					Point correctedLocation = getValidScreenLocation(new Rectangle(location.x, location.y, window.getWidth(), window.getHeight()));
					window.setLocation(correctedLocation);
				});
			}
			SwingUtilities.invokeLater(() -> {
				if (window instanceof Frame) {
					((Frame) window).setExtendedState(Frame.NORMAL);
				}
				window.setVisible(true);
				window.toFront();
			});
			return window;
		}
	}

	private static <T extends Component> JFrame createComponentFrame(String title, Supplier<T> componentSupplier, Consumer<T> contentUpdater) {
		JFrame frame = new JFrame(title);
		T component = componentSupplier.get();
		frame.getContentPane().add(component);
		updateFrameOnFocusGained(frame, () -> contentUpdater.accept(component));
		return frame;
	}

	private static Point getValidScreenLocation(Rectangle bounds) {
		Point location = bounds.getLocation();
		Rectangle screenBounds = getBoundsOfScreen(location);
		if (screenBounds.contains(bounds)) {
			return location;
		}
		int xMin = screenBounds.x;
		int xMax = screenBounds.x + screenBounds.width - bounds.width;
		int yMin = screenBounds.y;
		int yMax = screenBounds.y + screenBounds.height - bounds.height;
		int x = Math.max(Math.min(location.x, xMax), xMin);
		int y = Math.max(Math.min(location.y, yMax), yMin);
		return new Point(x, y);
	}

	private static Rectangle getBoundsOfScreen(Point location) {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screenDevices = graphicsEnvironment.getScreenDevices();
		if (screenDevices.length == 0) {
			throw new IllegalStateException("No screen devices found");
		}
		for (GraphicsDevice device : screenDevices) {
			GraphicsConfiguration defaultConfiguration = device.getDefaultConfiguration();
			Rectangle screenBounds = defaultConfiguration.getBounds();
			if (screenBounds.contains(location)) {
				return getEffectiveScreenBounds(device);
			}
		}
		return getEffectiveScreenBounds(screenDevices[0]);
	}

	/**
	 * @return The screen size without the task bar
	 */
	private static Rectangle getEffectiveScreenBounds(GraphicsDevice device) {
		GraphicsConfiguration defaultConfiguration = device.getDefaultConfiguration();
		Rectangle screenBounds = defaultConfiguration.getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(defaultConfiguration);
		/*
		 * The screen insets seem to describe how much space the task bar occupies in the left,
		 * the right, the top, and the bottom part of the screen.
		 */
		int x = screenBounds.x + screenInsets.left;
		int y = screenBounds.y + screenInsets.top;
		int width = screenBounds.width - (screenInsets.left + screenInsets.right);
		int height = screenBounds.height - (screenInsets.top + screenInsets.bottom);
		return new Rectangle(x, y, width, height);
	}
}
