package dd.kms.marple;

public interface InspectionLinkIF extends Runnable
{
	Object getObjectToInspect();

	// Should only be called by ObjectInspector
	void inspect(ObjectInspector inspector);
}
