package com.AMS.jBEAM.javaParser;

class ParseResult implements ParseResultIF
{
	private final int			parsedToPosition; // exclusive
	private final ObjectInfo	objectInfo;

	ParseResult(int parsedToPosition, ObjectInfo objectInfo) {
		this.parsedToPosition = parsedToPosition;
		this.objectInfo = objectInfo;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.PARSE_RESULT;
	}

	ObjectInfo getObjectInfo() {
		return objectInfo;
	}

	int getParsedToPosition() {
		return parsedToPosition;
	}

}
