package dd.kms.marple.impl.aiagent;

import dd.kms.marple.api.InspectionContext;
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
import java.util.function.BiConsumer;

/**
 * Non-modal dialog that shows the variables the agent has created in the current conversation. The user can
 * keep them (they become ordinary variables of the user) or delete them. The values provide the same
 * actions as in Marple's variable dialog (e.g., inspecting them).<br>
 * <br>
 * The dialog follows changes of the variables. It closes itself when the conversation becomes inactive
 * (e.g., when an action navigates to another object, which disposes the AI view) or when no variables
 * are left.
 */
class AgentVariablesDialog extends JDialog
{
	private final InspectionContext				context;
	private final AgentVariables				agentVariables;
	private final Runnable						changeListener	= this::onVariablesChanged;

	private final List<Variable>				variables		= new ArrayList<>();
	private final ListBasedTableModel<Variable>	tableModel;
	private final JTable						table;

	private final JButton						keepButton		= new JButton("Keep");
	private final JButton						deleteButton	= new JButton("Delete");
	private final JButton						deleteAllButton	= new JButton("Delete all");

	AgentVariablesDialog(@Nullable Window owner, InspectionContext context, AgentVariables agentVariables) {
		super(owner, "Agent Variables", ModalityType.MODELESS);
		this.context = context;
		this.agentVariables = agentVariables;

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

		keepButton.setToolTipText("Keeps the selected variables: they become your variables and are not removed when the conversation ends");
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

		keepButton.addActionListener(e -> forSelectedVariables(AgentVariables::keep));
		deleteButton.addActionListener(e -> forSelectedVariables(AgentVariables::delete));
		deleteAllButton.addActionListener(e -> deleteAll());
		table.getSelectionModel().addListSelectionListener(e -> updateButtons());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		agentVariables.addChangeListener(changeListener);
		updateContent();

		setSize(700, 350);
		setLocationRelativeTo(owner);
	}

	@Override
	public void dispose() {
		agentVariables.removeChangeListener(changeListener);
		super.dispose();
	}

	private void onVariablesChanged() {
		if (!agentVariables.isAttached() || agentVariables.getVariables().isEmpty()) {
			dispose();
		} else {
			updateContent();
		}
	}

	private void updateContent() {
		List<String> selectedNames = getSelectedNames();
		variables.clear();
		variables.addAll(agentVariables.getVariables());
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

	private void forSelectedVariables(BiConsumer<AgentVariables, String> operation) {
		for (String name : getSelectedNames()) {
			operation.accept(agentVariables, name);
		}
	}

	private void deleteAll() {
		int answer = JOptionPane.showConfirmDialog(this,
			"Delete all " + variables.size() + " variables the agent has created in this conversation?",
			"Delete agent variables", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (answer == JOptionPane.YES_OPTION) {
			agentVariables.deleteAll();
		}
	}
}
