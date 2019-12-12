package dd.kms.marple.gui.common;

import com.google.common.util.concurrent.Runnables;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
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
				Point location = determineWindowLocation(window.getWidth(), window.getHeight());
				MANAGED_WINDOWS.put(identifier, window);
				SwingUtilities.invokeLater(() -> {
					if (location != null) {
						window.setLocation(location);
					}
					window.setMinimumSize(window.getSize());
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

	private static @Nullable Point determineWindowLocation(int width, int height) {
		Alignment[] priorizedAlignments = { Alignment.RIGHT_TO, Alignment.BELOW, Alignment.LEFT_TO, Alignment.ABOVE };
		List<Window> potentialDockingWindows = getPotentialDockingWindows();
		for (Window dockingWindow : potentialDockingWindows) {
			for (Alignment alignment : priorizedAlignments) {
				for (SecondaryCoordinate secondaryCoordinate : SecondaryCoordinate.values()) {
					Rectangle windowBounds = determineBounds(dockingWindow.getBounds(), alignment, secondaryCoordinate, width, height);
					if (areBoundsValid(windowBounds)) {
						return windowBounds.getLocation();
					}
				}
			}
		}
		return null;
	}

	private static List<Window> getPotentialDockingWindows() {
		List<Window> candidates = new ArrayList<>();
		Window focusedWindow = FocusManager.getCurrentManager().getFocusedWindow();
		candidates.add(focusedWindow);
		candidates.addAll(MANAGED_WINDOWS.values());
		candidates.addAll(Arrays.asList(JFrame.getFrames()));
		candidates.addAll(Arrays.asList(Window.getWindows()));
		candidates.removeIf(window -> !window.isVisible());
		return candidates;
	}

	private static Rectangle determineBounds(Rectangle dockingRectangle, Alignment alignment, SecondaryCoordinate secondaryCoordinate, int width, int height) {
		int x = 0, y = 0;
		switch (alignment) {
			case LEFT_TO: {
				int right = dockingRectangle.x;
				x = right - width;
				break;
			}
			case RIGHT_TO: {
				x = dockingRectangle.x + dockingRectangle.width;
				break;
			}
			case ABOVE: {
				int bottom = dockingRectangle.y;
				y = bottom - height;
				break;
			}
			case BELOW: {
				y = dockingRectangle.y + dockingRectangle.height;
				break;
			}
			default:
				throw new IllegalArgumentException("Unsupported alignment: " + alignment);
		}

		switch (secondaryCoordinate) {
			case LOWER: {
				if (alignment == Alignment.LEFT_TO || alignment == Alignment.RIGHT_TO) {
					y = dockingRectangle.y;
				} else {
					x = dockingRectangle.x;
				}
				break;
			}
			case UPPER: {
				if (alignment == Alignment.LEFT_TO || alignment == Alignment.RIGHT_TO) {
					int bottom = dockingRectangle.y + dockingRectangle.height;
					y = bottom - height;
				} else {
					int right = dockingRectangle.x + dockingRectangle.width;
					x = right - width;
				}
				break;
			}
			default:
				throw new IllegalArgumentException("Unsupported secondary coordinate: " + secondaryCoordinate);
		}

		return new Rectangle(x, y, width, height);
	}

	private static boolean areBoundsValid(Rectangle bounds) {
		return !boundsIntersectWithWindows(bounds) && boundsAreOnOneScreen(bounds);
	}

	private static boolean boundsIntersectWithWindows(Rectangle bounds) {
		for (Window window : MANAGED_WINDOWS.values()) {
			if (bounds.intersects(window.getBounds())) {
				return true;
			}
		}
		return false;
	}

	private static boolean boundsAreOnOneScreen(Rectangle bounds) {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice device : graphicsEnvironment.getScreenDevices()) {
			if (device.getDefaultConfiguration().getBounds().contains(bounds)) {
				return true;
			}
		}
		return false;
	}

	private enum Alignment { LEFT_TO, RIGHT_TO, ABOVE, BELOW };
	private enum SecondaryCoordinate { LOWER, UPPER };
}
