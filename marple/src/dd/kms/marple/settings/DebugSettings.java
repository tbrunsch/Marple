package dd.kms.marple.settings;

public interface DebugSettings
{
	Runnable getBreakpointTriggerCommand();
	String getBreakpointTriggerCommandDescription();
}
