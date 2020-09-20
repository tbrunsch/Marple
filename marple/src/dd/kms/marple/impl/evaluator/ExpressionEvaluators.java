package dd.kms.marple.impl.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.Variable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ExpressionEvaluators
{
	public static <T> void setValue(T value, BiConsumer<ParserSettingsBuilder, T> valueSetter, InspectionContext context) {
		ExpressionEvaluator evaluator = context.getEvaluator();
		ParserSettingsBuilder builder = evaluator.getParserSettings().builder();
		valueSetter.accept(builder, value);
		ParserSettings parserSettings = builder.build();
		evaluator.setParserSettings(parserSettings);
	}

	public static <T> T getValue(Function<ParserSettings, T> valueGetter, InspectionContext context) {
		ExpressionEvaluator evaluator = context.getEvaluator();
		ParserSettings parserSettings = evaluator.getParserSettings();
		return valueGetter.apply(parserSettings);
	}

	public static void setVariables(List<Variable> variables, InspectionContext context) {
		setValue(variables, ParserSettingsBuilder::variables, context);
	}

	public static List<Variable> getVariables(InspectionContext context) {
		return getValue(ParserSettings::getVariables, context);
	}
}
