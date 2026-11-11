package dd.kms.marple.impl.aiagent;

import dd.kms.marple.api.aiagent.backend.AiAgentFactory;
import dd.kms.marple.api.aiagent.AiAgentViewUI;
import dd.kms.marple.api.settings.visual.VisualSettingsBuilder;

import javax.annotation.Nullable;

/**
 * Builder for the {@link AiAgentViewSettings}. Changes made after {@link #register(VisualSettingsBuilder)}
 * do not affect the registered view.
 */
public class AiAgentViewUIImpl implements AiAgentViewUI
{
	@Nullable
	private AiAgentFactory	agentFactory;
	private String			unavailableMessage		= "AI agent not available";
	private String			systemPromptAddition	= "";

	@Override
	public AiAgentViewUI agentFactory(@Nullable AiAgentFactory agentFactory) {
		this.agentFactory = agentFactory;
		return this;
	}

	@Override
	public AiAgentViewUI unavailableMessage(String unavailableMessage) {
		this.unavailableMessage = unavailableMessage;
		return this;
	}

	@Override
	public AiAgentViewUI systemPromptAddition(String systemPromptAddition) {
		this.systemPromptAddition = systemPromptAddition;
		return this;
	}

	@Override
	public VisualSettingsBuilder register(VisualSettingsBuilder visualSettingsBuilder) {
		AiAgentViewSettings settings = new AiAgentViewSettings(agentFactory, unavailableMessage, systemPromptAddition);
		return visualSettingsBuilder.objectView(Object.class, (object, context) -> new AiAgentView(object, context, settings));
	}
}
