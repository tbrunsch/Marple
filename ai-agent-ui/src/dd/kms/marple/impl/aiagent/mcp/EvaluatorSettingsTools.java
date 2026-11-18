package dd.kms.marple.impl.aiagent.mcp;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import dd.kms.marple.api.aiagent.backend.AiTool;
import dd.kms.marple.api.aiagent.backend.AiToolResult;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.Imports;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

import javax.annotation.Nullable;
import java.util.List;

import static dd.kms.marple.impl.aiagent.JsonUtils.getEnum;

/**
 * Tools that let the agent inspect the settings of its evaluator and change the minimum access modifiers
 * for fields and methods. Imports and the evaluation mode (static typing) cannot be changed by the agent:
 * It uses fully qualified class names instead. The agent's evaluator is separated from the user's evaluator
 * (see {@link dd.kms.marple.impl.aiagent.AgentContext}), so the changes do not affect the user.
 */
public class EvaluatorSettingsTools
{
	private static final Gson		GSON					= new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private final ExpressionEvaluator	evaluator;

	public EvaluatorSettingsTools(ExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public List<AiTool> createTools() {
		return ImmutableList.of(
			AiTool.builder("get_evaluator_settings",
					"Returns the settings of the expression evaluator: the minimum access modifiers for fields and methods, "
					+ "the evaluation mode, and the imported packages and classes (both cannot be changed).",
					args -> getSettings())
				.runInEventDispatchThread(true)
				.build(),
			AiTool.builder("set_minimum_access",
					"Sets the minimum access modifier of fields and/or methods that can be accessed in expressions, e.g., PRIVATE to access all members. "
					+ "Only use this for analyzing the application state, not for changing it. The change only affects your evaluator, not the user's.",
					args -> setMinimumAccess(getEnum(args, "fields", AccessModifier.class), getEnum(args, "methods", AccessModifier.class)))
				.parameter("fields", AccessModifier.class, "Minimum access modifier for fields", false)
				.parameter("methods", AccessModifier.class, "Minimum access modifier for methods", false)
				.runInEventDispatchThread(true)
				.build()
		);
	}

	private AiToolResult getSettings() {
		ParserSettings settings = evaluator.getParserSettings();
		Imports imports = settings.getImports();
		JsonObject result = new JsonObject();
		result.addProperty("minimumFieldAccess", settings.getMinimumFieldAccessModifier().name());
		result.addProperty("minimumMethodAccess", settings.getMinimumMethodAccessModifier().name());
		result.addProperty("evaluationMode", settings.getEvaluationMode().name());
		JsonArray packages = new JsonArray();
		imports.getImportedPackages().stream().sorted().forEach(packages::add);
		result.add("importedPackages", packages);
		JsonArray classes = new JsonArray();
		imports.getImportedClasses().stream().map(Class::getName).sorted().forEach(classes::add);
		result.add("importedClasses", classes);
		return AiToolResult.success(GSON.toJson(result));
	}

	private AiToolResult setMinimumAccess(@Nullable AccessModifier fieldAccess, @Nullable AccessModifier methodAccess) {
		if (fieldAccess == null && methodAccess == null) {
			return AiToolResult.error("Specify at least one of the parameters 'fields' and 'methods'");
		}
		ParserSettings settings = evaluator.getParserSettings();
		ParserSettingsBuilder builder = settings.builder();
		if (fieldAccess != null) {
			builder.minimumFieldAccessModifier(fieldAccess);
		}
		if (methodAccess != null) {
			builder.minimumMethodAccessModifier(methodAccess);
		}
		ParserSettings newSettings = builder.build();
		evaluator.setParserSettings(newSettings);
		return AiToolResult.success("Minimum access: fields = " + newSettings.getMinimumFieldAccessModifier().name()
			+ ", methods = " + newSettings.getMinimumMethodAccessModifier().name());
	}
}
