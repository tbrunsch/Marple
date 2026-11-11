package dd.kms.marple.api.aiagent.backend;

/**
 * Decides whether the agent may perform an action that requires approval, e.g., creating or modifying a file. The method blocks
 * until a decision has been made (e.g., by the user) and is called outside the event dispatch thread.
 */
@FunctionalInterface
public interface PermissionHandler
{
	PermissionDecision requestPermission(PermissionRequest request);
}
