package com.AMS.jBEAM.javaParser.result;

import com.AMS.jBEAM.javaParser.utils.wrappers.TypeInfo;

public class ClassParseResult implements ParseResultIF
{
	private final int		position; // exclusive
	private final TypeInfo type;

	public ClassParseResult(int position, TypeInfo type) {
		this.position = position;
		this.type = type;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.CLASS_PARSE_RESULT;
	}

	public TypeInfo getType() {
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
