package dd.kms.marple.gui.common;

import dd.kms.zenodot.ParseException;

public class ExceptionFormatter
{

	public static String formatParseException(String expression, ParseException e) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><p><b>").append(e.getMessage().replace("\n", "<br/>")).append("</b></p>");
		Throwable cause = e.getCause();
		if (cause != null) {
			builder.append("<br/>").append(formatException(cause, false));
		}
		int position = e.getPosition();
		if (0 <= position && position < expression.length()) {
			builder.append("<br/>").append(expression.substring(0, position)).append('^').append(expression.substring(position));
		}
		builder.append("</html>");
		return builder.toString();
	}

	public static String formatException(Throwable e, boolean encloseInHtml) {
		StringBuilder builder = new StringBuilder();
		if (encloseInHtml) {
			builder.append("<html>");
		}
		builder.append("<p>").append(e.getClass().getSimpleName());
		String message = e.getMessage();
		if (message != null) {
			builder.append(": ").append(message);
		}
		Throwable cause = e.getCause();
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
