package dd.kms.marple.impl.aiagent.claude;

import dd.kms.marple.impl.aiagent.ProcessRegistry;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A started Claude Code process. The stdout lines are collected by a reader thread, the last
 * lines of stderr are kept for error messages. The process owns temporary files (e.g., the
 * MCP configuration). When the process has terminated, no matter why, it releases its resources
 * itself: It unregisters from the {@link ProcessRegistry}, deletes the temporary files, and closes stdin.
 * <p>
 * Thread safety: All methods may be called from any thread.
 */
class ClaudeCodeProcess
{
	private static final long		GRACE_PERIOD_MS		= 3000;
	private static final int		MAX_STDERR_LINES	= 20;

	private final Process				process;
	private final Writer				stdin;
	private final BlockingQueue<String>	stdoutLines		= new LinkedBlockingQueue<>();
	private final Deque<String>			stderrLines		= new ArrayDeque<>();
	private final List<Path>			tempFiles;
	/**
	 * Set by the stdout reader thread after it has added the last line to {@link #stdoutLines}.
	 * Hence, no lines are added anymore once this flag is set.
	 */
	private volatile boolean			endOfStdout;

	/**
	 * Starts the process. On success, the process takes over the responsibility for deleting the
	 * temporary files.
	 */
	static ClaudeCodeProcess start(List<String> command, Path workingDirectory, List<Path> tempFiles) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(command)
			.directory(workingDirectory.toFile());
		// avoid that Claude Code thinks it is started from within another Claude Code session
		processBuilder.environment().remove("CLAUDECODE");
		Process process;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new IOException("Cannot start Claude Code ('" + command.get(0) + "'). Is it installed and on the PATH? " + e.getMessage(), e);
		}
		ProcessRegistry.register(process);
		return new ClaudeCodeProcess(process, tempFiles);
	}

	static Path writeTempFile(String prefix, String suffix, String content, List<Path> tempFiles) throws IOException {
		Path file = Files.createTempFile(prefix, suffix);
		tempFiles.add(file);
		file.toFile().deleteOnExit();
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
		return file;
	}

	static void deleteTempFiles(List<Path> tempFiles) {
		for (Path file : tempFiles) {
			try {
				Files.deleteIfExists(file);
			} catch (IOException e) {
				/* ignore; file will be deleted on exit */
			}
		}
	}

	private ClaudeCodeProcess(Process process, List<Path> tempFiles) {
		this.process = process;
		this.stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
		this.tempFiles = tempFiles;
		startReader(process.getInputStream(), stdoutLines::add, this::onEndOfStdout, "stdout");
		startReader(process.getErrorStream(), this::addStderrLine, () -> {}, "stderr");
	}

	boolean isAlive() {
		return process.isAlive();
	}

	void writeLine(String line) throws IOException {
		stdin.write(line);
		stdin.write("\n");
		stdin.flush();
	}

	/**
	 * @return the next line of stdout or {@code null} if no line is available within the specified time.
	 * If stdout ends while waiting, then {@code null} is returned and the next call throws an exception.
	 * @throws EOFException if stdout has ended and all lines have been consumed
	 */
	@Nullable
	String pollLine(long timeoutMs) throws InterruptedException, EOFException {
		/*
		 * Read the flag before polling: Once it is set, no lines are added anymore. Hence, if the
		 * flag is set and the queue is empty, then all lines have been consumed.
		 */
		boolean endOfStdout = this.endOfStdout;
		String line = endOfStdout
			? stdoutLines.poll()
			: stdoutLines.poll(timeoutMs, TimeUnit.MILLISECONDS);
		if (line == null && endOfStdout) {
			throw new EOFException("End of stdout");
		}
		return line;
	}

	/**
	 * @return the exit code (if the process has terminated) and the last lines of stderr, prefixed
	 * by a space or a line break; empty if there are no details
	 */
	String getErrorDetails() {
		String exitCode = process.isAlive() ? "" : " (exit code " + process.exitValue() + ")";
		synchronized (stderrLines) {
			return stderrLines.isEmpty() ? exitCode : exitCode + "\n" + String.join("\n", stderrLines);
		}
	}

	/**
	 * Terminates the process. This method does not block. The resources are released as soon as
	 * the process has terminated (see {@link #onEndOfStdout()}).
	 *
	 * @param graceful	if {@code true}, then the process is asked to terminate by closing its stdin and
	 *                  killed if it does not terminate within a grace period. Otherwise, it is killed
	 *                  immediately.
	 */
	void terminate(boolean graceful) {
		/*
		 * The process is not unregistered here: Both branches only initiate the termination. The process
		 * is unregistered in onEndOfStdout() once it has actually terminated. Unregistering it earlier
		 * would bypass the shutdown hook if the JVM exits while the process is still alive.
		 */
		if (graceful) {
			// closing stdin may block (concurrent write) => done by the terminator thread
			ProcessRegistry.terminateGracefully(process, stdin, GRACE_PERIOD_MS);
		} else {
			process.destroyForcibly();
		}
	}

	/**
	 * Called by the stdout reader thread after it has added the last line. Releases the resources
	 * of the process as soon as it has terminated.
	 */
	private void onEndOfStdout() {
		endOfStdout = true;
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// process may still be running => keep it registered; temporary files will be deleted on exit
			Thread.currentThread().interrupt();
			return;
		}
		ProcessRegistry.unregister(process);
		deleteTempFiles(tempFiles);
		try {
			// does not block because the process has terminated
			stdin.close();
		} catch (IOException e) {
			/* ignore */
		}
	}

	private void addStderrLine(String line) {
		synchronized (stderrLines) {
			stderrLines.addLast(line);
			while (stderrLines.size() > MAX_STDERR_LINES) {
				stderrLines.removeFirst();
			}
		}
	}

	private static void startReader(InputStream stream, Consumer<String> lineConsumer, Runnable onEndOfStream, String streamName) {
		Thread reader = new Thread(() -> {
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					lineConsumer.accept(line);
				}
			} catch (IOException e) {
				/* stream closed */
			} finally {
				onEndOfStream.run();
			}
		}, "Claude Code " + streamName + " reader");
		reader.setDaemon(true);
		reader.start();
	}
}
