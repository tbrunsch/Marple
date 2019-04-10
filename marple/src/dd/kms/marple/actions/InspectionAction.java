package dd.kms.marple.actions;

public interface InspectionAction
{
	String getName();
	String getDescription();
	boolean isEnabled();
	void perform();
}
