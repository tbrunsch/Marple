package com.AMS.jBEAM.javaParser.result;

class IntRange
{
	/**
	 * inclusive
	 */
	private final int begin;

	/**
	 * exclusive
	 */
	private final int   end;

	IntRange(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}

	int getBegin() {
		return begin;
	}

	int getEnd() {
		return end;
	}
}
