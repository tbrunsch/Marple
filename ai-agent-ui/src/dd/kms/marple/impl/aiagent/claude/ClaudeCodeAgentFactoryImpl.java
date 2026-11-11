package dd.kms.marple.impl.aiagent.claude;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.aiagent.backend.AiAgent;
import dd.kms.marple.api.aiagent.backend.AiAgentEnvironment;
import dd.kms.marple.api.aiagent.backend.AiAgentFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

class ClaudeCodeAgentFactoryImpl implements AiAgentFactory
{
	private final String						executable;
	private final Path							workingDirectory;
	private final ImmutableMap<String, Boolean>	tools;
	private final ImmutableList<String>			additionalArguments;

	ClaudeCodeAgentFactoryImpl(String executable, Path workingDirectory, ImmutableMap<String, Boolean> tools, List<String> additionalArguments) {
		this.executable = executable;
		this.workingDirectory = workingDirectory;
		this.tools = tools;
		this.additionalArguments = ImmutableList.copyOf(additionalArguments);
	}

	@Override
	public AiAgent createAgent(AiAgentEnvironment environment, @Nullable Object resumeState) {
		return new ClaudeCodeAgent(executable, workingDirectory, tools, additionalArguments, environment, resumeState);
	}
}
