package dd.kms.marple.api.aiagent.backend;

import java.io.IOException;

/**
 * Provides the {@link AiAgentFactory} for the "AI Agent" view (see
 * {@link dd.kms.marple.api.aiagent.AiAgentViewUI#create(AiAgentFactorySupplier)}).<br>
 * <br>
 * The supplier is called on the event dispatch thread when an "AI Agent" view is created, so it should
 * return quickly. A successfully created factory is used for all subsequent views. If the supplier throws
 * an exception, then the view displays the exception's message and the supplier is called again for the
 * next view. This way, the view becomes available once the environment has been fixed.
 */
@FunctionalInterface
public interface AiAgentFactorySupplier
{
	/**
	 * @return the factory, never {@code null}
	 * @throws IOException if the factory cannot be created, e.g., because the source code of the inspected
	 *                     application cannot be found. The exception's message is displayed to the user.
	 */
	AiAgentFactory get() throws IOException;
}
