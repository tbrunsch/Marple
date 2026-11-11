package dd.kms.marple.impl.aiagent;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Keeps track of all agent processes and terminates them when the JVM shuts down. This
 * ensures that no agent process survives the inspected application.
 */
public class ProcessRegistry
{
	/*
	 * TODO: Process.destroyForcibly() only kills the process itself, not its child processes. If the
	 *  executable is a wrapper script (e.g., "claude.cmd" installed via npm), then the actual agent
	 *  process survives cancel/close (here and in ClaudeCodeAgent). Killing descendants requires
	 *  ProcessHandle.descendants() (Java 9+) or platform-specific commands (e.g., "taskkill /T").
	 */
	private static final Set<Process>	PROCESSES				= Collections.newSetFromMap(new IdentityHashMap<>());
	private static boolean				shutdownHookRegistered	= false;

	public static synchronized void register(Process process) {
		if (!shutdownHookRegistered) {
			Thread shutdownHook = new Thread(ProcessRegistry::destroyAll, "Marple AI agent process cleanup");
			Runtime.getRuntime().addShutdownHook(shutdownHook);
			shutdownHookRegistered = true;
		}
		PROCESSES.add(process);
	}

	public static synchronized void unregister(Process process) {
		PROCESSES.remove(process);
	}

	/**
	 * Asks the process to terminate by closing its standard input and forcibly terminates it if
	 * it is still alive after the specified time. This method does not block. It does not unregister
	 * the process: This is the caller's responsibility once the process has terminated.
	 */
	public static void terminateGracefully(Process process, Closeable stdin, long gracePeriodMs) {
		Thread terminator = new Thread(() -> {
			try {
				try {
					stdin.close();
				} catch (IOException e) {
					/* ignore */
				}
				if (!process.waitFor(gracePeriodMs, TimeUnit.MILLISECONDS)) {
					process.destroyForcibly();
				}
			} catch (InterruptedException e) {
				process.destroyForcibly();
			}
		}, "Marple AI agent process terminator");
		terminator.setDaemon(true);
		terminator.start();
	}

	private static void destroyAll() {
		List<Process> processes;
		synchronized (ProcessRegistry.class) {
			processes = new ArrayList<>(PROCESSES);
			PROCESSES.clear();
		}
		for (Process process : processes) {
			process.destroyForcibly();
		}
	}
}
