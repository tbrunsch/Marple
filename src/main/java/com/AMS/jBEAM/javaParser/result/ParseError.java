package com.AMS.jBEAM.javaParser.result;

public class ParseError implements ParseResultIF
{
	public enum ErrorType implements Comparable<ErrorType>
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

	public ParseError(int position, String message, ErrorType errorType) {
		this(position, message, errorType, null);
	}

	public ParseError(int position, String message, ErrorType errorType, Exception exception) {
		this.position = position;
		this.message = message;
		this.errorType = errorType;
		this.exception = exception;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.PARSE_ERROR;
	}

	public int getPosition() {
		return position;
	}

	public String getMessage() {
		return message;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public Exception getException() {
		return exception;
	}

	@Override
	public String toString() {
		return position + ": " + errorType + ": " + message + (exception == null ? "" : " (" + exception.getMessage() + ")");
	}
}
