package dd.kms.marple.gui.search;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.gui.evaluator.textfields.ClassInputTextField;
import dd.kms.marple.gui.evaluator.textfields.CompiledExpressionInputTextField;
import dd.kms.marple.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.instancesearch.InstancePath;
import dd.kms.marple.instancesearch.InstancePathFinder;
import dd.kms.marple.instancesearch.settings.SearchSettings;
import dd.kms.marple.instancesearch.settings.SearchSettingsBuilders;
import dd.kms.zenodot.CompiledExpression;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
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
	private final CompiledExpressionInputTextField	targetFilterTF;
	private final JPanel							targetFilterPanel;
	private final JLabel							optionsLabel				= new JLabel("Options:");
	private final JCheckBox							onlyNonStaticFieldsCB		= new JCheckBox("non-static fields only");
	private final JCheckBox							onlyPureFieldsCB			= new JCheckBox("pure fields only");
	private final JCheckBox							limitSearchDepthCB			= new JCheckBox("limit search depth:");
	private final JTextField						maxSearchDepthTF			= new JTextField();

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

	private final InspectionContext					inspectionContext;

	private Object									root;
	private Object									target;

	private final InstancePathFinder				instancePathFinder;

	private final Map<InstancePath, SearchNode>		instanceSearchNodes			= new HashMap<>();

	InstanceSearchPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;

		targetClassTF = new ClassInputTextField(inspectionContext);
		targetClassTF.addInputVerifier();
		targetClassPanel = new EvaluationTextFieldPanel(targetClassTF, inspectionContext);
		targetFilterTF = new CompiledExpressionInputTextField(inspectionContext);
		targetFilterTF.addInputVerifier();
		targetFilterPanel = new EvaluationTextFieldPanel(targetFilterTF, inspectionContext);

		instancePathFinder = new InstancePathFinder(this::onPathDetected);

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

		// Hack: We need this panel to prevent the textfield from collapsing
		JPanel maxSearchDepthPanel = new JPanel(new GridBagLayout());
		maxSearchDepthPanel.add(maxSearchDepthTF,			new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, WEST, NONE, new Insets(0, 0, 0, 0), 0, 0));

		configurationPanel.add(limitSearchDepthCB,			new GridBagConstraints(1, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(maxSearchDepthPanel,			new GridBagConstraints(2, yPos++, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

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

		ActionProviderListeners.addMouseListeners(instanceSearchTree);
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
		limitSearchDepthCB.addItemListener(e -> maxSearchDepthTF.setEnabled(limitSearchDepthCB.isSelected()));
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

	void setInspectionContext(InspectionContext inspectionContext) {
		instancePathFinder.setInspectionContext(inspectionContext);
	}

	private void updateEnabilities() {
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

	private Optional<SearchSettings> getSearchSettings() {
		String maxSearchDepthText = maxSearchDepthTF.getText();
		int maxSearchDepth = 0;
		try {
			maxSearchDepth = Integer.parseInt(maxSearchDepthText);
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
		SearchSettings settings = SearchSettingsBuilders.create()
			.extendPathsBeyondAcceptedInstances(targetAllInstancesRB.isSelected())
			.searchOnlyNonStaticFields(onlyNonStaticFieldsCB.isSelected())
			.searchOnlyPureFields(onlyPureFieldsCB.isSelected())
			.limitSearchDepth(limitSearchDepthCB.isSelected())
			.maximumSearchDepth(maxSearchDepth)
			.addClassesToExclude(getClass(), ObjectInfo.class)
			.addExclusionFilter(clazz -> "sun.awt.AppContext".equals(clazz.getName()))
			.build();
		return Optional.of(settings);
	}

	private Optional<Class<?>> getTargetClass() {
		if (targetAllInstancesRB.isSelected()) {
			try {
				ClassInfo targetClassInfo = targetClassTF.evaluateText();
				return Optional.of(Class.forName(targetClassInfo.getNormalizedName()));
			} catch (Throwable t) {
				/* fall through until end */
			}
		} else if (targetConcreteInstanceRB.isSelected() && target != null) {
			return Optional.of(target.getClass());
		}
		return Optional.empty();
	}

	private Optional<Predicate<Object>> getTargetFilter() {
		if (targetAllInstancesRB.isSelected()) {
			Optional<Class<?>> optionalTargetClass = getTargetClass();
			if (!optionalTargetClass.isPresent()) {
				return Optional.empty();
			}
			Class<?> targetClass = optionalTargetClass.get();
			if (targetFilterCB.isSelected()) {
				try {
					CompiledExpression compiledFilter = targetFilterTF.evaluateText();
					return Optional.of(o -> targetClass.isInstance(o) && applyFilter(compiledFilter, o));
				} catch (Throwable t) {
					/* fall through until end */
				}
			} else {
				return Optional.of(o -> targetClass.isInstance(o));
			}
		} else if (targetConcreteInstanceRB.isSelected()) {
			return Optional.of(o -> o == target);
		}
		return Optional.empty();
	}

	private boolean applyFilter(CompiledExpression filter, Object o) {
		try {
			return Boolean.TRUE.equals(filter.evaluate(InfoProvider.createObjectInfo(o)));
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isRequiredInputSpecified() {
		if (root == null) {
			return false;
		}
		if (!getSearchSettings().isPresent()) {
			return false;
		}
		if (targetAllInstancesRB.isSelected()) {
			Optional<Class<?>> targetClass = getTargetClass();
			return targetClass.isPresent()
				&& (!targetFilterCB.isSelected() || getTargetFilter().isPresent());
		} else if (targetConcreteInstanceRB.isSelected()) {
			return target != null;
		}
		return false;
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
		return object == null ? "<none>" : inspectionContext.getDisplayText(InfoProvider.createObjectInfo(object));
	}

	private void updateStatusText() {
		String statusText = instancePathFinder.getStatusText();
		statusTF.setText(statusText);
		statusTF.revalidate();
		statusTF.repaint();
	}

	private void showError(String error) {
		JOptionPane.showMessageDialog(this, error, "Parse Error", JOptionPane.ERROR_MESSAGE);
	}

	private void startSearch() {
		statusTF.setText(null);
		instanceSearchTree.setModel(new DefaultTreeModel(null));
		instanceSearchNodes.clear();

		InstancePath sourcePath = new InstancePath(root, "root", null);

		Optional<Class<?>> optionalTargetClass = getTargetClass();
		if (!optionalTargetClass.isPresent()) {
			showError("Could not parse target class.");
			return;
		}

		Optional<Predicate<Object>> optionalTargetFilter = getTargetFilter();
		if (!optionalTargetFilter.isPresent()) {
			showError("Could not parse target filter.");
			return;
		}
		Predicate<Object> targetFilter = optionalTargetFilter.get();

		Optional<SearchSettings> settings = getSearchSettings();
		if (!settings.isPresent()) {
			showError("Invalid search settings.");
			return;
		}

		instancePathFinder.reset();
		new Thread(() -> instancePathFinder.search(sourcePath, targetFilter, settings.get())).start();
		new Thread(this::updateDisplayWhileSearching).start();
	}

	private void stopSearch() {
		instancePathFinder.stop();
	}

	private void addInstancePath(InstancePath path) {
		InstancePath parentPath = path.getParentPath();
		if (parentPath == null) {
			// root node
			if (!instanceSearchNodes.containsKey(path)) {
				SearchNode pathNode = new SearchNode(path, inspectionContext);
				instanceSearchNodes.put(path, pathNode);
				instanceSearchTree.setModel(new DefaultTreeModel(pathNode));
			}
		} else {
			if (!instanceSearchNodes.containsKey(parentPath)) {
				addInstancePath(parentPath);
			}
			SearchNode parentNode = instanceSearchNodes.get(parentPath);
			assert parentNode != null;
			SearchNode pathNode = new SearchNode(path, inspectionContext);
			instanceSearchNodes.put(path, pathNode);
			DefaultTreeModel model = (DefaultTreeModel) instanceSearchTree.getModel();
			model.insertNodeInto(pathNode, parentNode, model.getChildCount(parentNode));
			instanceSearchTree.expandPath(new TreePath(parentNode.getPath()));
		}
	}

	/*
	 * Listeners
	 */
	private void onPathDetected(InstancePath path) {
		SwingUtilities.invokeLater(() -> addInstancePath(path));
	}

	private void onTargetClassSpecified() {
		Optional<Class<?>> optionalTargetClass = getTargetClass();
		if (optionalTargetClass.isPresent()) {
			Class<?> targetClass = optionalTargetClass.get();
			targetFilterTF.setThisType(InfoProvider.createTypeInfo(targetClass));
		}
		updateEnabilities();
	}

	private void onTargetFilterSpecified() {
		updateEnabilities();
	}
}
