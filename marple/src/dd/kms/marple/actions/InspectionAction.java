package dd.kms.marple.actions;

public interface InspectionAction
{
	boolean isDefaultAction();
	String getName();
	String getDescription();
	boolean isEnabled();
	void perform();
}
