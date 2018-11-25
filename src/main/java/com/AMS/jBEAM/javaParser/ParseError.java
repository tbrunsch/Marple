package com.AMS.jBEAM.javaParser;

class ParseError implements ParseResultIF
{
	enum ErrorType implements Comparable<ErrorType>
	{
		/*
		 * The order is important: The smaller the ordinal, the higher the priority.
		 */
		INTERNAL_ERROR,
		EVALUATION_EXCEPTION,
		SEMANTIC_ERROR,
		SYNTAX_ERROR,
		WRONG_PARSER;
	};

	private final int		position;
	private final String	message;
	private final ErrorType	errorType;
	private final Exception	exception;

	ParseError(int position, String message, ErrorType errorType) {
		this(position, message, errorType, null);
	}

	ParseError(int position, String message, ErrorType errorType, Exception exception) {
		this.position = position;
		this.message = message;
		this.errorType = errorType;
		this.exception = exception;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.PARSE_ERROR;
	}

	int getPosition() {
		return position;
	}

	String getMessage() {
		return message;
	}

	ErrorType getErrorType() {
		return errorType;
	}

	Exception getException() {
		return exception;
	}
}
