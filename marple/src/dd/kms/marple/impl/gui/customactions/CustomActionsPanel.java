package dd.kms.marple.impl.gui.customactions;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.api.settings.actions.CustomAction;
import dd.kms.marple.api.settings.actions.CustomActionSettings;
import dd.kms.marple.api.settings.keys.KeyRepresentation;
import dd.kms.marple.impl.gui.common.ExceptionFormatter;
import dd.kms.marple.impl.gui.table.*;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class CustomActionsPanel extends JPanel implements Disposable
{
	public static final String	TITLE	= "Custom Actions";

	private final JScrollPane						scrollPane;
	private final JTable							table;
	private final ListBasedTableModel<CustomAction>	tableModel;

	private final JPanel							exceptionPanel		= new JPanel(new GridBagLayout());
	private final JLabel							exceptionLabel		= new JLabel();

	private final CustomActionSettings				customActionSettings;
	private final List<CustomAction>				customActions;

	public CustomActionsPanel(InspectionContext context) {
		super(new GridBagLayout());

		this.customActionSettings = context.getSettings().getCustomActionSettings();
		this.customActions = new ArrayList<>(customActionSettings.getCustomActions());

		List<ColumnDescription<CustomAction>> columnDescriptions = createColumnDescriptions();
		tableModel = new ListBasedTableModel<>(customActions, columnDescriptions);
		table = new JTable(tableModel);
		table.setRowHeight(20);
		scrollPane = new JScrollPane(table);

		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(1).setCellEditor(new ActionExpressionCellEditor(this::getRequiredClass, this::onException, context));
		columnModel.getColumn(2).setCellEditor(new ClassCellEditor(this::onException, context));

		table.setDefaultRenderer(Class.class, new ClassRenderer(context));

		add(scrollPane,		new GridBagConstraints(0, 0, REMAINDER, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,	GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));

		add(exceptionPanel,	new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST,	GridBagConstraints.HORIZONTAL, DEFAULT_INSETS, 0, 0));

		exceptionPanel.setVisible(false);
		exceptionPanel.add(exceptionLabel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		exceptionLabel.setForeground(Color.RED);


		setPreferredSize(new Dimension(800, 300));

		onTableContentChanged();

		addListeners();
	}

	private void onTableContentChanged() {
		customActionSettings.setCustomActions(customActions);

		updateButtons();
	}

	private Class<?> getRequiredClass(int row) {
		return 0 <= row && row < customActions.size()
			? customActions.get(row).getThisClass()
			: Object.class;
	}

	private void onException(@Nullable Throwable exception) {
		exceptionPanel.setVisible(exception != null);
		String exceptionMessage = exception == null ? null : ExceptionFormatter.formatException(exception, true);
		exceptionLabel.setText(exceptionMessage);
	}

	private void addListeners() {
		tableModel.addTableModelListener(e -> onTableContentChanged());

		// ...
	}

	private void updateButtons() {
		// TODO
	}

	private List<ColumnDescription<CustomAction>> createColumnDescriptions() {
		return Arrays.asList(
			new ColumnDescriptionBuilder<>("Name",					String.class, 				CustomAction::getName)				.editorSettings(this::changeName).build(),
			new ColumnDescriptionBuilder<>("Action expression",		String.class, 				CustomAction::getActionExpression)	.editorSettings(this::changeExpression).build(),
			new ColumnDescriptionBuilder<>("Required class",		Class.class, 				CustomAction::getThisClass)			.editorSettings(this::changeThisClass).build(),
			new ColumnDescriptionBuilder<>("Key",					KeyRepresentation.class,	CustomAction::getKey)				.editorSettings(this::changeKey).build()
		);
	}

	private void changeName(List<CustomAction> customActions, int elementIndex, Object nameAsObject) {
		if (!(nameAsObject instanceof String)) {
			return;
		}
		String name = (String) nameAsObject;
		if (name.trim().isEmpty()) {
			return;
		}
		CustomAction oldAction = customActions.get(elementIndex);
		CustomAction newAction = CustomAction.create(name, oldAction.getActionExpression(), oldAction.getThisClass(), oldAction.getKey());
		customActions.set(elementIndex, newAction);
		onException(null);
	}

	private void changeExpression(List<CustomAction> customActions, int elementIndex, Object expressionAsObject) {
		if (!(expressionAsObject instanceof String)) {
			return;
		}
		String expression = (String) expressionAsObject;
		CustomAction oldAction = customActions.get(elementIndex);
		CustomAction newAction = CustomAction.create(oldAction.getName(), expression, oldAction.getThisClass(), oldAction.getKey());
		customActions.set(elementIndex, newAction);
		onException(null);
	}

	private void changeThisClass(List<CustomAction> customActions, int elementIndex, Object classAsObject) {
		if (!(classAsObject instanceof Class<?>)) {
			return;
		}
		Class<?> clazz = (Class<?>) classAsObject;
		CustomAction oldAction = customActions.get(elementIndex);
		CustomAction newAction = CustomAction.create(oldAction.getName(), oldAction.getActionExpression(), clazz, oldAction.getKey());
		customActions.set(elementIndex, newAction);
		onException(null);
	}

	private void changeKey(List<CustomAction> customActions, int elementIndex, Object keyAsObject) {
		if (keyAsObject != null && !(keyAsObject instanceof KeyRepresentation)) {
			return;
		}
		KeyRepresentation key = (KeyRepresentation) keyAsObject;
		CustomAction oldAction = customActions.get(elementIndex);
		CustomAction newAction = CustomAction.create(oldAction.getName(), oldAction.getActionExpression(), oldAction.getThisClass(), key);
		customActions.set(elementIndex, newAction);
		onException(null);
	}

	@Override
	public void dispose() {
		/* Currently there is nothing to do */
	}
}
