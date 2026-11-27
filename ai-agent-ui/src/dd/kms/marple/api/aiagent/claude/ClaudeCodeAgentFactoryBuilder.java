package dd.kms.marple.api.aiagent.claude;

import dd.kms.marple.api.aiagent.backend.AiAgentFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Helper for setting up agents based on the Claude Code CLI. Each agent runs one Claude Code process
 * in the specified working directory.<br>
 * <br>
 * Example:
 * <pre>
 * AiAgentViewUI.create(() -&gt; ClaudeCodeAgentFactoryBuilder.create(sourceRootDirectory).build())
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
	 *                          inspected application. If it is {@code null} (e.g., because the source code cannot be
	 *                          found), then the agents can only analyze and change the running application: They
	 *                          are started in an empty directory without the tools {@code Read}, {@code Grep},
	 *                          {@code Glob}, {@code Edit}, and {@code Write}, and the view does not offer the scope
	 *                          "Files" (see {@link AiAgentFactory#supportsFileChanges()}). A non-null directory is
	 *                          validated by {@link #build()}.
	 */
	static ClaudeCodeAgentFactoryBuilder create(@Nullable Path workingDirectory) {
		return new dd.kms.marple.impl.aiagent.claude.ClaudeCodeAgentFactoryBuilderImpl(workingDirectory);
	}

	/**
	 * The Claude Code executable. Default: {@code claude} (resolved via PATH)
	 */
	ClaudeCodeAgentFactoryBuilder executable(String executable);

	/**
	 * Makes an additional built-in Claude Code tool available, e.g., {@code "WebSearch"} or {@code "WebFetch"}.
	 * If a working directory has been specified, then the agents can use {@code Read}, {@code Grep}, and
	 * {@code Glob} without approval and {@code Edit} and {@code Write} with approval by default. Otherwise,
	 * no built-in tools are available by default.<br>
	 * <br>
	 * {@code Edit} and {@code Write} are rejected without asking if the user has selected the scope
	 * "Running application". Other tools that require approval are not affected by the scope: The user is
	 * always asked.<br>
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
	 *
	 * @throws IOException if the specified working directory is not an existing directory or if the empty
	 *                     directory for agents without working directory cannot be created
	 */
	AiAgentFactory build() throws IOException;
}
