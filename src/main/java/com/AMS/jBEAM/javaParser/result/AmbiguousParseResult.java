package com.AMS.jBEAM.javaParser.result;

public class AmbiguousParseResult implements ParseResultIF
{
	private final int position;
	private final String message;

	public AmbiguousParseResult(int position, String message) {
		this.position = position;
		this.message = message;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.AMBIGUOUS_PARSE_RESULT;
	}

	public int getPosition() {
		return position;
	}

	public String getMessage() {
		return message;
	}
}
