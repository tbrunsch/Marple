package dd.kms.marple.impl.instancesearch;

import javax.annotation.Nullable;

public class InstancePath
{
	private final Object					lastNodeObject;
	private final String					lastNodeStringRepresentation;
	private final @Nullable InstancePath	parentPath;

	public InstancePath(Object object, String lastNodeStringRepresentation, @Nullable InstancePath parentPath) {
		this.lastNodeObject = object;
		this.lastNodeStringRepresentation = lastNodeStringRepresentation;
		this.parentPath = parentPath;
	}

	public Object getLastNodeObject() {
		return lastNodeObject;
	}

	public String getLastNodeStringRepresentation() {
		return lastNodeStringRepresentation;
	}

	public @Nullable InstancePath getParentPath() {
		return parentPath;
	}

	@Override
	public String toString() {
		return parentPath == null ? lastNodeStringRepresentation : parentPath.toString() + lastNodeStringRepresentation;
	}
}