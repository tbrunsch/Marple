package dd.kms.marple.impl.gui.inspector.views.methodview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.impl.gui.filters.ValueFilter;
import dd.kms.marple.impl.gui.filters.ValueFilters;
import dd.kms.zenodot.api.common.MethodScanner;
import dd.kms.zenodot.api.common.MethodScannerBuilder;
import dd.kms.zenodot.api.common.StaticMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class MethodList extends JPanel implements ObjectView
{
	private final DefaultListModel<Method>	listModel				= new DefaultListModel<>();
	private final JList<Method>				list					= new JList<>(listModel);
	private final JScrollPane				listScrollPane			= new JScrollPane(list);

	private final JPanel					filterPanel				= new JPanel(new GridBagLayout());
	private final JLabel					nameLabel				= new JLabel("Name:");
	private final Component					nameFilterEditor;
	private final JLabel					accessModifierLabel		= new JLabel("Minimum access modifier:");
	private final Component					modifierFilterEditor;

	private final List<Method>				methods;
	private MethodViewUtils					methodViewUtils;

	private final ValueFilter				nameFilter				= ValueFilters.createWildcardFilter();
	private final ValueFilter				modifierFilter			= ValueFilters.createModifierFilter(false);

	public MethodList(Object object, InspectionContext context) {
		super(new GridBagLayout());

		this.methodViewUtils = new MethodViewUtils(object, context);

		this.nameFilterEditor = nameFilter.getEditor();
		this.modifierFilterEditor = modifierFilter.getEditor();

		add(filterPanel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(listScrollPane,	new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		int xPos = 0;
		filterPanel.add(nameLabel,				new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		filterPanel.add(nameFilterEditor,		new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		filterPanel.add(accessModifierLabel,	new GridBagConstraints(xPos++, 0, 1, 1, 0.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));
		filterPanel.add(modifierFilterEditor,	new GridBagConstraints(xPos++, 0, 1, 1, 1.0, 0.0, NORTHWEST, NONE, DEFAULT_INSETS, 0, 0));

		filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

		if (nameFilterEditor instanceof JTextField) {
			((JTextField) nameFilterEditor).setColumns(20);
		}

		MethodScanner methodScanner = MethodScannerBuilder.create().staticMode(StaticMode.BOTH).build();
		this.methods = methodScanner.getMethods(object.getClass());
		updateListModel();
		list.setSelectionModel(new NoItemSelectionModel());
		list.setCellRenderer(new ActionProviderRenderer(context));

		addListeners();
	}

	@Override
	public String getViewName() {
		return "Quick Method View";
	}

	@Override
	public Component getViewComponent() {
		return this;
	}

	@Override
	public Object getViewSettings() {
		Object nameFilterSettings = nameFilter.getSettings();
		Object modifierFilterSettings = modifierFilter.getSettings();
		return new MethodListSettings(nameFilterSettings, modifierFilterSettings);
	}

	@Override
	public void applyViewSettings(Object settings, ViewSettingsOrigin origin) {
		if (settings instanceof MethodListSettings) {
			MethodListSettings listSettings = (MethodListSettings) settings;
			if (origin == ViewSettingsOrigin.SAME_CONTEXT) {
				nameFilter.applySettings(listSettings.getNameFilterSettings());
				modifierFilter.applySettings(listSettings.getModifierFilterSettings());
			}
		}
	}

	private void addListeners() {
		nameFilter.addFilterChangedListener(this::onFilterChanged);
		modifierFilter.addFilterChangedListener(this::onFilterChanged);

		ActionProviderListeners.addMouseListeners(list, this::getActionProvider);
	}

	private void updateListModel() {
		listModel.clear();
		methods.stream().filter(this::isMethodVisible).forEach(listModel::addElement);
	}

	private boolean isMethodVisible(Method method) {
		if (nameFilter.isActive() && !nameFilter.test(method.getName())) {
			return false;
		}
		if (modifierFilter.isActive() && !modifierFilter.test(method)) {
			return false;
		}
		return true;
	}

	private ActionProvider getActionProvider(MouseEvent e) {
		int row = list.locationToIndex(e.getPoint());
		if (row < 0 || row >= methods.size()) {
			return null;
		}
		Method method = listModel.get(row);
		return methodViewUtils.getMethodActionProvider(method);
	}

	/*
	 * Listeners
	 */
	private void onFilterChanged() {
		updateListModel();
	}

	@Override
	public void dispose() {
		methodViewUtils = null;
	}

	private static class ActionProviderRenderer extends JPanel implements ListCellRenderer<Method>
	{
		private final JLabel			returnTypeLabel	= new JLabel();
		private final JLabel			methodNameLabel	= new JLabel();
		private final JLabel			argumentsLabel	= new JLabel();

		private final InspectionContext	context;

		ActionProviderRenderer(InspectionContext context) {
			super(new GridBagLayout());
			this.context = context;

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
			argumentsLabel.setText(MessageFormat.format("({0})", MethodViewUtils.formatArguments(method, context)));

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

	private static class MethodListSettings
	{
		private final Object	nameFilterSettings;
		private final Object	modifierFilterSettings;

		MethodListSettings(Object nameFilterSettings, Object modifierFilterSettings) {
			this.nameFilterSettings = nameFilterSettings;
			this.modifierFilterSettings = modifierFilterSettings;
		}

		Object getNameFilterSettings() {
			return nameFilterSettings;
		}

		Object getModifierFilterSettings() {
			return modifierFilterSettings;
		}
	}
}
