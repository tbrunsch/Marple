package dd.kms.marple.api.aiagent.backend;

import javax.annotation.Nullable;

public final class PermissionDecision
{
	private static final PermissionDecision	ALLOW	= new PermissionDecision(null);

	public static PermissionDecision allow() {
		return ALLOW;
	}

	public static PermissionDecision deny(String denyMessage) {
		return new PermissionDecision(denyMessage);
	}

	/**
	 * {@code null} if the request is allowed, otherwise the reason why it is denied
	 */
	@Nullable
	private final String	denyMessage;

	private PermissionDecision(@Nullable String denyMessage) {
		this.denyMessage = denyMessage;
	}

	public boolean isAllowed() {
		return denyMessage == null;
	}

	/**
	 * @return the reason why the request is denied
	 * @throws IllegalStateException if the request is allowed
	 */
	public String getDenyMessage() {
		if (denyMessage == null) {
			throw new IllegalStateException("The request is allowed");
		}
		return denyMessage;
	}
}
