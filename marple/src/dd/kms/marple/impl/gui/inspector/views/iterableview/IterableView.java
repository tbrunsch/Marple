package dd.kms.marple.impl.gui.inspector.views.iterableview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.common.TypedObjectInfo;
import dd.kms.marple.impl.common.UniformView;
import dd.kms.marple.impl.gui.common.ResultPanel;
import dd.kms.marple.impl.gui.inspector.views.iterableview.panels.ContextPanel;
import dd.kms.marple.impl.gui.inspector.views.iterableview.panels.OperationPanel;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.*;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

public class IterableView extends JPanel implements ObjectView
{
	public static final String	NAME	= "Iterables";

	private final Iterable<?>		iterable;
	private final TypeInfo			commonElementType;
	private final InspectionContext	context;

	private final JPanel			contextPanel;
	private final OperationPanel	operationPanel;
	private final ResultPanel		resultPanel;

	public IterableView(TypedObjectInfo<? extends Iterable<?>> iterableInfo, InspectionContext context) {
		super(new GridBagLayout());

		this.iterable = iterableInfo.getObject();
		this.commonElementType = UniformView.getCommonElementType(iterableInfo);
		this.context = context;

		this.contextPanel = new ContextPanel(iterableInfo, commonElementType, context);
		this.operationPanel = new OperationPanel(commonElementType, context);
		this.resultPanel = new ResultPanel(context);

		setName(NAME);

		int yPos = 0;
		add(contextPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(operationPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(resultPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		operationPanel.setExceptionConsumer(resultPanel::displayException);
		operationPanel.setAction(this::onRunOperation);
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
				return new FilterOperationExecutor(iterable, commonElementType, (FilterSettings) settings, context);
			case MAP:
				return new MapOperationExecutor(iterable, commonElementType, (MapSettings) settings, context);
			case FOR_EACH:
				return new ForEachOperationExecutor(iterable, commonElementType, (ForEachSettings) settings, context);
			case COLLECT:
				return new CollectOperationExecutor(iterable, commonElementType, (CollectSettings) settings, context);
			case TO_MAP:
				return new ToMapOperationExecutor(iterable, commonElementType, (ToMapSettings) settings, context);
			default:
				throw new IllegalStateException("Unsupported operation: " + operation);
		}
	}

}
