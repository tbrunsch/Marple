package dd.kms.marple.impl.aiagent;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.impl.gui.table.*;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Non-modal dialog that shows the variables the agent has created in the current conversation. The user can
 * keep them (they become ordinary variables of the user) or delete them. The values provide the same
 * actions as in Marple's variable dialog (e.g., inspecting them).<br>
 * <br>
 * The dialog follows changes of the variables. It closes itself when no variables are left. The AI view
 * closes it when it is disposed (e.g., when an action navigates to another object).
 */
class AgentVariablesDialog extends JDialog
{
	private final InspectionContext				context;
	private final AgentContext					agentContext;
	private final Runnable						changeListener	= this::onVariablesChanged;

	private final List<Variable>				variables		= new ArrayList<>();
	private final ListBasedTableModel<Variable>	tableModel;
	private final JTable						table;

	private final JButton						keepButton		= new JButton("Keep");
	private final JButton						deleteButton	= new JButton("Delete");
	private final JButton						deleteAllButton	= new JButton("Delete all");

	AgentVariablesDialog(@Nullable Window owner, InspectionContext context, AgentContext agentContext) {
		super(owner, "Agent Variables", ModalityType.MODELESS);
		this.context = context;
		this.agentContext = agentContext;

		List<ColumnDescription<Variable>> columnDescriptions = Arrays.asList(
			new ColumnDescriptionBuilder<>("Name",	String.class,			Variable::getName).build(),
			new ColumnDescriptionBuilder<>("Value",	ActionProvider.class,	this::getValueAsActionProvider).build(),
			new ColumnDescriptionBuilder<>("Type",	Class.class,			Variable::getType).build()
		);
		tableModel = new ListBasedTableModel<>(variables, columnDescriptions);
		table = new JTable(tableModel);
		table.setRowHeight(20);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.getColumnModel().getColumn(1).setCellRenderer(new ActionProviderRenderer());
		table.setDefaultRenderer(Class.class, new ClassRenderer(context));
		ActionProviderListeners.addMouseListeners(table);

		keepButton.setToolTipText("Keeps the selected variables: they are added to your variables");
		deleteButton.setToolTipText("Deletes the selected variables");
		deleteAllButton.setToolTipText("Deletes all variables the agent has created in this conversation");

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		buttonPanel.add(keepButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(deleteAllButton);

		JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(contentPanel);

		keepButton.addActionListener(e -> keepSelectedVariables());
		deleteButton.addActionListener(e -> getSelectedNames().forEach(agentContext::delete));
		deleteAllButton.addActionListener(e -> deleteAll());
		table.getSelectionModel().addListSelectionListener(e -> updateButtons());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		agentContext.addChangeListener(changeListener);
		updateContent();

		setSize(700, 350);
		setLocationRelativeTo(owner);
	}

	@Override
	public void dispose() {
		agentContext.removeChangeListener(changeListener);
		super.dispose();
	}

	private void onVariablesChanged() {
		if (agentContext.getVariables().isEmpty()) {
			dispose();
		} else {
			updateContent();
		}
	}

	private void updateContent() {
		List<String> selectedNames = getSelectedNames();
		variables.clear();
		variables.addAll(agentContext.getVariables());
		tableModel.fireTableChanged(new TableModelEvent(tableModel));
		for (int row = 0; row < variables.size(); row++) {
			if (selectedNames.contains(variables.get(row).getName())) {
				table.addRowSelectionInterval(row, row);
			}
		}
		updateButtons();
	}

	private void updateButtons() {
		boolean hasSelection = table.getSelectedRowCount() > 0;
		keepButton.setEnabled(hasSelection);
		deleteButton.setEnabled(hasSelection);
		deleteAllButton.setEnabled(!variables.isEmpty());
	}

	private ActionProvider getValueAsActionProvider(Variable variable) {
		Object value = variable.getValue();
		return new ActionProviderBuilder(context.getDisplayText(value), value, context)
			.evaluateAs(variable.getName())
			.build();
	}

	private List<String> getSelectedNames() {
		List<String> names = new ArrayList<>();
		for (int row : table.getSelectedRows()) {
			names.add(variables.get(table.convertRowIndexToModel(row)).getName());
		}
		return names;
	}

	/**
	 * Adds the selected variables to the user's variables. Asks before replacing variables of the user.
	 */
	private void keepSelectedVariables() {
		ExpressionEvaluator userEvaluator = context.getEvaluator();
		List<String> names = getSelectedNames();
		List<String> existingNames = names.stream()
			.filter(name -> AgentContext.findVariable(userEvaluator, name) != null)
			.collect(Collectors.toList());
		if (!existingNames.isEmpty()) {
			int answer = JOptionPane.showConfirmDialog(this,
				"Replace your variables " + String.join(", ", existingNames) + "?",
				"Keep agent variables", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (answer != JOptionPane.YES_OPTION) {
				return;
			}
		}
		names.forEach(name -> agentContext.keep(name, userEvaluator));
	}

	private void deleteAll() {
		int answer = JOptionPane.showConfirmDialog(this,
			"Delete all " + variables.size() + " variables the agent has created in this conversation?",
			"Delete agent variables", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (answer == JOptionPane.YES_OPTION) {
			agentContext.deleteAll();
		}
	}
}
