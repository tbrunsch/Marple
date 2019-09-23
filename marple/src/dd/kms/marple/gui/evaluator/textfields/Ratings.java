package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.result.CompletionSuggestion;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Ratings
{
	private static final int						NUM_STRING_MATCH_RATINGS	= StringMatch.values().length;

	private static final Map<MatchRating, Integer>	MATCH_RATING_RATINGS		= createRatingMapping(createMatchRatings());

	private static <T extends Comparable<T>> Map<T, Integer> createRatingMapping(Collection<T> domain) {
		List<T> sortedDomain = new ArrayList<>(domain);
		Collections.sort(sortedDomain);
		Collections.reverse(sortedDomain);
		return IntStream.range(0, sortedDomain.size())
			.boxed()
			.collect(Collectors.toMap(
				i -> sortedDomain.get(i),
				i -> i
			));
	}

	private static List<MatchRating> createMatchRatings() {
		List<MatchRating> matchRatings = new ArrayList<>();
		for (StringMatch stringMatch : StringMatch.values()) {
			for (TypeMatch typeMatch : TypeMatch.values()) {
				for (AccessMatch accessMatch : AccessMatch.values()) {
					matchRatings.add(MatchRatings.create(stringMatch, typeMatch, accessMatch));
				}
			}
		}
		return matchRatings;
	}

	public static Map<CompletionSuggestion, Integer> filterAndTransformStringMatches(Map<CompletionSuggestion, StringMatch> ratedSuggestions) {
		return filterAndTransform(ratedSuggestions, Function.identity(), Ratings::getRating);
	}

	public static Map<CompletionSuggestion, Integer> filterAndTransformMatchRatings(Map<CompletionSuggestion, MatchRating> ratedSuggestions) {
		return filterAndTransform(ratedSuggestions, MatchRating::getNameMatch, Ratings::getRating);
	}

	private static <T> Map<CompletionSuggestion, Integer> filterAndTransform(Map<CompletionSuggestion, T> ratedSuggestions, Function<T, StringMatch> stringMatchExtractor, Function<T, Integer> ratingFunction) {
		return ratedSuggestions.keySet().stream()
			.filter(suggestion -> stringMatchExtractor.apply(ratedSuggestions.get(suggestion)) != StringMatch.NONE)
			.collect(Collectors.toMap(
				Function.identity(),
				suggestion -> ratingFunction.apply(ratedSuggestions.get(suggestion))
			));
	}

	private static int getRating(StringMatch stringMatch) {
		return NUM_STRING_MATCH_RATINGS - 1 - stringMatch.ordinal();
	}

	private static int getRating(MatchRating matchRating) {
		return MATCH_RATING_RATINGS.get(matchRating);
	}
}
