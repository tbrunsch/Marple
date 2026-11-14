package dd.kms.marple.impl.aiagent.mcp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.ObjectInspectionFramework;
import dd.kms.marple.api.aiagent.backend.AiTool;
import dd.kms.marple.api.aiagent.backend.AiToolResult;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.api.settings.components.ComponentHierarchy;
import dd.kms.marple.impl.aiagent.AgentVariables;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.common.GeneralizedField;

import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.regex.Pattern;

import static dd.kms.marple.impl.aiagent.JsonUtils.getString;

/**
 * Tools that give the agent access to the inspected object ("this"), the Swing component hierarchy,
 * expression evaluation and variables. All tools are executed on the event dispatch thread. The agent
 * may only overwrite and delete the variables it has created (see {@link AgentVariables}).
 */
public class MarpleTools
{
	private static final int		MAX_DISPLAY_TEXT_LENGTH	= 500;
	private static final int		MAX_NUM_FIELDS			= 300;
	private static final Pattern	IDENTIFIER_PATTERN		= Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
	private static final Gson		GSON					= new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();

	private final InspectionContext	context;
	@Nullable
	private final Object			thisValue;
	private final AgentVariables	agentVariables;

	/**
	 * @param thisValue	the inspected object. It does not change during a conversation: A conversation belongs to one object.
	 */
	public MarpleTools(InspectionContext context, @Nullable Object thisValue, AgentVariables agentVariables) {
		this.context = context;
		this.thisValue = thisValue;
		this.agentVariables = agentVariables;
	}

	public List<AiTool> createTools() {
		return ImmutableList.of(
			AiTool.builder("get_object_info",
					"Returns information about \"this\" (the object the user inspects in Marple) or about the result of the specified expression: "
					+ "class, superclasses, all implemented interfaces, display text, identity hash code and whether it is a java.awt.Component.",
					args -> getObjectInfo(getString(args, "expression")))
				.parameter("expression", "string", "Optional Zenodot expression evaluated in the context of \"this\". If omitted, information about \"this\" is returned.", false)
				.runInEventDispatchThread(true)
				.build(),
			AiTool.builder("get_component_hierarchy",
					"Returns the component hierarchy from the root window down to \"this\" or to the result of the specified expression, which must be a java.awt.Component. "
					+ "For each component it lists the fields of components in the hierarchy that reference it. These fields indicate where in the source code the component is created or held.",
					args -> getComponentHierarchy(getString(args, "expression")))
				.parameter("expression", "string", "Optional Zenodot expression evaluated in the context of \"this\" that yields a component. If omitted, the hierarchy of \"this\" is returned.", false)
				.runInEventDispatchThread(true)
				.build(),
			AiTool.builder("get_fields",
					"Returns the fields (including inherited and private ones) of \"this\" or of the result of the specified expression: name, declaring class, declared type, runtime type of the value and display text of the value.",
					args -> getFields(getString(args, "expression")))
				.parameter("expression", "string", "Optional Zenodot expression evaluated in the context of \"this\". If omitted, the fields of \"this\" are returned.", false)
				.runInEventDispatchThread(true)
				.build(),
			AiTool.builder("evaluate_expression",
					"Evaluates a single Zenodot expression (Java-like syntax) in the context of \"this\" (fields and methods of \"this\" can be accessed directly or via \"this.\") and returns the runtime type and display text of the result. "
					+ "Expressions may have side effects, e.g., changing Swing components (remember to call revalidate()/repaint()). "
					+ "Follow the Zenodot rules in the system prompt. "
					+ "Optionally, the result is stored in a Marple variable that can be referenced by its name in later expressions. "
					+ "Variables of the user cannot be overwritten.",
					args -> evaluateExpression(getRequiredString(args, "expression"), getString(args, "variableName")))
				.parameter("expression", "string", "The expression to evaluate", true)
				.parameter("variableName", "string", "Optional name of a variable the result should be stored in. It must not be the name of a variable of the user.", false)
				.runInEventDispatchThread(true)
				.build(),
			AiTool.builder("list_variables",
					"Lists all Marple variables that can be referenced in expressions: name, type, display text of the value, whether the variable is final, "
					+ "and whether you have created it in this conversation (createdByAgent).",
					args -> listVariables())
				.runInEventDispatchThread(true)
				.build(),
			AiTool.builder("delete_variable",
					"Deletes a Marple variable you have created in this conversation. Variables of the user cannot be deleted.",
					args -> deleteVariable(getRequiredString(args, "name")))
				.parameter("name", "string", "Name of the variable to delete", true)
				.runInEventDispatchThread(true)
				.build()
		);
	}

