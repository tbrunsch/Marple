package dd.kms.marple.evaluator;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.Variable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ExpressionEvaluators
{
	public static ExpressionEvaluator create() {
		return new ExpressionEvaluatorImpl();
	}

	public static <T> void setValue(T value, BiConsumer<ParserSettingsBuilder, T> valueSetter, InspectionContext inspectionContext) {
		ExpressionEvaluator evaluator = inspectionContext.getEvaluator();
		ParserSettingsBuilder builder = evaluator.getParserSettings().builder();
		valueSetter.accept(builder, value);
		ParserSettings parserSettings = builder.build();
		evaluator.setParserSettings(parserSettings);
	}

	public static <T> T getValue(Function<ParserSettings, T> valueGetter, InspectionContext inspectionContext) {
		ExpressionEvaluator evaluator = inspectionContext.getEvaluator();
		ParserSettings parserSettings = evaluator.getParserSettings();
		return valueGetter.apply(parserSettings);
	}

	public static void setVariables(List<Variable> variables, InspectionContext inspectionContext) {
		setValue(variables, ParserSettingsBuilder::variables, inspectionContext);
	}

	public static List<Variable> getVariables(InspectionContext inspectionContext) {
		return getValue(ParserSettings::getVariables, inspectionContext);
	}
}
