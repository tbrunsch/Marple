package dd.kms.marple.api.settings;

public interface DebugSettings
{
	Runnable getBreakpointTriggerCommand();
	String getBreakpointTriggerCommandDescription();
}