	private AiToolResult getObjectInfo(@Nullable String expression) {
		Object target;
		try {
			target = getTarget(expression);
		} catch (ParseException e) {
			return parseError(e);
		}
		JsonObject result = describeObject(target);
		if (target != null) {
			JsonArray superclasses = new JsonArray();
			for (Class<?> superclass = target.getClass().getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
				superclasses.add(superclass.getName());
			}
			result.add("superclasses", superclasses);

			JsonArray interfaces = new JsonArray();
			for (Class<?> implementedInterface : ReflectionUtils.getAllInterfaces(target.getClass())) {
				interfaces.add(implementedInterface.getName());
			}
			result.add("interfaces", interfaces);
			result.addProperty("isComponent", target instanceof Component);
		}
		return success(result);
	}

	private AiToolResult getComponentHierarchy(@Nullable String expression) {
		Object target;
		try {
			target = getTarget(expression);
		} catch (ParseException e) {
			return parseError(e);
		}
		if (!(target instanceof Component)) {
			String type = target == null ? "null" : target.getClass().getName();
			if (!isEmpty(expression)) {
				return AiToolResult.error("The result of the expression is not a java.awt.Component, but " + type);
			}
			// "this" may be a sub-component (e.g., a table cell value) whose component is not known to the agent
			return AiToolResult.error("\"this\" is not a java.awt.Component, but " + type + ". If the user inspects a sub-component "
				+ "(e.g., a table cell value or a tree node), ask the user which component it belongs to or reach that component via an expression.");
		}
		ComponentHierarchy hierarchy = ObjectInspectionFramework.getComponentHierarchy((Component) target);
		Multimap<Object, GeneralizedField> referencingFields = ObjectInspectionFramework.findReferencingFields(hierarchy);
		List<Object> components = hierarchy.getComponents();

		JsonArray entries = new JsonArray();
		for (int i = 0; i < components.size(); i++) {
			Object component = components.get(i);
			JsonObject entry = describeObject(component);
			entry.addProperty("depth", i);
			JsonArray fields = new JsonArray();
			for (GeneralizedField field : referencingFields.get(component)) {
				fields.add(field.getDeclaringClass().getName() + "." + field.getName());
			}
			entry.add("referencedByFields", fields);
			entries.add(entry);
		}
		JsonObject result = new JsonObject();
		result.addProperty("description", "Hierarchy from the root (depth 0) to the requested component (last entry)");
		result.add("components", entries);
		return success(result);
	}

