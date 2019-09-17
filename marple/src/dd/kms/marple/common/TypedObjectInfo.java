package dd.kms.marple.common;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

public class TypedObjectInfo<T> implements ObjectInfo
{
	private final ObjectInfo	objectInfo;

	public TypedObjectInfo(ObjectInfo objectInfo) {
		this.objectInfo = objectInfo;
	}

	@Override
	public T getObject() {
		return (T) objectInfo.getObject();
	}

	@Override
	public TypeInfo getDeclaredType() {
		return objectInfo.getDeclaredType();
	}

	@Override
	public ValueSetter getValueSetter() {
		return objectInfo.getValueSetter();
	}
}
