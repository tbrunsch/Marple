package dd.kms.marple.gui.search;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeMouseListener;
import dd.kms.marple.gui.actionprovidertree.ActionProviderTreeMouseMotionListener;
import dd.kms.marple.gui.evaluator.textfields.ClassInputTextField;
import dd.kms.marple.instancesearch.InstancePath;
import dd.kms.marple.instancesearch.InstancePathFinder;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

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
import java.util.function.Predicate;

import static java.awt.GridBagConstraints.*;

class InstanceSearchPanel extends JPanel
{
	private static final Insets	DEFAULT_INSETS	= new Insets(5, 5, 5, 5);

	private final JPanel				configurationPanel			= new JPanel(new GridBagLayout());
	private final JLabel				rootLabel					= new JLabel("Root of search:");
	private final JLabel				rootValueLabel				= new JLabel("---");
	private final JLabel				targetLabel					= new JLabel("Instances to find:");
	private final JRadioButton			targetAllInstancesRB		= new JRadioButton("all instances of class:");
	private final ClassInputTextField	targetClassTF;
	private final JRadioButton			targetConcreteInstanceRB	= new JRadioButton("concrete instance:");
	private final JLabel				targetValueLabel			= new JLabel("---");
	private final ButtonGroup			targetButtonGroup			= new ButtonGroup();

	private final JPanel				resultPanel					= new JPanel(new GridBagLayout());
	private final JLabel				statusLabel					= new JLabel("Status:");
	private final JTextField			statusTF					= new JTextField();
	private final JTree					instanceSearchTree			= new JTree((TreeNode) null);
	private final JScrollPane			instanceSearchScrollPane	= new JScrollPane(instanceSearchTree);
	private final JLabel				fullPathLabel				= new JLabel();

	private final JPanel				controlPanel				= new JPanel(new GridBagLayout());
	private final JButton				searchButton				= new JButton("Find");
	private final JButton				stopSearchButton			= new JButton("Stop");

	private final InspectionContext		inspectionContext;

	private Object						root;
	private Object						target;

	private final InstancePathFinder	instancePathFinder;

	private final Map<InstancePath, SearchNode>	instanceSearchNodes	= new HashMap<>();

	InstanceSearchPanel(InspectionContext inspectionContext) {
		super(new GridBagLayout());

		this.inspectionContext = inspectionContext;

		targetClassTF = new ClassInputTextField(classInfo -> {}, e -> {}, inspectionContext);
		targetClassTF.addInputVerifier();

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

		configurationPanel.add(rootLabel,					new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(rootValueLabel,				new GridBagConstraints(1, 0, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		configurationPanel.add(targetLabel,					new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(targetConcreteInstanceRB,	new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(targetValueLabel,			new GridBagConstraints(2, 1, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		configurationPanel.add(targetAllInstancesRB,		new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		configurationPanel.add(targetClassTF,				new GridBagConstraints(2, 2, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		targetButtonGroup.add(targetAllInstancesRB);
		targetButtonGroup.add(targetConcreteInstanceRB);

		targetConcreteInstanceRB.setSelected(true);
	}

	private void setupResultPanel() {
		resultPanel.setBorder(new TitledBorder("Search Result"));

		resultPanel.add(statusLabel,				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
		resultPanel.add(statusTF,					new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
		resultPanel.add(instanceSearchScrollPane,	new GridBagConstraints(0, 1, REMAINDER, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		resultPanel.add(fullPathLabel,				new GridBagConstraints(0, 2, REMAINDER, 1, 1.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));

		instanceSearchTree.addMouseListener(new ActionProviderTreeMouseListener());
		instanceSearchTree.addMouseMotionListener(new ActionProviderTreeMouseMotionListener());
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

	void setInspectionContext(InspectionContext inspectionContext) {
		instancePathFinder.setInspectionContext(inspectionContext);
	}

	private void updateEnabilities() {
		InstancePathFinder.ProcessingState processingState = instancePathFinder.getProcessingState();
		boolean searching = processingState != InstancePathFinder.ProcessingState.NOT_RUNNING && processingState != InstancePathFinder.ProcessingState.FINISHED;
		searchButton.setEnabled(!searching && isRequiredInputSpecified());
		stopSearchButton.setEnabled(!searchButton.isEnabled());
	}

	private boolean isRequiredInputSpecified() {
		if (root == null) {
			return false;
		}
		if (targetAllInstancesRB.isSelected()) {
			try {
				targetClassTF.evaluateText();
				return true;
			} catch (ParseException e) {
				/* happens if the class name cannot be parsed */
				return false;
			}
		}
		if (targetConcreteInstanceRB.isSelected()) {
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
		return object == null ? "<none>" : inspectionContext.getDisplayText(object);
	}

	private void updateStatusText() {
		String statusText = instancePathFinder.getStatusText();
		statusTF.setText(statusText);
		statusTF.revalidate();
		statusTF.repaint();
	}

	private void startSearch() {
		statusTF.setText(null);
		instanceSearchTree.setModel(new DefaultTreeModel(null));
		instanceSearchNodes.clear();

		InstancePath sourcePath = new InstancePath(root, "root", null);

		final Class<?> targetClass;
		final Predicate<Object> targetFilter;
		if (targetAllInstancesRB.isSelected()) {
			try {
				ClassInfo targetClassInfo = targetClassTF.evaluateText();
				targetClass = Class.forName(targetClassInfo.getNormalizedName());
			} catch (Throwable t) {
				String message = "Invalid target class:\n\n" + t.getMessage();
				JOptionPane.showMessageDialog(this, message, "Parse Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			targetFilter = targetClass::isInstance;
		} else if (targetConcreteInstanceRB.isSelected()) {
			targetClass = target.getClass();
			targetFilter = o -> o == target;
		} else {
			throw new IllegalStateException("None of the supported search options is selected");
		}

		instancePathFinder.reset();
		new Thread(() -> instancePathFinder.search(sourcePath, targetClass, targetFilter)).start();
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
}
