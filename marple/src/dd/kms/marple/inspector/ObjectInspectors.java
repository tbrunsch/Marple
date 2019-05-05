package dd.kms.marple.inspector;

public class ObjectInspectors
{
	public static ObjectInspector create() {
		return new ObjectInspectorImpl();
	}
}
