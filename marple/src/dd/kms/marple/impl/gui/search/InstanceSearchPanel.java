package dd.kms.marple.impl.gui.search;

import com.google.common.base.Strings;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNodes;
import dd.kms.marple.impl.gui.common.ExceptionFormatter;
import dd.kms.marple.impl.gui.evaluator.completion.CodeCompletionDecorator;
import dd.kms.marple.impl.gui.evaluator.textfields.ClassInputTextField;
import dd.kms.marple.impl.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.impl.gui.evaluator.textfields.LambdaExpressionInputTextField;
import dd.kms.marple.impl.instancesearch.InstancePath;
import dd.kms.marple.impl.instancesearch.InstancePathFinder;
import dd.kms.marple.impl.instancesearch.settings.SearchSettings;
import dd.kms.marple.impl.instancesearch.settings.SearchSettingsBuilders;
import dd.kms.zenodot.api.CompiledLambdaExpression;
import dd.kms.zenodot.api.ParseException;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

class InstanceSearchPanel extends JPanel
{
	private final JPanel							configurationPanel			= new JPanel(new GridBagLayout());
	private final JLabel							rootLabel					= new JLabel("Root of search:");
	private final JLabel							rootValueLabel				= new JLabel("---");
	private final JLabel							targetLabel					= new JLabel("Instances to find:");
	private final JRadioButton						targetConcreteInstanceRB	= new JRadioButton("concrete instance:");
	private final JLabel							targetValueLabel			= new JLabel("---");
	private final JRadioButton						targetAllInstancesRB		= new JRadioButton("all instances of class:");
	private final ClassInputTextField				targetClassTF;
	private final JPanel							targetClassPanel;
	private final JCheckBox							targetFilterCB				= new JCheckBox("filter:");
	private final LambdaExpressionInputTextField	targetFilterTF;
	private final JPanel							targetFilterPanel;
	private final JLabel							optionsLabel				= new JLabel("Options:");
	private final JCheckBox							onlyNonStaticFieldsCB		= new JCheckBox("non-static fields only");
	private final JCheckBox							onlyPureFieldsCB			= new JCheckBox("pure fields only");
	private final JCheckBox							limitSearchDepthCB			= new JCheckBox("limit search depth:");
	private final JTextField						maxSearchDepthTF			= new JTextField();
	private final JLabel							errorLabel					= new JLabel();

	private final ButtonGroup						targetButtonGroup			= new ButtonGroup();

	private final JPanel							resultPanel					= new JPanel(new GridBagLayout());
	private final JLabel							statusLabel					= new JLabel("Status:");
	private final JTextField						statusTF					= new JTextField();
	private final JTree								instanceSearchTree			= new JTree((TreeNode) null);
	private final JScrollPane						instanceSearchScrollPane	= new JScrollPane(instanceSearchTree);
	private final JLabel							fullPathLabel				= new JLabel();

	private final JPanel							controlPanel				= new JPanel(new GridBagLayout());
	private final JButton							searchButton				= new JButton("Find");
	private final JButton							stopSearchButton			= new JButton("Stop");

	private final InspectionContext					context;

	private Object									root;
	private Object									target;

	private final InstancePathFinder				instancePathFinder;

	private final Map<InstancePath, SearchNode>		instanceSearchNodes			= new HashMap<>();
	private final Set<Object>						matchingObjects				= new HashSet<>();

