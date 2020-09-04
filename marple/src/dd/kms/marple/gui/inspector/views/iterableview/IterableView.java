package dd.kms.marple.gui.inspector.views.iterableview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.common.TypedObjectInfo;
import dd.kms.marple.gui.common.ExceptionFormatter;
import dd.kms.marple.gui.evaluator.completion.CodeCompletionDecorators;
import dd.kms.marple.gui.inspector.views.fieldview.FieldTree;
import dd.kms.marple.gui.inspector.views.iterableview.panels.OperationPanel;
import dd.kms.marple.gui.inspector.views.iterableview.settings.*;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class IterableView extends JPanel implements ObjectView
{
	private static final String	NAME	= "Iterables";

	private final Iterable<?>		iterable;
	private final TypeInfo			commonElementType;
	private final InspectionContext	inspectionContext;

	private final JPanel			contextPanel;
	private final OperationPanel operationPanel;
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

		operationPanel.setExceptionConsumer(e -> resultPanel.displayException(e));
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
			operationPanel.setSettings((OperationSettings) settings);
		}
	}

	private void onRunOperation() {
		OperationSettings settings = operationPanel.getSettings();
		AbstractOperationExecutor<?> executor = createExecutor(settings);
		executor.setResultConsumer(resultPanel::displayResult);
		executor.setTextConsumer(resultPanel::displayText);
		try {
			executor.execute();
		} catch (Exception e) {
			resultPanel.displayException(e);
		}
	}

	private AbstractOperationExecutor<?> createExecutor(OperationSettings settings) {
		Operation operation = settings.getOperation();
		switch (operation) {
			case FILTER:
				return new FilterOperationExecutor(iterable, commonElementType, (FilterSettings) settings, inspectionContext);
			case MAP:
				return new MapOperationExecutor(iterable, commonElementType, (MapSettings) settings, inspectionContext);
			case FOR_EACH:
				return new ForEachOperationExecutor(iterable, commonElementType, (ForEachSettings) settings, inspectionContext);
			case COLLECT:
				return new CollectOperationExecutor(iterable, commonElementType, (CollectSettings) settings, inspectionContext);
			case TO_MAP:
				return new ToMapOperationExecutor(iterable, commonElementType, (ToMapSettings) settings, inspectionContext);
			default:
				throw new IllegalStateException("Unsupported operation: " + operation);
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

	private static class ResultPanel extends JPanel
	{
		private final InspectionContext	context;

		ResultPanel(InspectionContext context) {
			super(new GridBagLayout());
			this.context = context;

			setBorder(BorderFactory.createTitledBorder("Result"));
		}

		void displayException(@Nullable Throwable t) {
			String error = ExceptionFormatter.formatParseException(t);
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
