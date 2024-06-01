package dd.kms.marple.api.actions;

public interface InspectionAction
{
	String getName();
	String getDescription();
	boolean isEnabled();
	void perform();
}
