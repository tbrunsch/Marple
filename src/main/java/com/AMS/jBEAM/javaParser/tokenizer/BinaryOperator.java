package com.AMS.jBEAM.javaParser.tokenizer;

public enum BinaryOperator
{
	MULTIPLY				("*",			4,	Associativity.LEFT_TO_RIGHT),
	DIVIDE					("/",			4,	Associativity.LEFT_TO_RIGHT),
	MODULO					("%",			4,	Associativity.LEFT_TO_RIGHT),
	ADD_OR_CONCAT			("+",			5,	Associativity.LEFT_TO_RIGHT),
	SUBTRACT				("-",			5,	Associativity.LEFT_TO_RIGHT),
	LEFT_SHIFT				("<<",			6,	Associativity.LEFT_TO_RIGHT),
	RIGHT_SHIFT				(">>",			6,	Associativity.LEFT_TO_RIGHT),
	UNSIGNED_RIGHT_SHIFT	(">>>",			6,	Associativity.LEFT_TO_RIGHT),
	LESS_THAN				("<",			7,	Associativity.LEFT_TO_RIGHT),
	LESS_THAN_OR_EQUAL_TO	("<=",			7,	Associativity.LEFT_TO_RIGHT),
	GREATER_THAN			(">",			7,	Associativity.LEFT_TO_RIGHT),
	GREATER_THAN_OR_EQUAL_TO(">=",			7,	Associativity.LEFT_TO_RIGHT),
	EQUAL_TO				("==",			8,	Associativity.LEFT_TO_RIGHT),
	NOT_EQUAL_TO			("!=",			8,	Associativity.LEFT_TO_RIGHT),
	BITWISE_AND				("&",			9,	Associativity.LEFT_TO_RIGHT),
	BITWISE_XOR				("^",			10,	Associativity.LEFT_TO_RIGHT),
	BITWISE_OR				("|",			11,	Associativity.LEFT_TO_RIGHT),
	LOGICAL_AND				("&&",			12,	Associativity.LEFT_TO_RIGHT),
	LOGICAL_OR				("||",			13,	Associativity.LEFT_TO_RIGHT),
	ASSIGNMENT				("=",			15,	Associativity.RIGHT_TO_LEFT);

	public static BinaryOperator getValue(String operatorString) {
		for (BinaryOperator operator : values()) {
			if (operator.getOperator().equals(operatorString)) {
				return operator;
			}
		}
		return null;
	}

	private final String		operator;
	private final int 			precedenceLevel;
	private final Associativity associativity;

	BinaryOperator(String operator, int precedenceLevel, Associativity associativity) {
		this.operator = operator;
		this.precedenceLevel = precedenceLevel;
		this.associativity = associativity;
	}

	public String getOperator() {
		return operator;
	}

	public int getPrecedenceLevel() {
		return precedenceLevel;
	}

	public Associativity getAssociativity() {
		return associativity;
	}

	@Override
	public String toString() {
		return operator;
	}
}
