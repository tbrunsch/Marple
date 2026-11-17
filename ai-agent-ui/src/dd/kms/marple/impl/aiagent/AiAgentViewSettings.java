package dd.kms.marple.impl.aiagent;

import dd.kms.marple.api.aiagent.backend.AiAgentFactory;
import dd.kms.marple.api.aiagent.backend.AiAgentFactorySupplier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Settings shared by all AI views of one registration (see {@link AiAgentViewUIImpl#register}). The object
 * lives as long as the registered inspection settings. "Auto approve" is deliberately not persisted: It is
 * off whenever the application is started.
 */
class AiAgentViewSettings
{
	private final AiAgentFactorySupplier	agentFactorySupplier;
	private final String					systemPromptAddition;
	/**
	 * The factory provided by the supplier. It is {@code null} as long as the supplier has not
	 * succeeded yet.
	 */
	@Nullable
	private AiAgentFactory					agentFactory;
	private volatile boolean				autoApprove				= false;
	private final List<Runnable>			autoApproveListeners	= new CopyOnWriteArrayList<>();

	AiAgentViewSettings(AiAgentFactorySupplier agentFactorySupplier, String systemPromptAddition) {
		this.agentFactorySupplier = agentFactorySupplier;
		this.systemPromptAddition = systemPromptAddition;
	}

	/**
	 * Returns the agent factory. The supplier is only called until it succeeds: A successfully created
	 * factory is reused, a failure is retried with the next call.
	 *
	 * @throws IOException if the supplier fails to create the factory
	 */
	synchronized AiAgentFactory getAgentFactory() throws IOException {
		if (agentFactory == null) {
			agentFactory = Objects.requireNonNull(agentFactorySupplier.get(), "The agent factory supplier returned null");
		}
		return agentFactory;
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
