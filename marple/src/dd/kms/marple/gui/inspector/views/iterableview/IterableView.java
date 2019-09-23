package dd.kms.marple.gui.inspector.views.iterableview;

import com.google.common.collect.ImmutableMap;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.common.TypedObjectInfo;
import dd.kms.marple.gui.common.ExceptionFormatter;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.marple.gui.evaluator.textfields.CompiledExpressionInputTextField;
import dd.kms.marple.gui.evaluator.textfields.EvaluationTextFieldPanel;
import dd.kms.marple.gui.inspector.views.fieldview.FieldTree;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

import static java.awt.GridBagConstraints.*;

public class IterableView extends JPanel implements ObjectView
{
	private static final String	NAME			= "Iterables";

	private static final Insets	DEFAULT_INSETS	= new Insets(3, 3, 3, 3);

	private final Iterable<?>		iterable;
	private final TypeInfo			commonElementType;
	private final InspectionContext	inspectionContext;

	private final JPanel			contextPanel;
	private final OperationPanel	operationPanel;
	private final ResultPanel		resultPanel;

	public IterableView(TypedObjectInfo<? extends Iterable<?>> iterableInfo, InspectionContext context) {
		super(new GridBagLayout());

		this.iterable = iterableInfo.getObject();
		this.commonElementType = determineCommonElementType(iterableInfo);
		this.inspectionContext = context;

		this.contextPanel = new ContextPanel(iterableInfo, commonElementType, context);
		this.operationPanel = new OperationPanel(commonElementType, context);
		this.resultPanel = new ResultPanel(context);

		setName(NAME);

		int yPos = 0;
		add(contextPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(operationPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(resultPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		operationPanel.setExceptionConsumer(e -> resultPanel.displayException(operationPanel.getSettings().getExpression(), e));
		operationPanel.setAction(this::onRunOperation);
	}

	private TypeInfo determineCommonElementType(TypedObjectInfo<? extends Iterable<?>> iterableInfo) {
		TypeInfo iterableType = ReflectionUtils.getRuntimeTypeInfo(iterableInfo);
		TypeInfo iteratorResultTypeInfo = ReflectionUtils.getUniqueMethodInfo(iterableType, "iterator").getReturnType();
		TypeInfo declaredElementType = ReflectionUtils.getUniqueMethodInfo(iteratorResultTypeInfo, "next").getReturnType();
		Class<?> commonElementClass = ReflectionUtils.getCommonSuperClass(iterable);
		return ReflectionUtils.getRuntimeTypeInfo(declaredElementType, commonElementClass);
	}

	@Override
	public String getViewName() {
		return NAME;
	}

	@Override
	public Component getViewComponent() {
		return this;
	}

	@Override
	public Object getViewSettings() {
		return operationPanel.getSettings();
	}

	@Override
	public void applyViewSettings(Object settings) {
		if (settings instanceof OperationSettings) {
			operationPanel.applySettings((OperationSettings) settings);
		}
	}

	private void onRunOperation() {
		OperationSettings settings = operationPanel.getSettings();
		Operation operation = settings.getOperation();
		OperationResultType resultType = settings.getResultType();
		String expression = settings.getExpression();
		AbstractOperationExecutor executor;
		switch (operation) {
			case FILTER:
				executor = new FilterOperationExecutor(iterable, commonElementType, inspectionContext);
				break;
			case MAP:
				executor = new MapOperationExecutor(iterable, commonElementType, inspectionContext);
				break;
			case FOR_EACH:
				executor = new ForEachOperationExecutor(iterable, commonElementType, inspectionContext);
				break;
			default:
				throw new IllegalStateException("Unsupported operation: " + operation);
		}
		executor.setResultConsumer(resultPanel::displayResult);
		executor.setTextConsumer(resultPanel::displayText);
		try {
			executor.execute(expression, resultType);
		} catch (Exception e) {
			resultPanel.displayException(expression, e);
		}
	}

	private static class ContextPanel extends JPanel
	{
		private final JLabel		commonElementClassInfoLabel	= new JLabel("Element class:");
		private final JLabel		commonElementClassLabel		= new JLabel();

		private final JComponent	fieldTree;

		ContextPanel(TypedObjectInfo<? extends Iterable<?>> iterableInfo, TypeInfo commonElementType, InspectionContext context) {
			super(new GridBagLayout());

			setBorder(BorderFactory.createTitledBorder("Iterable"));

			fieldTree = new FieldTree(iterableInfo, true, context);
			fieldTree.setPreferredSize(new Dimension(fieldTree.getPreferredSize().width, 100));

			commonElementClassLabel.setText(commonElementType.toString());

			add(commonElementClassInfoLabel,	new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
			add(commonElementClassLabel,		new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

			add(fieldTree,						new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, WEST, BOTH, DEFAULT_INSETS, 0, 0));
		}
	}

	private static class OperationPanel extends JPanel
	{
		private final JLabel									operationTypeLabel		= new JLabel("Operation type:");
		private final JToggleButton								filterTB				= new JToggleButton();
		private final JToggleButton								mapTB					= new JToggleButton();
		private final JToggleButton								forEachTB				= new JToggleButton();
		private final ButtonGroup								operationButtonGroup	= new ButtonGroup();
		private final Map<Operation, JToggleButton>				operationToButton		= ImmutableMap.<Operation, JToggleButton>builder()
																							.put(Operation.FILTER,		filterTB)
																							.put(Operation.MAP,			mapTB)
																							.put(Operation.FOR_EACH,	forEachTB)
																							.build();

		private final JLabel									resultTypeLabel			= new JLabel("Result type:");
		private final JToggleButton								listTB					= new JToggleButton();
		private final JToggleButton								indexMapTB				= new JToggleButton();
		private final ButtonGroup								resultTypeButtonGroup	= new ButtonGroup();
		private final Map<OperationResultType, JToggleButton>	resultTypeToButton		= ImmutableMap.<OperationResultType, JToggleButton>builder()
																							.put(OperationResultType.LIST,		listTB)
																							.put(OperationResultType.INDEX_MAP,	indexMapTB)
																							.build();

		private final JLabel									expressionLabel			= new JLabel("Expression:");
		private final JPanel									expressionPanel;
		private final CompiledExpressionInputTextField			expressionTF;
		private final JLabel									expressionInfoLabel		= new JLabel("'this' always refers to the element currently processed");
		private final JButton									runButton				= new JButton("Run");

		OperationPanel(TypeInfo commonElementType, InspectionContext context) {
			super(new GridBagLayout());

			setBorder(BorderFactory.createTitledBorder("Operation"));

			expressionTF = new CompiledExpressionInputTextField(context);
			expressionPanel = new EvaluationTextFieldPanel(expressionTF, context);
			expressionTF.setThisType(commonElementType);

			initButtons(operationToButton, 	operationButtonGroup,	Operation.FILTER);
			initButtons(resultTypeToButton, resultTypeButtonGroup,	OperationResultType.LIST);

			int yPos = 0;

			int xPos = 0;
			add(operationTypeLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
			add(filterTB,				new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
			add(mapTB,					new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
			add(forEachTB,				new GridBagConstraints(xPos++, yPos++, 1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

			xPos = 0;
			add(resultTypeLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
			add(listTB,					new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
			add(indexMapTB,				new GridBagConstraints(xPos++, yPos++, 2, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));

			xPos = 0;
			add(expressionLabel,		new GridBagConstraints(xPos++, yPos,   1, 1, 0.0, 0.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
			add(expressionPanel,		new GridBagConstraints(xPos++, yPos,   3, 1, 0.0, 0.0, WEST, HORIZONTAL, DEFAULT_INSETS, 0, 0));
			xPos += 2;
			add(expressionInfoLabel,	new GridBagConstraints(xPos++, yPos,   1, 1, 1.0, 1.0, WEST, NONE, DEFAULT_INSETS, 0, 0));
			add(runButton,				new GridBagConstraints(xPos++, yPos++, 1, 1, 0.0, 0.0, EAST, NONE, DEFAULT_INSETS, 0, 0));

			for (JToggleButton button : operationToButton.values()) {
				button.addActionListener(e -> onOperationTypeChanged());
			}
		}

		void setExceptionConsumer(Consumer<Throwable> exceptionConsumer) {
			expressionTF.setExceptionConsumer(exceptionConsumer);
		}

		void setAction(Runnable action) {
			expressionTF.addActionListener(e -> action.run());
			runButton.addActionListener(e -> action.run());
		}

		private <T> void initButtons(Map<T, JToggleButton> valueToButtonMap, ButtonGroup buttonGroup, T initialValue) {
			for (T value : valueToButtonMap.keySet()) {
				JToggleButton button = valueToButtonMap.get(value);
				button.setText(value.toString());
				buttonGroup.add(button);
			}
			setSelectedValue(valueToButtonMap, initialValue);
		}

		private <T> T getSelectedValue(Map<T, JToggleButton> valueToButtonMap) {
			for (T value : valueToButtonMap.keySet()) {
				JToggleButton button = valueToButtonMap.get(value);
				if (button.isSelected()) {
					return value;
				}
			}
			return null;
		}

		private <T> void setSelectedValue(Map<T, JToggleButton> valueToButtonMap, T value) {
			valueToButtonMap.get(value).setSelected(true);
		}

		OperationSettings getSettings() {
			Operation operation = getSelectedValue(operationToButton);
			OperationResultType resultType = getSelectedValue(resultTypeToButton);
			String expression = expressionTF.getText();
			return new OperationSettings(operation, resultType, expression);
		}

		void applySettings(OperationSettings settings) {
			setSelectedValue(operationToButton, settings.getOperation());
			setSelectedValue(resultTypeToButton, settings.getResultType());
			expressionTF.setText(settings.getExpression());
		}

		private void onOperationTypeChanged() {
			Operation operation = getSelectedValue(operationToButton);
			boolean enableResultType = operation == Operation.FILTER;
			resultTypeLabel.setEnabled(enableResultType);
			listTB.setEnabled(enableResultType);
			indexMapTB.setEnabled(enableResultType);
		}
	}

	private static class ResultPanel extends JPanel
	{
		private final InspectionContext	context;

		ResultPanel(InspectionContext context) {
			super(new GridBagLayout());
			this.context = context;

			setBorder(BorderFactory.createTitledBorder("Result"));
		}

		void displayException(String expression, Throwable t) {
			String error = ExceptionFormatter.formatParseException(expression, t);
			JLabel exceptionLabel = new JLabel(error);
			CodeCompletionDecorators.configureExceptionComponent(exceptionLabel);
			displayComponent(exceptionLabel);
		}

		void displayResult(Object result) {
			ObjectInfo resultInfo = InfoProvider.createObjectInfo(result);
			FieldTree fieldTree = new FieldTree(resultInfo, false, context);
			displayComponent(fieldTree);
		}

		void displayText(String text) {
			JLabel label = new JLabel(text);
			displayComponent(label);
		}

		private void displayComponent(JComponent component) {
			removeAll();
			add(component,	new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, NORTH, BOTH, new Insets(3, 3, 3, 3), 0, 0));
			revalidate();
			repaint();
		}
	}
}
