package dd.kms.marple.impl.aiagent;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.gui.filters.ValueFilter;
import dd.kms.marple.impl.gui.filters.ValueFilters;
import dd.kms.marple.impl.gui.table.ActionProviderRenderer;
import dd.kms.marple.impl.gui.table.ColumnDescription;
import dd.kms.marple.impl.gui.table.ColumnDescriptionBuilder;
import dd.kms.marple.impl.gui.table.ListBasedTable;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Non-modal dialog that lists the tool calls of the current conversation with their complete parameters.
 * The tools can be filtered via the context menu of the column header. For {@code evaluate_expression},
 * the parameters provide an action that opens the expression in Marple's evaluation dialog.<br>
 * <br>
 * The dialog is updated by the AI view via {@link #setToolCalls(List)}.
 */
class ToolCallsDialog extends JDialog
{
	private static final String	EVALUATE_EXPRESSION_TOOL	= "evaluate_expression";
	private static final int	MAX_TOOL_TIP_LENGTH			= 3000;

	private final InspectionContext			context;
	private final Object					thisValue;

	private final List<ToolCall>			toolCalls			= new ArrayList<>();
	private final ValueFilter				toolFilter;
	private final ListBasedTable<ToolCall>	table;

	/**
	 * @param thisValue	the object in whose context the agent has evaluated expressions
	 */
	ToolCallsDialog(@Nullable Window owner, InspectionContext context, Object thisValue, List<TranscriptEntry> toolEntries) {
		super(owner, "Tool Calls", ModalityType.MODELESS);
		this.context = context;
		this.thisValue = thisValue;

		toolCalls.addAll(createToolCalls(toolEntries));
		toolFilter = ValueFilters.createSelectionFilter(context);
		List<ColumnDescription<ToolCall>> columnDescriptions = Arrays.asList(
			new ColumnDescriptionBuilder<ToolCall>("#",				Integer.class,	ToolCall::getNumber).build(),
			new ColumnDescriptionBuilder<ToolCall>("Tool",			String.class,	ToolCall::getToolName).valueFilter(toolFilter).build(),
			new ColumnDescriptionBuilder<ToolCall>("Parameters",	Object.class,	this::getParametersValue).valueFilter(ValueFilters.createWildcardFilter()).build()
		);
		table = new ListBasedTable<>(toolCalls, columnDescriptions);

		JTable internalTable = table.getInternalTable();
		TableColumnModel columnModel = internalTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(40);
		columnModel.getColumn(0).setMaxWidth(60);
		columnModel.getColumn(1).setPreferredWidth(180);
		columnModel.getColumn(1).setMaxWidth(300);
		columnModel.getColumn(2).setPreferredWidth(600);
		columnModel.getColumn(2).setCellRenderer(new ParametersRenderer());
		internalTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		JLabel hintLabel = new JLabel("Right-click a column header to filter");
		hintLabel.setForeground(Color.GRAY);

		JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPanel.add(table, BorderLayout.CENTER);
		contentPanel.add(hintLabel, BorderLayout.SOUTH);
		setContentPane(contentPanel);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setSize(900, 400);
		setLocationRelativeTo(owner);
		scrollToLastRow();
	}

	/**
	 * Updates the dialog with the tool entries of the transcript (entries with a tool name).
	 */
	void setToolCalls(List<TranscriptEntry> toolEntries) {
		JTable internalTable = table.getInternalTable();
		boolean hadSelection = internalTable.getSelectedRowCount() > 0;
		toolCalls.clear();
		toolCalls.addAll(createToolCalls(toolEntries));
		// the selection filter only knows the tools that existed when the table was created
		for (ToolCall toolCall : toolCalls) {
			toolFilter.addAvailableValue(toolCall.getToolName());
		}
		((AbstractTableModel) internalTable.getModel()).fireTableDataChanged();
		if (!hadSelection) {
			scrollToLastRow();
		}
	}

	private void scrollToLastRow() {
		JTable internalTable = table.getInternalTable();
		int lastRow = internalTable.getRowCount() - 1;
		if (lastRow >= 0) {
			internalTable.scrollRectToVisible(internalTable.getCellRect(lastRow, 0, true));
		}
	}

	private Object getParametersValue(ToolCall toolCall) {
		String expression = toolCall.getExpression();
		if (expression == null) {
			return toolCall.getParameters();
		}
		return ActionProvider.of(toolCall.getParameters(),
			ImmutableList.of(context.createEvaluateExpressionAction(expression, expression.length(), thisValue)),
			() -> null);
	}

	private static List<ToolCall> createToolCalls(List<TranscriptEntry> toolEntries) {
		List<ToolCall> toolCalls = new ArrayList<>(toolEntries.size());
		for (TranscriptEntry entry : toolEntries) {
			String toolName = entry.getToolName();
			JsonObject input = entry.getToolInput();
			if (toolName != null && input != null) {
				toolCalls.add(new ToolCall(toolCalls.size() + 1, toolName, input));
			}
		}
		return toolCalls;
	}

	private static String formatParameters(JsonObject input, boolean singleLine) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, JsonElement> parameter : input.entrySet()) {
			if (builder.length() > 0) {
				builder.append(singleLine ? ", " : "\n");
			}
			JsonElement value = parameter.getValue();
			String valueText = value.isJsonPrimitive() ? value.getAsString() : value.toString();
			if (singleLine) {
				valueText = valueText.replace("\r\n", "\n").replace('\n', '↵');
			}
			builder.append(parameter.getKey()).append(" = ").append(valueText);
		}
		return builder.toString();
	}

	private static class ToolCall
	{
		private final int		number;
		private final String	toolName;
		private final String	parameters;
		private final String	fullParameters;
		@Nullable
		private final String	expression;

		ToolCall(int number, String toolName, JsonObject input) {
			this.number = number;
			this.toolName = toolName;
			this.parameters = formatParameters(input, true);
			this.fullParameters = formatParameters(input, false);
			JsonElement expressionElement = input.get("expression");
			this.expression = EVALUATE_EXPRESSION_TOOL.equals(toolName) && expressionElement != null && expressionElement.isJsonPrimitive()
				? expressionElement.getAsString()
				: null;
		}

		int getNumber() {
			return number;
		}

		String getToolName() {
			return toolName;
		}

		String getParameters() {
			return parameters;
		}

		String getFullParameters() {
			return fullParameters;
		}

		@Nullable
		String getExpression() {
			return expression;
		}
	}

	/**
	 * Renders action providers like Marple does and shows the complete parameters (multi-line) as tool tip.
	 */
	private class ParametersRenderer implements TableCellRenderer
	{
		private final ActionProviderRenderer	wrappedRenderer	= new ActionProviderRenderer();

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component component = wrappedRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (component instanceof JComponent) {
				int modelRow = table.convertRowIndexToModel(row);
				String fullParameters = toolCalls.get(modelRow).getFullParameters();
				if (fullParameters.length() > MAX_TOOL_TIP_LENGTH) {
					fullParameters = fullParameters.substring(0, MAX_TOOL_TIP_LENGTH) + "...";
				}
				((JComponent) component).setToolTipText(fullParameters.isEmpty() ? null : "<html><pre>" + MarkdownToHtml.escape(fullParameters) + "</pre></html>");
			}
			return component;
		}
	}
}
