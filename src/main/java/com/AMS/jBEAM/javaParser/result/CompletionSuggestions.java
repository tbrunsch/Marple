package com.AMS.jBEAM.javaParser.result;

import java.util.Collections;
import java.util.Map;

public class CompletionSuggestions implements ParseResultIF
{
	public static final CompletionSuggestions none(int position) {
		return new CompletionSuggestions(position, Collections.emptyMap());
	}

	private final int									position;
	private final Map<CompletionSuggestionIF, Integer>	ratedSuggestions;

	public CompletionSuggestions(int position, Map<CompletionSuggestionIF, Integer> ratedSuggestions) {
		this.position = position;
		this.ratedSuggestions = ratedSuggestions;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.COMPLETION_SUGGESTIONS;
	}

	@Override
	public int getPosition() {
		return position;
	}

	public Map<CompletionSuggestionIF, Integer> getRatedSuggestions() {
		return ratedSuggestions;
	}
}
