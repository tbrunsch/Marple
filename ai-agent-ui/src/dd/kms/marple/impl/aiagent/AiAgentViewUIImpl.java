package dd.kms.marple.impl.aiagent;

import com.google.common.base.Preconditions;
import dd.kms.marple.api.aiagent.AiAgentViewUI;
import dd.kms.marple.api.aiagent.backend.AiAgentFactorySupplier;
import dd.kms.marple.api.settings.visual.VisualSettingsBuilder;

/**
 * Builder for the {@link AiAgentViewSettings}. Changes made after {@link #register(VisualSettingsBuilder)}
 * do not affect the registered view.
 */
public class AiAgentViewUIImpl implements AiAgentViewUI
{
	private final AiAgentFactorySupplier	agentFactorySupplier;
	private String							systemPromptAddition	= "";

	public AiAgentViewUIImpl(AiAgentFactorySupplier agentFactorySupplier) {
		this.agentFactorySupplier = Preconditions.checkNotNull(agentFactorySupplier, "The agent factory supplier must not be null");
	}

	@Override
	public AiAgentViewUI systemPromptAddition(String systemPromptAddition) {
		this.systemPromptAddition = systemPromptAddition;
		return this;
	}

	@Override
	public VisualSettingsBuilder register(VisualSettingsBuilder visualSettingsBuilder) {
		AiAgentViewSettings settings = new AiAgentViewSettings(agentFactorySupplier, systemPromptAddition);
		return visualSettingsBuilder.objectView(Object.class, (object, context) -> new AiAgentView(object, context, settings));
	}
}
