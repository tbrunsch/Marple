package dd.kms.marple.gui.inspector.views.methodview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.actions.Actions;
import dd.kms.marple.actions.ImmediateInspectionAction;
import dd.kms.marple.gui.filters.ValueFilter;
import dd.kms.marple.gui.filters.ValueFilters;
import dd.kms.zenodot.common.MethodScanner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

import static java.awt.GridBagConstraints.*;

class MethodList extends JPanel
{
	private static final Insets DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	private final List<Method>				methods;

	private final DefaultListModel<Method>	listModel					= new DefaultListModel<>();
	private final JList<Method>				list						= new JList<>(listModel);
	private final JScrollPane				listScrollPane				= new JScrollPane(list);

	private final JPanel					filterPanel					= new JPanel(new GridBagLayout());
	private final JLabel					nameLabel					= new JLabel("Name:");
	private final Component					nameFilterEditor;
	private final JLabel					accessLevelLabel			= new JLabel("Access level:");
	private final Component					accessLevelFilterEditor;

	private final MethodViewUtils			methodViewUtils;

	private final ValueFilter				nameFilter					= ValueFilters.createWildcardFilter();
	private final ValueFilter				accessLevelFilter			= ValueFilters.createMinimumAccessLevelFilter();

	public MethodList(Object object, InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.methodViewUtils = new MethodViewUtils(object, inspectionContext);

		this.nameFilterEditor = nameFilter.getEditor();
		this.accessLevelFilterEditor = accessLevelFilter.getEditor();

		add(filterPanel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(listScrollPane,	new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		int xPos = 0;
		filterPanel.add(nameLabel,					new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		filterPanel.add(nameFilterEditor,			new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		filterPanel.add(accessLevelLabel,			new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		filterPanel.add(accessLevelFilterEditor,	new GridBagConstraints(xPos++, 0, 1, 1, 1.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));

		filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

		if (nameFilterEditor instanceof JTextField) {
			((JTextField) nameFilterEditor).setColumns(20);
		}

		this.methods = new MethodScanner().getMethods(object.getClass());
		updateListModel();
		list.setSelectionModel(new NoItemSelectionModel());
		list.setCellRenderer(new ActionProviderRenderer(methodViewUtils));

		addListeners();
	}

	private void addListeners() {
		nameFilter.addFilterChangedListener(this::onFilterChanged);
		accessLevelFilter.addFilterChangedListener(this::onFilterChanged);
		list.addMouseListener(new ActionProviderListMouseListener());
		list.addMouseMotionListener(new ActionProviderListMouseMotionListener());
	}

	private void updateListModel() {
		listModel.clear();
		methods.stream().filter(this::isMethodVisible).forEach(listModel::addElement);
	}

	private boolean isMethodVisible(Method method) {
		if (nameFilter.isActive() && !nameFilter.test(method.getName())) {
			return false;
		}
		if (accessLevelFilter.isActive() && !accessLevelFilter.test(methodViewUtils.getAccessModifier(method))) {
			return false;
		}
		return true;
	}

	private ActionProvider getActionProvider(MouseEvent e) {
		int row = list.locationToIndex(e.getPoint());
		if (row < 0 || row >= methods.size()) {
			return null;
		}
		Method method = methods.get(row);
		return methodViewUtils.getMethodActionProvider(method);
	}

	/*
	 * Listeners
	 */
	private void onFilterChanged() {
		updateListModel();
	}

	private static class ActionProviderRenderer extends JPanel implements ListCellRenderer<Method>
	{
		private final JLabel			returnTypeLabel	= new JLabel();
		private final JLabel			methodNameLabel	= new JLabel();
		private final JLabel			argumentsLabel	= new JLabel();

		private final MethodViewUtils	methodViewUtils;

		ActionProviderRenderer(MethodViewUtils methodViewUtils) {
			super(new GridBagLayout());

			this.methodViewUtils = methodViewUtils;

			int xPos = 0;
			add(methodNameLabel,	new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(1, 1, 1, 1), 0, 0));
			add(argumentsLabel,		new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(1, 1, 1, 1), 0, 0));
			add(returnTypeLabel,	new GridBagConstraints(xPos++, 0, 1, 1, 1.0, 0.0, EAST, NONE, new Insets(1, 1, 1, 1), 0, 0));

			setFontStyle(returnTypeLabel,	Font.PLAIN);
			setFontStyle(methodNameLabel,	Font.BOLD);
			setFontStyle(argumentsLabel,	Font.PLAIN);

			returnTypeLabel.setEnabled(false);
			argumentsLabel.setEnabled(false);
		}

		private void setFontStyle(Component component, int fontStyle) {
			Font oldFont = component.getFont();
			Font newFont = oldFont.deriveFont(fontStyle);
			component.setFont(newFont);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Method> list, Method method, int index, boolean isSelected, boolean cellHasFocus) {
			returnTypeLabel.setText(method.getReturnType().getSimpleName());
			methodNameLabel.setText(method.getName());
			argumentsLabel.setText(MessageFormat.format("({0})", methodViewUtils.getArgumentsAsString(method)));

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			return this;
		}
	}

	private class ActionProviderListMouseListener extends MouseAdapter
	{
		@Override
		public void mouseReleased(MouseEvent e) {
			ActionProvider actionProvider = getActionProvider(e);
			if (actionProvider != null) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					Actions.performDefaultAction(actionProvider);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					Actions.showActionPopup(e.getComponent(), actionProvider, e);
				}
			}
		}
	}

	private class ActionProviderListMouseMotionListener extends MouseMotionAdapter
	{
		@Override
		public void mouseMoved(MouseEvent e) {
			ActionProvider actionProvider = getActionProvider(e);
			Cursor cursor = Actions.hasDefaultAction(actionProvider)
				? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
				: Cursor.getDefaultCursor();
			e.getComponent().setCursor(cursor);

			Actions.performImmediateActions(actionProvider);
		}
	}

	private static class NoItemSelectionModel extends DefaultListSelectionModel
	{
		@Override
		public void setSelectionInterval(int index0, int index1) {
			super.setSelectionInterval(-1, -1);
		}
	}
}
