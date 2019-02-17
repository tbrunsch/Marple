package com.AMS.jBEAM.javaParser.result;

import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;

public class ObjectParseResult implements ParseResultIF
{
	private final int			position; // exclusive
	private final ObjectInfo 	objectInfo;

	public ObjectParseResult(int position, ObjectInfo objectInfo) {
		this.position = position;
		this.objectInfo = objectInfo;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.OBJECT_PARSE_RESULT;
	}

	public ObjectInfo getObjectInfo() {
		return objectInfo;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "Parsed until " + position + ": " + objectInfo;
	}
}
