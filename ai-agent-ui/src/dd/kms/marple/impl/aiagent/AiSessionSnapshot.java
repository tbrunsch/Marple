package dd.kms.marple.impl.aiagent;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * View settings of the AI view: the transcript, the agent's resume state, the agent's evaluator, and the
 * selected scope.
 * It allows restoring the conversation when the user navigates back to an object.<br>
 * <br>
 * The transcript and the resume state are immutable. The agent context is shared with the session
 * on purpose: All snapshots of a conversation refer to the same {@link AgentContext}, so they always
 * reflect its current state, and it is garbage collected with the last snapshot.
 */
class AiSessionSnapshot
{
	private final List<TranscriptEntry>	transcript;
	@Nullable
	private final Object				resumeState;
	private final AgentContext			agentContext;
	private final Scope					scope;

	AiSessionSnapshot(List<TranscriptEntry> transcript, @Nullable Object resumeState, AgentContext agentContext, Scope scope) {
		this.transcript = ImmutableList.copyOf(transcript);
		this.resumeState = resumeState;
		this.agentContext = agentContext;
		this.scope = scope;
	}

	List<TranscriptEntry> getTranscript() {
		return transcript;
	}

	@Nullable
	Object getResumeState() {
		return resumeState;
	}

	AgentContext getAgentContext() {
		return agentContext;
	}

	Scope getScope() {
		return scope;
	}
}
