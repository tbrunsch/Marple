package dd.kms.marple.impl.gui.inspector.views.iterableview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.gui.common.ResultPanel;
import dd.kms.marple.impl.gui.inspector.views.iterableview.panels.ContextPanel;
import dd.kms.marple.impl.gui.inspector.views.iterableview.panels.OperationPanel;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.*;

import javax.swing.*;
import java.awt.*;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

public class IterableView extends JPanel implements ObjectView
{
	public static final String	NAME	= "Iterables";

	private Object					object;
	private Iterable<?>				iterableView;
	private final Class<?>			commonElementType;
	private final InspectionContext	context;

	private final ContextPanel		contextPanel;
	private final OperationPanel	operationPanel;
	private final ResultPanel		resultPanel;

	public IterableView(Object object, Iterable<?> iterableView, InspectionContext context) {
		super(new GridBagLayout());

		this.object = object;
		this.iterableView = iterableView;
		this.commonElementType = ReflectionUtils.getCommonSuperClass(this.iterableView);
		this.context = context;

		this.contextPanel = new ContextPanel(iterableView, commonElementType, context);
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
	public void applyViewSettings(Object settings, ViewSettingsOrigin origin) {
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
				return new FilterOperationExecutor(object, iterableView, commonElementType, (FilterSettings) settings, context);
			case MAP:
				return new MapOperationExecutor(object, iterableView, commonElementType, (MapSettings) settings, context);
			case FOR_EACH:
				return new ForEachOperationExecutor(object, iterableView, commonElementType, (ForEachSettings) settings, context);
			case COLLECT:
				return new CollectOperationExecutor(object, iterableView, commonElementType, (CollectSettings) settings, context);
			case TO_MAP:
				return new ToMapOperationExecutor(object, iterableView, commonElementType, (ToMapSettings) settings, context);
			case COUNT:
				return new CountOperationExecutor(object, iterableView, commonElementType, (CountSettings) settings, context);
			case GROUP:
				return new GroupOperationExecutor(object, iterableView, commonElementType, (GroupSettings) settings, context);
			default:
				throw new IllegalStateException("Unsupported operation: " + operation);
		}
	}

	@Override
	public void dispose() {
		object = null;
		iterableView = null;
		contextPanel.dispose();
		resultPanel.dispose();
	}
}
