package dd.kms.marple.impl.aiagent.claude;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.aiagent.backend.AiAgentFactory;
import dd.kms.marple.api.aiagent.claude.ClaudeCodeAgentFactoryBuilder;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ClaudeCodeAgentFactoryBuilderImpl implements ClaudeCodeAgentFactoryBuilder
{
	private final Path					workingDirectory;
	private String						executable				= "claude";
	/**
	 * Maps the available built-in tools to whether they require approval
	 */
	private final Map<String, Boolean>	tools					= new LinkedHashMap<>();
	private List<String>				additionalArguments		= ImmutableList.of();

	public ClaudeCodeAgentFactoryBuilderImpl(Path workingDirectory) {
		this.workingDirectory = workingDirectory;
		addTool("Read", false);
		addTool("Grep", false);
		addTool("Glob", false);
		addTool("Edit", true);
		addTool("Write", true);
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
	public AiAgentFactory build() {
		return new ClaudeCodeAgentFactoryImpl(executable, workingDirectory, ImmutableMap.copyOf(tools), additionalArguments);
	}
}
