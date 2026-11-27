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
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/**
 * Non-modal dialog that lists the tool calls of the current conversation with their complete parameters.
 * <ul>
 *     <li>
 *         Column "#": the number of the tool call. Clicking a number highlights the tool call in the transcript.
 *     </li>
 *     <li>
 *         Column "Request": the number of the user message the tool call belongs to.
 *     </li>
 *     <li>
 *         Columns "Tool" and "Parameters". For {@code evaluate_expression}, the parameters provide an action
 *         that opens the expression in Marple's evaluation dialog.
 *     </li>
 * </ul>
 * The columns can be filtered via the context menu of the column header. The dialog is updated by the AI view
 * via {@link #setTranscript(List)}.
 */
class ToolCallsDialog extends JDialog
{
	private static final String	EVALUATE_EXPRESSION_TOOL	= "evaluate_expression";
	private static final int	MAX_TOOL_TIP_LENGTH			= 3000;

	private final InspectionContext			context;
	private final Object					thisValue;
	private final IntConsumer				showInTranscript;

	private final List<ToolCall>			toolCalls			= new ArrayList<>();
	private final ValueFilter				requestFilter;
	private final ValueFilter				toolFilter;
	private final ListBasedTable<ToolCall>	table;

	/**
	 * @param thisValue			the object in whose context the agent has evaluated expressions
	 * @param showInTranscript	shows the tool call with the specified number in the transcript
	 */
	ToolCallsDialog(@Nullable Window owner, InspectionContext context, Object thisValue, List<TranscriptEntry> transcript, IntConsumer showInTranscript) {
		super(owner, "Tool Calls", ModalityType.MODELESS);
		this.context = context;
		this.thisValue = thisValue;
		this.showInTranscript = showInTranscript;

		toolCalls.addAll(createToolCalls(transcript));
		requestFilter = ValueFilters.createSelectionFilter(context);
		toolFilter = ValueFilters.createSelectionFilter(context);
		List<ColumnDescription<ToolCall>> columnDescriptions = Arrays.asList(
			new ColumnDescriptionBuilder<>("#",				Integer.class,	ToolCall::getNumber).build(),
			new ColumnDescriptionBuilder<>("Request",		Integer.class,	ToolCall::getRequest).valueFilter(requestFilter).build(),
			new ColumnDescriptionBuilder<>("Tool",			String.class,	ToolCall::getToolName).valueFilter(toolFilter).build(),
			new ColumnDescriptionBuilder<>("Parameters",	Object.class,	this::getParametersValue).valueFilter(ValueFilters.createWildcardFilter()).build()
		);
		table = new ListBasedTable<>(toolCalls, columnDescriptions);

		JTable internalTable = table.getInternalTable();
		TableColumnModel columnModel = internalTable.getColumnModel();
		setColumnWidths(columnModel, 0, 40, 60);
		setColumnWidths(columnModel, 1, 60, 80);
		setColumnWidths(columnModel, 2, 180, 300);
		columnModel.getColumn(3).setPreferredWidth(600);
		columnModel.getColumn(0).setCellRenderer(new NumberRenderer());
		columnModel.getColumn(3).setCellRenderer(new ParametersRenderer());
		internalTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		// a plain click handler instead of an action provider: there is only one action, so no popup menu is needed
		internalTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				onMouseReleased(e);
			}
		});

		JLabel hintLabel = new JLabel("Click a number to highlight the tool call in the transcript. Right-click a column header to filter.");
		hintLabel.setForeground(Color.GRAY);

		JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPanel.add(table, BorderLayout.CENTER);
		contentPanel.add(hintLabel, BorderLayout.SOUTH);
		setContentPane(contentPanel);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setSize(950, 400);
		setLocationRelativeTo(owner);
		scrollToRow(internalTable.getRowCount() - 1);
	}

	/**
	 * Updates the dialog with the tool calls of the transcript. The selection is preserved.
	 */
	void setTranscript(List<TranscriptEntry> transcript) {
		Set<Integer> selectedToolCallNumbers = getSelectedToolCallNumbers();
		toolCalls.clear();
		toolCalls.addAll(createToolCalls(transcript));
		// the selection filters only know the values that existed when the table was created
		for (ToolCall toolCall : toolCalls) {
			requestFilter.addAvailableValue(toolCall.getRequest());
			toolFilter.addAvailableValue(toolCall.getToolName());
		}
		JTable internalTable = table.getInternalTable();
		((AbstractTableModel) internalTable.getModel()).fireTableDataChanged();
		if (selectedToolCallNumbers.isEmpty()) {
			scrollToRow(internalTable.getRowCount() - 1);
		} else {
			selectToolCalls(toolCall -> selectedToolCallNumbers.contains(toolCall.getNumber()));
		}
	}

	/**
	 * Selects the tool calls with the numbers {@code first} to {@code last} (1-based) as far as they are
	 * not hidden by a filter.
	 */
	void selectToolCalls(int first, int last) {
		selectToolCalls(toolCall -> first <= toolCall.getNumber() && toolCall.getNumber() <= last);
	}

	private void selectToolCalls(Predicate<ToolCall> predicate) {
		JTable internalTable = table.getInternalTable();
		internalTable.clearSelection();
		int firstViewRow = -1;
		for (int modelRow = 0; modelRow < toolCalls.size(); modelRow++) {
			if (!predicate.test(toolCalls.get(modelRow))) {
				continue;
			}
			int viewRow = internalTable.convertRowIndexToView(modelRow);
			if (viewRow >= 0) {
				internalTable.addRowSelectionInterval(viewRow, viewRow);
				firstViewRow = firstViewRow < 0 ? viewRow : Math.min(firstViewRow, viewRow);
			}
		}
		scrollToRow(firstViewRow);
	}

	private Set<Integer> getSelectedToolCallNumbers() {
		JTable internalTable = table.getInternalTable();
		Set<Integer> toolCallNumbers = new HashSet<>();
		for (int viewRow : internalTable.getSelectedRows()) {
			toolCallNumbers.add(toolCalls.get(internalTable.convertRowIndexToModel(viewRow)).getNumber());
		}
		return toolCallNumbers;
	}

	private void scrollToRow(int viewRow) {
		JTable internalTable = table.getInternalTable();
		if (viewRow >= 0 && viewRow < internalTable.getRowCount()) {
			internalTable.scrollRectToVisible(internalTable.getCellRect(viewRow, 0, true));
		}
	}

	private void onMouseReleased(MouseEvent e) {
		JTable internalTable = table.getInternalTable();
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}
		int viewRow = internalTable.rowAtPoint(e.getPoint());
		int viewColumn = internalTable.columnAtPoint(e.getPoint());
		if (viewRow < 0 || viewColumn < 0 || internalTable.convertColumnIndexToModel(viewColumn) != 0) {
			return;
		}
		showInTranscript.accept(toolCalls.get(internalTable.convertRowIndexToModel(viewRow)).getNumber());
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

	private static void setColumnWidths(TableColumnModel columnModel, int column, int preferredWidth, int maxWidth) {
		columnModel.getColumn(column).setPreferredWidth(preferredWidth);
		columnModel.getColumn(column).setMaxWidth(maxWidth);
	}

	/**
	 * Tool calls are numbered from 1 in the order of the transcript. The request of a tool call is the number of
	 * user messages before it.
	 */
	private static List<ToolCall> createToolCalls(List<TranscriptEntry> transcript) {
		List<ToolCall> toolCalls = new ArrayList<>();
		int request = 0;
		for (TranscriptEntry entry : transcript) {
			if (entry.getRole() == TranscriptEntry.Role.USER) {
				request++;
			}
			String toolName = entry.getToolName();
			JsonObject input = entry.getToolInput();
			if (toolName != null && input != null) {
				toolCalls.add(new ToolCall(toolCalls.size() + 1, request, toolName, input));
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
		private final int		request;
		private final String	toolName;
		private final String	parameters;
		private final String	fullParameters;
		@Nullable
		private final String	expression;

		ToolCall(int number, int request, String toolName, JsonObject input) {
			this.number = number;
			this.request = request;
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

		int getRequest() {
			return request;
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
	 * Renders the number like a link (as Marple renders action providers) to indicate that it can be clicked.
	 */
	private static class NumberRenderer extends DefaultTableCellRenderer
	{
		private static final Color	DARK_BLUE	= Color.BLUE.darker();

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			component.setForeground(DARK_BLUE);
			setHorizontalAlignment(LEADING);
			setToolTipText("Highlights the tool call in the transcript");
			return component;
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
