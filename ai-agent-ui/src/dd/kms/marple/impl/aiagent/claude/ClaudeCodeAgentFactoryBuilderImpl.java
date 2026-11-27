package dd.kms.marple.impl.aiagent.claude;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.aiagent.backend.AiAgentFactory;
import dd.kms.marple.api.aiagent.claude.ClaudeCodeAgentFactoryBuilder;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClaudeCodeAgentFactoryBuilderImpl implements ClaudeCodeAgentFactoryBuilder
{
	/**
	 * Name of the empty directory (in the temp directory) agents without working directory are started in. It
	 * is the same for all factories because Claude Code stores the sessions per directory: Resuming a session
	 * requires the same directory.
	 */
	private static final String			EMPTY_DIRECTORY_NAME	= "marple-ai-agent";

	@Nullable
	private final Path					workingDirectory;
	private String						executable				= "claude";
	/**
	 * Maps the available built-in tools to whether they require approval
	 */
	private final Map<String, Boolean>	tools					= new LinkedHashMap<>();
	private List<String>				additionalArguments		= ImmutableList.of();

	public ClaudeCodeAgentFactoryBuilderImpl(@Nullable Path workingDirectory) {
		this.workingDirectory = workingDirectory;
		if (workingDirectory != null) {
			addTool("Read", false);
			addTool("Grep", false);
			addTool("Glob", false);
			addTool("Edit", true);
			addTool("Write", true);
		}
	}

	@Override
	public ClaudeCodeAgentFactoryBuilder executable(String executable) {
		this.executable = executable;
		return this;
	}

	@Override
	public ClaudeCodeAgentFactoryBuilder addTool(String tool, boolean requiresApproval) {
		Objects.requireNonNull(tool);
		Boolean registeredRequiresApproval = tools.get(tool);
		if (registeredRequiresApproval == null) {
			tools.put(tool, requiresApproval);
		} else if (registeredRequiresApproval != requiresApproval) {
			throw new IllegalArgumentException("The tool '" + tool + "' has already been registered "
				+ (registeredRequiresApproval ? "with" : "without") + " approval");
		}
		return this;
	}

	@Override
	public ClaudeCodeAgentFactoryBuilder additionalArguments(List<String> additionalArguments) {
		this.additionalArguments = ImmutableList.copyOf(additionalArguments);
		return this;
	}

	@Override
	public AiAgentFactory build() throws IOException {
		boolean supportsFileChanges = workingDirectory != null;
		Path directory = supportsFileChanges ? workingDirectory : createEmptyDirectory();
		if (!Files.isDirectory(directory)) {
			throw new IOException("The working directory '" + directory + "' does not exist or is not a directory");
		}
		return new ClaudeCodeAgentFactoryImpl(executable, directory, supportsFileChanges, ImmutableMap.copyOf(tools), additionalArguments);
	}

	private static Path createEmptyDirectory() throws IOException {
		Path directory = Paths.get(System.getProperty("java.io.tmpdir"), EMPTY_DIRECTORY_NAME);
		try {
			return Files.createDirectories(directory);
		} catch (IOException e) {
			throw new IOException("Cannot create the directory '" + directory + "' for the AI agent: " + e.getMessage(), e);
		}
	}
}
