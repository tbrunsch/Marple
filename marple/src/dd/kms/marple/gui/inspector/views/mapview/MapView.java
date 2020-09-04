package dd.kms.marple.gui.inspector.views.mapview;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.common.TypedObjectInfo;
import dd.kms.marple.common.UniformView;
import dd.kms.marple.gui.common.ResultPanel;
import dd.kms.marple.gui.inspector.views.mapview.panels.ContextPanel;
import dd.kms.marple.gui.inspector.views.mapview.panels.OperationPanel;
import dd.kms.marple.gui.inspector.views.mapview.settings.FilterSettings;
import dd.kms.marple.gui.inspector.views.mapview.settings.MapSettings;
import dd.kms.marple.gui.inspector.views.mapview.settings.Operation;
import dd.kms.marple.gui.inspector.views.mapview.settings.OperationSettings;
import dd.kms.marple.settings.visual.ObjectView;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static dd.kms.marple.gui.common.GuiCommons.DEFAULT_INSETS;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

public class MapView extends JPanel implements ObjectView
{
	public static final String	NAME	= "Maps";

	private final Map<?, ?>			map;
	private final TypeInfo			commonKeyType;
	private final TypeInfo			commonValueType;
	private final InspectionContext	inspectionContext;

	private final JPanel			contextPanel;
	private final OperationPanel	operationPanel;
	private final ResultPanel		resultPanel;

	public MapView(TypedObjectInfo<? extends Map<?,?>> mapInfo, InspectionContext context) {
		super(new GridBagLayout());

		this.map = mapInfo.getObject();

		TypeInfo mapType = ReflectionUtils.getRuntimeTypeInfo(mapInfo);
		TypeInfo keysTypeInfo = ReflectionUtils.getUniqueMethodInfo(mapType, "keySet").getReturnType();
		TypeInfo valuesTypeInfo = ReflectionUtils.getUniqueMethodInfo(mapType, "values").getReturnType();

		Set<?> keys = map.keySet();
		Collection<?> values = map.values();

		TypedObjectInfo<Set<?>> keysInfo = new TypedObjectInfo<>(InfoProvider.createObjectInfo(keys, keysTypeInfo));
		TypedObjectInfo<Collection<?>> valuesInfo = new TypedObjectInfo<>(InfoProvider.createObjectInfo(values, valuesTypeInfo));

		this.commonKeyType = UniformView.getCommonElementType(keysInfo);
		this.commonValueType = UniformView.getCommonElementType(valuesInfo);

		this.inspectionContext = context;

		this.contextPanel = new ContextPanel(mapInfo, commonKeyType, commonValueType, context);
		this.operationPanel = new OperationPanel(commonKeyType, commonValueType, context);
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
				return new FilterOperationExecutor(map, commonKeyType, commonValueType, (FilterSettings) settings, inspectionContext);
			case MAP:
				return new MapOperationExecutor(map, commonKeyType, commonValueType, (MapSettings) settings, inspectionContext);
			default:
				throw new IllegalStateException("Unsupported operation: " + operation);
		}
	}
}
