package dd.kms.marple.impl.settings;

import dd.kms.marple.api.DebugSupport;
import dd.kms.marple.api.settings.DebugSettings;

class DefaultDebugSettings implements DebugSettings
{
	static final DebugSettings	INSTANCE	= new DefaultDebugSettings();

	private DefaultDebugSettings() {}

	@Override
	public Runnable getBreakpointTriggerCommand() {
		return DebugSupport::triggerBreakpoint;
	}

	@Override
	public String getBreakpointTriggerCommandDescription() {
		return "DebugSupport.triggerBreakpoint()";
	}
}