	private AiToolResult getFields(@Nullable String expression) {
		Object target;
		try {
			target = getTarget(expression);
		} catch (ParseException e) {
			return parseError(e);
		}
		if (target == null) {
			return AiToolResult.error("The object is null");
		}
		JsonArray fields = new JsonArray();
		int numFields = 0;
		boolean truncated = false;
		for (Class<?> clazz = target.getClass(); clazz != null && !truncated; clazz = clazz.getSuperclass()) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isSynthetic()) {
					continue;
				}
				if (numFields++ >= MAX_NUM_FIELDS) {
					truncated = true;
					break;
				}
				fields.add(describeField(field, target));
			}
		}
		JsonObject result = new JsonObject();
		result.add("object", describeObject(target));
		result.add("fields", fields);
		if (truncated) {
			result.addProperty("note", "Only the first " + MAX_NUM_FIELDS + " fields are listed");
		}
		return success(result);
	}

	/**
	 * @return "this" if no expression is specified, otherwise the result of the expression evaluated in the context of "this"
	 */
	@Nullable
	private Object getTarget(@Nullable String expression) throws ParseException {
		return isEmpty(expression) ? thisValue : context.getEvaluator().evaluate(expression, thisValue);
	}

	private static boolean isEmpty(@Nullable String expression) {
		return expression == null || expression.trim().isEmpty();
	}

	private JsonObject describeField(Field field, Object target) {
		JsonObject fieldObject = new JsonObject();
		fieldObject.addProperty("name", field.getName());
		fieldObject.addProperty("declaringClass", field.getDeclaringClass().getName());
		fieldObject.addProperty("declaredType", field.getGenericType().getTypeName());
		fieldObject.addProperty("modifiers", Modifier.toString(field.getModifiers()));
		try {
			field.setAccessible(true);
			Object value = field.get(Modifier.isStatic(field.getModifiers()) ? null : target);
			fieldObject.addProperty("runtimeType", value == null ? null : value.getClass().getName());
			fieldObject.addProperty("displayText", getDisplayText(value));
		} catch (Throwable t) {
			fieldObject.addProperty("error", "Cannot access field: " + t);
		}
		return fieldObject;
	}

	private AiToolResult evaluateExpression(String expression, @Nullable String variableName) {
		if (variableName != null && !IDENTIFIER_PATTERN.matcher(variableName).matches()) {
			return AiToolResult.error("Invalid variable name: '" + variableName + "'");
		}
		// check before evaluating: the expression might have side effects
		if (variableName != null && !agentVariables.canStore(variableName)) {
			return AiToolResult.error("The variable '" + variableName + "' belongs to the user and cannot be overwritten. Choose a different name.");
		}
		Object value;
		try {
			value = context.getEvaluator().evaluate(expression, thisValue);
		} catch (ParseException e) {
			return parseError(e);
		}
		JsonObject result = describeObject(value);
		if (variableName != null) {
			agentVariables.store(variableName, value);
			result.addProperty("storedInVariable", variableName);
		}
		return success(result);
	}

	private AiToolResult listVariables() {
		JsonArray variables = new JsonArray();
		for (Variable variable : context.getEvaluator().getVariables()) {
			JsonObject variableObject = new JsonObject();
			variableObject.addProperty("name", variable.getName());
			variableObject.addProperty("type", variable.getType().getName());
			variableObject.addProperty("displayText", getDisplayText(variable.getValue()));
			variableObject.addProperty("final", variable.isFinal());
			variableObject.addProperty("createdByAgent", agentVariables.isAgentVariable(variable.getName()));
			variables.add(variableObject);
		}
		JsonObject result = new JsonObject();
		result.add("variables", variables);
		return success(result);
	}

	private AiToolResult deleteVariable(String name) {
		if (agentVariables.delete(name)) {
			return AiToolResult.success("Variable '" + name + "' has been deleted");
		}
		boolean exists = context.getEvaluator().getVariables().stream().anyMatch(variable -> variable.getName().equals(name));
		return exists
			? AiToolResult.error("The variable '" + name + "' belongs to the user and cannot be deleted. The user can delete it in Marple.")
			: AiToolResult.error("There is no variable '" + name + "'");
	}

	private JsonObject describeObject(@Nullable Object object) {
		JsonObject description = new JsonObject();
		if (object == null) {
			description.add("class", JsonNull.INSTANCE);
			description.addProperty("displayText", "null");
			return description;
		}
		description.addProperty("class", object.getClass().getName());
		description.addProperty("displayText", getDisplayText(object));
		description.addProperty("identityHashCode", Integer.toHexString(System.identityHashCode(object)));
		return description;
	}

	private String getDisplayText(@Nullable Object object) {
		String displayText;
		try {
			displayText = context.getDisplayText(object);
		} catch (Throwable t) {
			displayText = "<cannot determine display text: " + t + ">";
		}
		if (displayText == null) {
			return "null";
		}
		return displayText.length() <= MAX_DISPLAY_TEXT_LENGTH
			? displayText
			: displayText.substring(0, MAX_DISPLAY_TEXT_LENGTH) + "... (truncated)";
	}

	private AiToolResult success(JsonElement result) {
		return AiToolResult.success(GSON.toJson(result));
	}

	private static AiToolResult parseError(ParseException e) {
		StringBuilder message = new StringBuilder("Error evaluating expression: ").append(e.getMessage());
		String expression = e.getExpression();
		int position = e.getPosition();
		if (expression != null && position >= 0 && position <= expression.length()) {
			message.append("\nPosition ").append(position).append(": ")
				.append(expression, 0, position).append(" <here> ").append(expression.substring(position));
		}
		Throwable cause = e.getCause();
		if (cause != null) {
			message.append("\nCause: ").append(cause);
		}
		return AiToolResult.error(message.toString());
	}

	static String getRequiredString(JsonObject arguments, String name) {
		String value = getString(arguments, name);
		if (value == null) {
			throw new IllegalArgumentException("Missing argument '" + name + "'");
		}
		return value;
	}
}
