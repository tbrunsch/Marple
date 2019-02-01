package com.AMS.jBEAM.javaParser.result;

import com.AMS.jBEAM.javaParser.utils.ObjectInfo;

public class ParseResult implements ParseResultIF
{
	private final int			parsedToPosition; // exclusive
	private final ObjectInfo 	objectInfo;

	public ParseResult(int parsedToPosition, ObjectInfo objectInfo) {
		this.parsedToPosition = parsedToPosition;
		this.objectInfo = objectInfo;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.PARSE_RESULT;
	}

	public ObjectInfo getObjectInfo() {
		return objectInfo;
	}

	public int getParsedToPosition() {
		return parsedToPosition;
	}

	@Override
	public String toString() {
		return "Parsed until " + parsedToPosition + ": " + objectInfo;
	}
}
