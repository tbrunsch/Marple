package dd.kms.marple.api.aiagent.claude;

import dd.kms.marple.api.aiagent.backend.AiAgentFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * Helper for setting up agents based on the Claude Code CLI. Each agent runs one Claude Code process
 * in the specified working directory.<br>
 * <br>
 * Example:
 * <pre>
 * AiAgentViewUI.create()
 *     .agentFactory(ClaudeCodeAgentFactoryBuilder.create(sourceRootDirectory).build())
 *     .register(visualSettingsBuilder);
 * </pre>
 * The agents use the headless mode of Claude Code ({@code claude -p} with stream-json input and output).
 * They have been tested with Claude Code 2.1.x. The version is not checked, so if another version does
 * not support the command line options or messages used, starting a request fails with an error message
 * that contains Claude Code's error output.
 */
public interface ClaudeCodeAgentFactoryBuilder
{
	/**
	 * @param workingDirectory	the directory the agents are started in. It should contain the source code of the
	 *                          inspected application.
	 */
	static ClaudeCodeAgentFactoryBuilder create(Path workingDirectory) {
		return new dd.kms.marple.impl.aiagent.claude.ClaudeCodeAgentFactoryBuilderImpl(workingDirectory);
	}

	/**
	 * The Claude Code executable. Default: {@code claude} (resolved via PATH)
	 */
	ClaudeCodeAgentFactoryBuilder executable(String executable);

	/**
	 * Makes an additional built-in Claude Code tool available, e.g., {@code "WebSearch"} or {@code "WebFetch"}.
	 * By default, the agents can use {@code Read}, {@code Grep}, and {@code Glob} without approval and
	 * {@code Edit} and {@code Write} with approval.<br>
	 * <br>
	 * The first registration of a tool wins: Registering a tool again with the same approval flag has no
	 * effect, registering it with a different flag results in an exception.<br>
	 * <br>
	 * Note: Tools that modify files should require approval. Tools that access the web can load content
	 * with instructions that make the agent misuse its access to the inspected application (e.g., evaluating
	 * arbitrary expressions).
	 *
	 * @param tool				the name of the tool (without arguments like in {@code "Bash(git *)"})
	 * @param requiresApproval	if {@code true}, then the user has to approve each call of the tool
	 *                          (like file changes). Otherwise, the tool is used without asking.
	 * @throws IllegalArgumentException if the tool has already been registered with a different approval flag
	 */
	ClaudeCodeAgentFactoryBuilder addTool(String tool, boolean requiresApproval);

	/**
	 * Additional command line arguments, e.g., {@code --model <model>}
	 */
	ClaudeCodeAgentFactoryBuilder additionalArguments(List<String> additionalArguments);

	/**
	 * Creates the factory. Changes of the builder made afterwards do not affect the factory.
	 */
	AiAgentFactory build();
}
