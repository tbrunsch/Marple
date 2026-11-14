package dd.kms.marple.impl.aiagent;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * View settings of the AI view: the transcript, the agent's resume state and the variables the agent
 * has created. It allows restoring the conversation when the user navigates back to an object.<br>
 * <br>
 * The transcript and the resume state are immutable. The agent variables are shared with the session
 * on purpose: All snapshots of a conversation refer to the same {@link AgentVariables}, so they always
 * reflect the current variables, and the variables are garbage collected with the last snapshot.
 */
final class AiSessionSnapshot
{
	private final List<TranscriptEntry>	transcript;
	@Nullable
	private final Object				resumeState;
	private final AgentVariables		agentVariables;

	AiSessionSnapshot(List<TranscriptEntry> transcript, @Nullable Object resumeState, AgentVariables agentVariables) {
		this.transcript = ImmutableList.copyOf(transcript);
		this.resumeState = resumeState;
		this.agentVariables = agentVariables;
	}

	List<TranscriptEntry> getTranscript() {
		return transcript;
	}

	@Nullable
	Object getResumeState() {
		return resumeState;
	}

	AgentVariables getAgentVariables() {
		return agentVariables;
	}
}
