package dd.kms.marple.api.evaluator;

/**
 * Represents a history of entered strings that can be navigated back and forth.
 */
public interface StringHistory
{
	/**
	 * Add a new string to the end of the history. The current lookup index will be
	 * set behind this new string such that the call {@code lookup(LookupDirection.PREVIOUS)}
	 * will return this string.
	 */
	void addString(String s);

	/**
	 * Navigates the string history one index in the specified direction and returns the string
	 * there. Call this method again with the same parameter to navigate further into the
	 * specified direction.
	 *
	 * @return The string at the new lookup index or {@code null} if the history is empty
	 */
	String lookup(LookupDirection lookupDirection);

	enum LookupDirection { PREVIOUS, NEXT }
}
