package dd.kms.marple.gui.inspector.views.iterableview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.TypedObjectInfo;
import dd.kms.marple.common.UniformView;
import dd.kms.marple.gui.common.ResultPanel;
import dd.kms.marple.gui.inspector.views.iterableview.panels.ContextPanel;
import dd.kms.marple.gui.inspector.views.iterableview.panels.OperationPanel;
import dd.kms.marple.gui.inspector.views.iterableview.settings.*;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.*;

public class IterableView extends JPanel implements ObjectView
{
	public static final String	NAME	= "Iterables";

	private final Iterable<?>		iterable;
	private final TypeInfo			commonElementType;
	private final InspectionContext	inspectionContext;

	private final JPanel			contextPanel;
	private final OperationPanel operationPanel;
	private final ResultPanel resultPanel;

	public IterableView(TypedObjectInfo<? extends Iterable<?>> iterableInfo, InspectionContext context) {
		super(new GridBagLayout());

		this.iterable = iterableInfo.getObject();
		this.commonElementType = UniformView.getCommonElementType(iterableInfo);
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

}
