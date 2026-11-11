package dd.kms.marple.impl.aiagent;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Immutable view settings of the AI view: the transcript and the agent's resume state. It allows
 * restoring the conversation when the user navigates back to an object.
 */
final class AiSessionSnapshot
{
	private final List<TranscriptEntry>	transcript;
	@Nullable
	private final Object				resumeState;

	AiSessionSnapshot(List<TranscriptEntry> transcript, @Nullable Object resumeState) {
		this.transcript = ImmutableList.copyOf(transcript);
		this.resumeState = resumeState;
	}

	List<TranscriptEntry> getTranscript() {
		return transcript;
	}

	@Nullable
	Object getResumeState() {
		return resumeState;
	}
}
