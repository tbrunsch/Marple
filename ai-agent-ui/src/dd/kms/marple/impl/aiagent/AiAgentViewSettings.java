package dd.kms.marple.impl.aiagent;

import dd.kms.marple.api.aiagent.backend.AiAgentFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Settings shared by all AI views of one registration (see {@link AiAgentViewUIImpl#register}). The object
 * lives as long as the registered inspection settings. "Auto approve" is deliberately not persisted: It is
 * off whenever the application is started.
 */
class AiAgentViewSettings
{
	@Nullable
	private final AiAgentFactory	agentFactory;
	private final String			unavailableMessage;
	private final String			systemPromptAddition;
	private volatile boolean		autoApprove				= false;
	private final List<Runnable>	autoApproveListeners	= new CopyOnWriteArrayList<>();

	AiAgentViewSettings(@Nullable AiAgentFactory agentFactory, String unavailableMessage, String systemPromptAddition) {
		this.agentFactory = agentFactory;
		this.unavailableMessage = unavailableMessage;
		this.systemPromptAddition = systemPromptAddition;
	}

	@Nullable
	AiAgentFactory getAgentFactory() {
		return agentFactory;
	}

	String getUnavailableMessage() {
		return unavailableMessage;
	}

	String getSystemPromptAddition() {
		return systemPromptAddition;
	}

	boolean isAutoApprove() {
		return autoApprove;
	}

	void setAutoApprove(boolean autoApprove) {
		if (this.autoApprove == autoApprove) {
			return;
		}
		this.autoApprove = autoApprove;
		autoApproveListeners.forEach(Runnable::run);
	}

	void addAutoApproveListener(Runnable listener) {
		autoApproveListeners.add(listener);
	}

	void removeAutoApproveListener(Runnable listener) {
		autoApproveListeners.remove(listener);
	}
}
