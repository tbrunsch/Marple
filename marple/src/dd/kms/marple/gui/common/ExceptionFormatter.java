package dd.kms.marple.gui.common;

import dd.kms.zenodot.api.ParseException;

import javax.annotation.Nullable;

public class ExceptionFormatter
{
	/**
	 * Returns null of the throwable is null.
	 */
	public static String formatParseException(String expression, @Nullable Throwable t) {
		if (t == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<html><p><b>").append(t.getMessage().replace("\n", "<br/>")).append("</b></p>");
		Throwable cause = t.getCause();
		if (cause != null) {
			builder.append("<br/>").append(formatException(cause, false));
		}
		int position = t instanceof ParseException ? ((ParseException) t).getPosition() : -1;
		if (0 <= position && position < expression.length()) {
			builder.append("<br/>").append(expression.substring(0, position)).append('^').append(expression.substring(position));
		}
		builder.append("</html>");
		return builder.toString();
	}

	public static String formatException(Throwable t, boolean encloseInHtml) {
		StringBuilder builder = new StringBuilder();
		if (encloseInHtml) {
			builder.append("<html>");
		}
		builder.append("<p>").append(t.getClass().getSimpleName());
		String message = t.getMessage();
		if (message != null) {
			builder.append(": ").append(message.replace("\n", "<br/>"));
		}
		Throwable cause = t.getCause();
		if (cause != null) {
			builder.append("<br/>  Cause: ").append(formatException(cause, false));
		}
		builder.append("</p>");
		if (encloseInHtml) {
			builder.append("</html>");
		}
		return builder.toString();
	}
}
