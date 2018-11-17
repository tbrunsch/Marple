package com.AMS.jBEAM.javaParser;

class AmbiguousParseResult implements ParseResultIF
{
	private final int position;
	private final String message;

	AmbiguousParseResult(int position, String message) {
		this.position = position;
		this.message = message;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.AMBIGUOUS_PARSE_RESULT;
	}

	int getPosition() {
		return position;
	}

	String getMessage() {
		return message;
	}
}
