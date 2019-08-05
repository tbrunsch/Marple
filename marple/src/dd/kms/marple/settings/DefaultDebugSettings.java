package dd.kms.marple.settings;

import dd.kms.marple.DebugSupport;

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
