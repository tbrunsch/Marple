package com.AMS.jBEAM.javaParser.result;

import com.google.common.reflect.TypeToken;

public class ClassParseResult implements ParseResultIF
{
	private final int			position; // exclusive
	private final TypeToken<?>	type;

	public ClassParseResult(int position, TypeToken<?> type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.CLASS_PARSE_RESULT;
	}

	public TypeToken<?> getType() {
		return type;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "Parsed until " + position + ": " + type;
	}
}
