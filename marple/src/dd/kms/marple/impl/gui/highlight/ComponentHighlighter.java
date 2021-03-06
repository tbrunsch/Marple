package dd.kms.marple.impl.gui.highlight;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

public class ComponentHighlighter implements Runnable
{
	private static final long	REPAINT_INTERVAL_MS	= 100;
	private static final long	ANIMATION_TIME_MS	= 1000;
	private static final int	NUM_SEQUENCES		= 3;

	private static final Color	TARGET_COLOR		= Color.GREEN;

	private static final Object	LOCK				= new Object();

	private final Component	component;

	public ComponentHighlighter(Component component) {
		this.component = component;
	}

	@Override
	public void run() {
		long startTimeMs = System.currentTimeMillis();
		long durationMs = NUM_SEQUENCES * ANIMATION_TIME_MS;
		synchronized (LOCK) {
			Color originalColor = component.getBackground();
			boolean originalOpaque = component.isOpaque();
			try {
				setOpaque(true);
				long elapsedTimeMs;
				while ((elapsedTimeMs = System.currentTimeMillis() - startTimeMs) < durationMs) {
					Color color = computeColor(originalColor, elapsedTimeMs);
					SwingUtilities.invokeAndWait(() -> setColor(color));
					Thread.sleep(REPAINT_INTERVAL_MS);
				}
			} catch (InterruptedException | InvocationTargetException ignored) {
				/* nothing we can do here */
			} finally {
				setColor(originalColor);
				setOpaque(originalOpaque);
			}
		}
	}

	private Color computeColor(Color originalColor, long elapsedTimeMs) {
		double c = determineColorCoefficient(elapsedTimeMs);
		Color blendedColor = blendColors(originalColor, TARGET_COLOR, c);
		return blendedColor;
	}

	private double determineColorCoefficient(long elapsedTimeMs) {
		long normalizedTimeMs = elapsedTimeMs;
		while (normalizedTimeMs - ANIMATION_TIME_MS >= 0) {
			normalizedTimeMs -= ANIMATION_TIME_MS;
		}
		return 0.5*(1 + Math.cos(normalizedTimeMs*2*Math.PI/ANIMATION_TIME_MS));
	}

	private Color blendColors(Color color1, Color color2, double c) {
		return new Color(
			interpolate(color1.getRed(),	color2.getRed(),	c),
			interpolate(color1.getGreen(),	color2.getGreen(),	c),
			interpolate(color1.getBlue(),	color2.getBlue(),	c)
		);
	}

	private int interpolate(int i1, int i2, double c) {
		return (int) (c*i1 + (1-c)*i2);
	}

	private void setColor(Color color) {
		SwingUtilities.invokeLater(() -> setColorInUiThread(color));
	}

	private void setColorInUiThread(Color color) {
		component.setBackground(color);
		component.revalidate();
	}

	private void setOpaque(boolean opaque) {
		SwingUtilities.invokeLater(() -> setOpaqueInUiThread(opaque));
	}

	private void setOpaqueInUiThread(boolean opaque) {
		if (component instanceof JLabel) {
			((JLabel) component).setOpaque(opaque);
			component.revalidate();
		}
	}
}
