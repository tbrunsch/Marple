package dd.kms.marple.impl.aiagent;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Executes code in the event dispatch thread. If the current thread is the event dispatch thread,
 * then the code is executed directly.
 */
public class EventDispatchThreadUtils
{
	/**
	 * Executes the supplier in the event dispatch thread and waits for its result.
	 *
	 * @throws InvocationTargetException	if the supplier throws an exception. The exception is wrapped no matter
	 *                                      whether the supplier is executed directly or in another thread.
	 * @throws InterruptedException			if the current thread is interrupted while waiting
	 */
	public static <T> T callAndWait(Supplier<T> supplier) throws InterruptedException, InvocationTargetException {
		if (SwingUtilities.isEventDispatchThread()) {
			try {
				return supplier.get();
			} catch (Throwable t) {
				throw new InvocationTargetException(t);
			}
		}
		AtomicReference<T> result = new AtomicReference<>();
		SwingUtilities.invokeAndWait(() -> result.set(supplier.get()));
		return result.get();
	}

	/**
	 * Executes the runnable in the event dispatch thread without waiting for it: directly if the current
	 * thread is the event dispatch thread, later otherwise.
	 */
	public static void runNowOrLater(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}
}
