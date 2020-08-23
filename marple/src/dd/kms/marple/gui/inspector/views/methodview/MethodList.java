package dd.kms.marple.gui.inspector.views.methodview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.actions.ActionProvider;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.gui.filters.ValueFilter;
import dd.kms.marple.gui.filters.ValueFilters;
import dd.kms.zenodot.api.common.MethodScanner;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.List;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class MethodList extends JPanel
{
	private final DefaultListModel<ExecutableInfo>	listModel					= new DefaultListModel<>();
	private final JList<ExecutableInfo>				list						= new JList<>(listModel);
	private final JScrollPane						listScrollPane				= new JScrollPane(list);

	private final JPanel							filterPanel					= new JPanel(new GridBagLayout());
	private final JLabel							nameLabel					= new JLabel("Name:");
	private final Component							nameFilterEditor;
	private final JLabel							accessModifierLabel			= new JLabel("Minimum access modifier:");
	private final Component							accessModifierFilterEditor;

	private final List<ExecutableInfo>				methodInfos;
	private final MethodViewUtils					methodViewUtils;

	private final ValueFilter						nameFilter					= ValueFilters.createWildcardFilter();
	private final ValueFilter						accessModifierFilter		= ValueFilters.createMinimumAccessModifierFilter();

	public MethodList(ObjectInfo objectInfo, InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.methodViewUtils = new MethodViewUtils(objectInfo, inspectionContext);

		this.nameFilterEditor = nameFilter.getEditor();
		this.accessModifierFilterEditor = accessModifierFilter.getEditor();

		add(filterPanel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(listScrollPane,	new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		int xPos = 0;
		filterPanel.add(nameLabel,					new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		filterPanel.add(nameFilterEditor,			new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		filterPanel.add(accessModifierLabel,		new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		filterPanel.add(accessModifierFilterEditor,	new GridBagConstraints(xPos++, 0, 1, 1, 1.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));

		filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

		if (nameFilterEditor instanceof JTextField) {
			((JTextField) nameFilterEditor).setColumns(20);
		}

		this.methodInfos = InfoProvider.getMethodInfos(ReflectionUtils.getRuntimeTypeInfo(objectInfo), new MethodScanner());
		updateListModel();
		list.setSelectionModel(new NoItemSelectionModel());
		list.setCellRenderer(new ActionProviderRenderer());

		addListeners();
	}

	private void addListeners() {
		nameFilter.addFilterChangedListener(this::onFilterChanged);
		accessModifierFilter.addFilterChangedListener(this::onFilterChanged);

		ActionProviderListeners.addMouseListeners(list, this::getActionProvider);
	}

	private void updateListModel() {
		listModel.clear();
		methodInfos.stream().filter(this::isMethodVisible).forEach(listModel::addElement);
	}

	private boolean isMethodVisible(ExecutableInfo methodInfo) {
		if (nameFilter.isActive() && !nameFilter.test(methodInfo.getName())) {
			return false;
		}
		if (accessModifierFilter.isActive() && !accessModifierFilter.test(methodInfo.getAccessModifier())) {
			return false;
		}
		return true;
	}

	private ActionProvider getActionProvider(MouseEvent e) {
		int row = list.locationToIndex(e.getPoint());
		if (row < 0 || row >= methodInfos.size()) {
			return null;
		}
		ExecutableInfo methodInfo = methodInfos.get(row);
		return methodViewUtils.getMethodActionProvider(methodInfo);
	}

	/*
	 * Listeners
	 */
	private void onFilterChanged() {
		updateListModel();
	}

	private static class ActionProviderRenderer extends JPanel implements ListCellRenderer<ExecutableInfo>
	{
		private final JLabel			returnTypeLabel	= new JLabel();
		private final JLabel			methodNameLabel	= new JLabel();
		private final JLabel			argumentsLabel	= new JLabel();

		ActionProviderRenderer() {
			super(new GridBagLayout());

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
		public Component getListCellRendererComponent(JList<? extends ExecutableInfo> list, ExecutableInfo methodInfo, int index, boolean isSelected, boolean cellHasFocus) {
			returnTypeLabel.setText(methodInfo.getReturnType().getSimpleName());
			methodNameLabel.setText(methodInfo.getName());
			argumentsLabel.setText(MessageFormat.format("({0})", methodInfo.formatArguments()));

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

	private static class NoItemSelectionModel extends DefaultListSelectionModel
	{
		@Override
		public void setSelectionInterval(int index0, int index1) {
			super.setSelectionInterval(-1, -1);
		}
	}
}
