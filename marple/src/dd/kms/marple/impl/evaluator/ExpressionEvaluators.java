package dd.kms.marple.impl.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.framework.common.PreferenceUtils;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

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
		PreferenceUtils.writeSettings(context);
	}

	public static <T> T getValue(Function<ParserSettings, T> valueGetter, InspectionContext context) {
		ExpressionEvaluator evaluator = context.getEvaluator();
		ParserSettings parserSettings = evaluator.getParserSettings();
		return valueGetter.apply(parserSettings);
	}

	public static Variables toVariableCollection(List<Variable> variables, boolean forceFinal) {
		Variables variableCollection = Variables.create();
		for (Variable variable : variables) {
			variableCollection.createVariable(variable.getName(), variable.getType(), variable.getValue(), forceFinal || variable.isFinal());
		}
		return variableCollection;
	}
}