	InstanceSearchPanel(InspectionContext context) {
		super(new GridBagLayout());

		this.context = context;

		targetClassTF = new ClassInputTextField(context);
		targetClassTF.setExceptionConsumer(t -> showError(ExceptionFormatter.formatParseException(t)));
		targetClassPanel = new EvaluationTextFieldPanel(targetClassTF, context);
		targetFilterTF = new LambdaExpressionInputTextField(Predicate.class, context);
		targetFilterTF.setExpression("x -> true");
		targetFilterTF.setExceptionConsumer(t -> showError(ExceptionFormatter.formatParseException(t)));
		targetFilterPanel = new EvaluationTextFieldPanel(targetFilterTF, context);

		instancePathFinder = new InstancePathFinder(this::onPathDetected);

		CodeCompletionDecorator.configureExceptionComponent(errorLabel);

		add(configurationPanel,	new GridBagConstraints(0, 0, 1, 1, 1.0, 0.1, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(resultPanel,		new GridBagConstraints(0, 1, 1, 1, 1.0, 0.9, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(controlPanel,		new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, CENTER, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		setupConfigurationPanel();
		setupResultPanel();
		setupControlPanel();

		updateEnabilities();

		addListeners();
	}

	private void setupConfigurationPanel() {
		configurationPanel.setBorder(new TitledBorder("Search Configuration"));

		int yPos = 0;

		configurationPanel.add(rootLabel,					new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(rootValueLabel,				new GridBagConstraints(1, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		configurationPanel.add(targetLabel,					new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(targetConcreteInstanceRB,	new GridBagConstraints(1, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(targetValueLabel,			new GridBagConstraints(2, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		configurationPanel.add(targetAllInstancesRB,		new GridBagConstraints(1, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(targetClassPanel,			new GridBagConstraints(2, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		configurationPanel.add(targetFilterCB,				new GridBagConstraints(1, yPos,   1, 1, 0.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(targetFilterPanel,			new GridBagConstraints(2, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		configurationPanel.add(optionsLabel,				new GridBagConstraints(0, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(onlyNonStaticFieldsCB,		new GridBagConstraints(1, yPos++, REMAINDER, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		configurationPanel.add(onlyPureFieldsCB,			new GridBagConstraints(1, yPos++, REMAINDER, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

		// Hack: We need this panel to prevent the text field from collapsing
		JPanel maxSearchDepthPanel = new JPanel(new GridBagLayout());
		maxSearchDepthPanel.add(maxSearchDepthTF,			new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, NONE, new Insets(0, 0, 0, 0), 0, 0));

		configurationPanel.add(limitSearchDepthCB,			new GridBagConstraints(1, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(maxSearchDepthPanel,			new GridBagConstraints(2, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		configurationPanel.add(errorLabel,					new GridBagConstraints(0, yPos++, REMAINDER, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		targetButtonGroup.add(targetAllInstancesRB);
		targetButtonGroup.add(targetConcreteInstanceRB);

		targetConcreteInstanceRB.setSelected(true);

		onlyNonStaticFieldsCB.setToolTipText("If selected, then static fields will be ignored for the search");
		onlyPureFieldsCB.setToolTipText("If selected, then only field values will be considered. The content of arrays, collections, maps etc. will be ignored.");
		maxSearchDepthTF.setColumns(3);

		SearchSettings defaultSettings = SearchSettingsBuilders.create().build();
		setSearchSettings(defaultSettings);
	}

	private void setupResultPanel() {
		resultPanel.setBorder(new TitledBorder("Search Result"));

		resultPanel.add(statusLabel,				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		resultPanel.add(statusTF,					new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
		resultPanel.add(instanceSearchScrollPane,	new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		resultPanel.add(fullPathLabel,				new GridBagConstraints(0, 2, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		instanceSearchTree.setRootVisible(false);
		instanceSearchTree.setShowsRootHandles(true);

		TreeCellRenderer renderer = new SearchTreeRenderer(matchingObjects, instanceSearchTree.getCellRenderer());
		instanceSearchTree.setCellRenderer(renderer);

		ActionProviderListeners.addMouseListeners(instanceSearchTree);
		ActionProviderTreeNodes.enableFullTextToolTips(instanceSearchTree);
		instanceSearchTree.addMouseMotionListener(new FullPathMouseMotionListener(fullPathLabel::setText));

		statusTF.setEditable(false);
	}

	private void setupControlPanel() {
		controlPanel.add(searchButton,		new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));
		controlPanel.add(stopSearchButton,	new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));
	}

	private void addListeners() {
		searchButton.addActionListener(e -> startSearch());
		stopSearchButton.addActionListener(e -> stopSearch());
		targetAllInstancesRB.addActionListener(e -> updateEnabilities());
		targetConcreteInstanceRB.addActionListener(e -> updateEnabilities());
		targetClassTF.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				onTargetClassSpecified();
			}
		});
		targetFilterTF.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				onTargetFilterSpecified();
			}
		});
		limitSearchDepthCB.addItemListener(e -> updateEnabilities());
		maxSearchDepthTF.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateEnabilities();
			}
		});
	}

	void setRoot(Object root) {
		this.root = root;
		updateDisplay();
	}

	void setTarget(Object target) {
		this.target = target;
		targetConcreteInstanceRB.setSelected(true);
		updateDisplay();
	}

	void setInspectionContext(InspectionContext context) {
		instancePathFinder.setInspectionContext(context);
	}

	private void updateEnabilities() {
		maxSearchDepthTF.setEnabled(limitSearchDepthCB.isSelected());
		InstancePathFinder.ProcessingState processingState = instancePathFinder.getProcessingState();
		boolean searching = processingState != InstancePathFinder.ProcessingState.NOT_RUNNING && processingState != InstancePathFinder.ProcessingState.FINISHED;
		searchButton.setEnabled(!searching && isRequiredInputSpecified());
		stopSearchButton.setEnabled(searching);
	}

	private void setSearchSettings(SearchSettings settings) {
		onlyNonStaticFieldsCB.setSelected(settings.isSearchOnlyNonStaticFields());
		onlyPureFieldsCB.setSelected(settings.isSearchOnlyPureFields());
		limitSearchDepthCB.setSelected(settings.isLimitSearchDepth());
		maxSearchDepthTF.setText(Integer.toString(settings.getMaximumSearchDepth()));
		maxSearchDepthTF.setEnabled(settings.isLimitSearchDepth());
	}

	private SearchSettings getSearchSettings() throws SettingsException {
		boolean limitSearchDepth = limitSearchDepthCB.isSelected();
		int maxSearchDepth = 0;
		if (limitSearchDepth) {
			String maxSearchDepthText = maxSearchDepthTF.getText();
			try {
				maxSearchDepth = Integer.parseInt(maxSearchDepthText);
			} catch (NumberFormatException e) {
				throw new SettingsException("Could not parse maximum search depth '" + maxSearchDepthText + "': " + e.getMessage());
			}
			if (maxSearchDepth < 0) {
				throw new SettingsException("Maximum search depth must not be negative");
			}
		}
		return SearchSettingsBuilders.create()
			.extendPathsBeyondAcceptedInstances(targetAllInstancesRB.isSelected())
			.searchOnlyNonStaticFields(onlyNonStaticFieldsCB.isSelected())
			.searchOnlyPureFields(onlyPureFieldsCB.isSelected())
			.limitSearchDepth(limitSearchDepth)
			.maximumSearchDepth(maxSearchDepth)
			.addClassesToExclude(getClass())
			.addExclusionFilter(clazz -> "sun.awt.AppContext".equals(clazz.getName()))
			.build();
	}

	private Class<?> getTargetClass() throws SettingsException {
		if (targetAllInstancesRB.isSelected()) {
			String targetClassName = targetClassTF.getText();
			if (Strings.isNullOrEmpty(targetClassName)) {
				throw new SettingsException("No target class specified");
			}
			try {
				return targetClassTF.evaluateText();
			} catch (ParseException e) {
				throw new SettingsException(ExceptionFormatter.formatParseException(e));
			} catch (Throwable t) {
				throw new SettingsException("Unknown target class '" + targetClassName + "'");
			}
		} else if (targetConcreteInstanceRB.isSelected() && target != null) {
			return target.getClass();
		} else {
			throw createNoSearchOptionSelectedException();
		}
	}

	private Predicate<Object> getTargetFilter() throws SettingsException {
		if (targetAllInstancesRB.isSelected()) {
			Class<?> targetClass = getTargetClass();
			if (targetFilterCB.isSelected()) {
				String targetFilterExpression = targetFilterTF.getText();
				try {
					CompiledLambdaExpression<?> compiledFilter = targetFilterTF.evaluateText();
					Predicate<Object> filter = (Predicate<Object>) compiledFilter.evaluate(null);
					return o -> targetClass.isInstance(o) && filter.test(o);
				} catch (ParseException e) {
					throw new SettingsException(ExceptionFormatter.formatParseException(e));
				} catch (Throwable t) {
					throw new SettingsException("Cannot compile filter expression '" + targetFilterExpression + "'");
				}
			} else {
				return targetClass::isInstance;
			}
		} else if (targetConcreteInstanceRB.isSelected()) {
			return o -> o == target;
		} else {
			throw createNoSearchOptionSelectedException();
		}
	}

	private boolean isRequiredInputSpecified() {
		try {
			checkInput();
			showError(null);
			return true;
		} catch (SettingsException e) {
			showError(e.getMessage());
			return false;
		}
	}

	private void checkInput() throws SettingsException {
		if (root == null) {
			throw new SettingsException("No root specified for search");
		}
		getSearchSettings();
		if (targetAllInstancesRB.isSelected()) {
			getTargetClass();
			if (targetFilterCB.isSelected()) {
				getTargetFilter();
			}
		} else if (targetConcreteInstanceRB.isSelected()) {
			if (target == null) {
				throw new SettingsException("No instance to search for specified");
			}
		} else {
			onNoSearchOptionSelected();
		}
	}

	private void updateDisplayWhileSearching() {
		while (instancePathFinder.getProcessingState() != InstancePathFinder.ProcessingState.FINISHED) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
			SwingUtilities.invokeLater(this::updateDisplay);
		}
		SwingUtilities.invokeLater(this::updateDisplay);
	}

	private void updateDisplay() {
		updateEnabilities();
		updateValueTexts();
		updateStatusText();
	}

	private void updateValueTexts() {
		rootValueLabel.setText(getDisplayText(root));
		targetValueLabel.setText(getDisplayText(target));
	}

	private String getDisplayText(Object object) {
		return object == null ? "<none>" : context.getDisplayText(object);
	}

	private void updateStatusText() {
		String statusText = instancePathFinder.getStatusText();
		statusTF.setText(statusText);
		statusTF.revalidate();
		statusTF.repaint();
	}

	private void showError(@Nullable String error) {
		errorLabel.setText(error == null ? null : "<html><p>" + error + "</p></html>");
	}

	private void startSearch() {
		statusTF.setText(null);
		DefaultMutableTreeNode invisibleSearchRoot = new DefaultMutableTreeNode();
		instanceSearchTree.setModel(new DefaultTreeModel(invisibleSearchRoot));

		instanceSearchNodes.clear();
		matchingObjects.clear();

		InstancePath sourcePath = new InstancePath(root, "root", null);

		Predicate<Object> targetFilter;
		SearchSettings settings;
		try {
			targetFilter = getTargetFilter();
			settings = getSearchSettings();
		} catch (SettingsException e) {
			showError(e.getMessage());
			return;
		}
		showError(null);

		instancePathFinder.reset();
		CompletableFuture.runAsync(() -> instancePathFinder.search(sourcePath, targetFilter, settings));
		CompletableFuture.runAsync(this::updateDisplayWhileSearching);
	}

	private void stopSearch() {
		instancePathFinder.stop();
	}

	private void addInstancePath(InstancePath path) {
		if (instanceSearchNodes.containsKey(path)) {
			return;
		}
		InstancePath parentPath = path.getParentPath();
		DefaultTreeModel model = (DefaultTreeModel) instanceSearchTree.getModel();
		final DefaultMutableTreeNode parentNode;
		if (parentPath == null) {
			// root or class node
			parentNode = (DefaultMutableTreeNode) model.getRoot();
		} else {
			if (!instanceSearchNodes.containsKey(parentPath)) {
				addInstancePath(parentPath);
			}
			 parentNode = instanceSearchNodes.get(parentPath);
		}
		assert parentNode != null;
		SearchNode pathNode = new SearchNode(path, context);
		instanceSearchNodes.put(path, pathNode);
		model.insertNodeInto(pathNode, parentNode, model.getChildCount(parentNode));
		instanceSearchTree.expandPath(new TreePath(parentNode.getPath()));
	}

	private void onNoSearchOptionSelected() throws SettingsException {
		throw createNoSearchOptionSelectedException();
	}

	private SettingsException createNoSearchOptionSelectedException() {
		return new SettingsException("Neither option 'concrete instance' nor option 'all instances of class' is selected");
	}

	/*
	 * Listeners
	 */
	private void onPathDetected(InstancePath path) {
		SwingUtilities.invokeLater(() -> {
			matchingObjects.add(path.getLastNodeObject());
			addInstancePath(path);
		});
	}

	private void onTargetClassSpecified() {
		Class<?> targetClass;
		try {
			targetClass = getTargetClass();
		} catch (SettingsException e) {
			showError(e.getMessage());
			return;
		}
		targetFilterTF.setParameterTypes(targetClass);
		updateEnabilities();
	}

	private void onTargetFilterSpecified() {
		updateEnabilities();
	}

	private static class SearchTreeRenderer implements TreeCellRenderer
	{
		private final Set<Object>		matchingObjects;
		private final TreeCellRenderer	oldRenderer;

		SearchTreeRenderer(Set<Object> matchingObjects, TreeCellRenderer oldRenderer) {
			this.matchingObjects = matchingObjects;
			this.oldRenderer = oldRenderer;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Component rendererComponent = oldRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			if (!(value instanceof SearchNode)) {
				return rendererComponent;
			}
			Object object = ((SearchNode) value).getObject();
			boolean matches = matchingObjects.contains(object);
			if (matches) {
				rendererComponent.setBackground(Color.GREEN);
			}
			if (rendererComponent instanceof JComponent) {
				((JComponent) rendererComponent).setOpaque(matches);
			}
			return rendererComponent;
		}
	}
}
