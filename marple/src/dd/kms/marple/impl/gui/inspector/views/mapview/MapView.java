package dd.kms.marple.impl.gui.inspector.views.mapview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.common.ReflectionUtils;
import dd.kms.marple.impl.gui.common.ResultPanel;
import dd.kms.marple.impl.gui.inspector.views.mapview.panels.ContextPanel;
import dd.kms.marple.impl.gui.inspector.views.mapview.panels.OperationPanel;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.FilterSettings;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.MapSettings;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.Operation;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.OperationSettings;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

public class MapView extends JPanel implements ObjectView
{
	public static final String	NAME	= "Maps";

	private Map<?, ?>				map;
	private final Class<?>			commonKeyType;
	private final Class<?>			commonValueType;
	private final InspectionContext	context;

	private final ContextPanel		contextPanel;
	private final OperationPanel	operationPanel;
	private final ResultPanel		resultPanel;

	public MapView(Map<?, ?> map, InspectionContext context) {
		super(new GridBagLayout());

		this.map = map;

		this.commonKeyType = ReflectionUtils.getCommonSuperClass(map.keySet());
		this.commonValueType = ReflectionUtils.getCommonSuperClass(map.values());

		this.context = context;

		this.contextPanel = new ContextPanel(map, commonKeyType, commonValueType, context);
		this.operationPanel = new OperationPanel(commonKeyType, commonValueType, context);
		this.resultPanel = new ResultPanel(context);

		setName(NAME);

		int yPos = 0;
		add(contextPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(operationPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 0.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));
		add(resultPanel,	new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0, CENTER, BOTH, DEFAULT_INSETS, 0, 0));

		operationPanel.setMapType(map.getClass());
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
				return new FilterOperationExecutor(map, commonKeyType, commonValueType, (FilterSettings) settings, context);
			case MAP:
				return new MapOperationExecutor(map, commonKeyType, commonValueType, (MapSettings) settings, context);
			default:
				throw new IllegalStateException("Unsupported operation: " + operation);
		}
	}

	@Override
	public void dispose() {
		map = null;
		contextPanel.dispose();
		resultPanel.dispose();
	}
}
