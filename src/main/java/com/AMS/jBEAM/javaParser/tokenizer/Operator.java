package com.AMS.jBEAM.javaParser.tokenizer;

public enum Operator
{
	MUL						("*",			4,	Associativity.LEFT_TO_RIGHT, false),
	DIV						("/",			4,	Associativity.LEFT_TO_RIGHT, false),
	MOD						("%",			4,	Associativity.LEFT_TO_RIGHT, false),
	ADD_OR_CONCAT			("+",			5,	Associativity.LEFT_TO_RIGHT, false),
	SUB						("-",			5,	Associativity.LEFT_TO_RIGHT, false),
	LEFT_SHIFT				("<<",			6,	Associativity.LEFT_TO_RIGHT, false),
	RIGHT_SHIFT				(">>",			6,	Associativity.LEFT_TO_RIGHT, false),
	UNSIGNED_RIGHT_SHIFT	(">>>",			6,	Associativity.LEFT_TO_RIGHT, false),
	LESS_THAN				("<",			7,	Associativity.LEFT_TO_RIGHT, false),
	LESS_THAN_OR_EQUAL_TO	("<=",			7,	Associativity.LEFT_TO_RIGHT, false),
	GREATER_THAN			(">",			7,	Associativity.LEFT_TO_RIGHT, false),
	GREATER_THAN_OR_EQUAL_TO(">=",			7,	Associativity.LEFT_TO_RIGHT, false),
	EQUAL_TO				("==",			8,	Associativity.LEFT_TO_RIGHT, false),
	NOT_EQUAL_TO			("!=",			8,	Associativity.LEFT_TO_RIGHT, false),
	BITWISE_AND				("&",			9,	Associativity.LEFT_TO_RIGHT, false),
	BITWISE_XOR				("^",			10,	Associativity.LEFT_TO_RIGHT, false),
	BITWISE_OR				("|",			11,	Associativity.LEFT_TO_RIGHT, false),
	LOGICAL_AND				("&&",			12,	Associativity.LEFT_TO_RIGHT, true),
	LOGICAL_OR				("||",			13,	Associativity.LEFT_TO_RIGHT, true),
	ASSIGNMENT				("=",			15,	Associativity.RIGHT_TO_LEFT, false);

	public static Operator getValue(String operatorString) {
		for (Operator operator : values()) {
			if (operator.getOperator().equals(operatorString)) {
				return operator;
			}
		}
		return null;
	}

	private final String		operator;
	private final int 			precedenceLevel;
	private final Associativity associativity;
	private final boolean		useShortCircuitEvaluation;

	Operator(String operator, int precedenceLevel, Associativity associativity, boolean useShortCircuitEvaluation) {
		this.operator = operator;
		this.precedenceLevel = precedenceLevel;
		this.associativity = associativity;
		this.useShortCircuitEvaluation = useShortCircuitEvaluation;
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

	public boolean isUseShortCircuitEvaluation() {
		return useShortCircuitEvaluation;
	}

	@Override
	public String toString() {
		return operator;
	}
}
