package dd.kms.marple.settings.visual;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.common.TypedObjectInfo;
import dd.kms.marple.common.UniformView;
import dd.kms.marple.gui.inspector.views.fieldview.FieldView;
import dd.kms.marple.gui.inspector.views.iterableview.IterableView;
import dd.kms.marple.gui.inspector.views.mapview.MapView;
import dd.kms.marple.gui.inspector.views.methodview.MethodView;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Map;
import java.util.function.Function;

public class VisualSettingsUtils
{
	public static VisualSettingsBuilder createBuilder() {
		return new VisualSettingsBuilderImpl();
	}

	public static void addDefaultDisplayTextFunctions(VisualSettingsBuilder builder) {
		builder
			.displayText(String.class,			s -> '"' + s.getObject() + '"')
			.displayText(char.class,			c -> "'" + c.getObject() + "'")
			.displayText(Character.class,		c -> "'" + c.getObject() + "'")
			.displayText(Object.class, 			objectInfo -> getObjectDisplayText(objectInfo.getObject()))
			.displayText(Frame.class, 			frameInfo -> getDisplayText(frameInfo.getObject(), Frame::getTitle))
			.displayText(AbstractButton.class,	buttonInfo -> getDisplayText(buttonInfo.getObject(), AbstractButton::getText))
			.displayText(JLabel.class,			labelInfo -> getDisplayText(labelInfo.getObject(), JLabel::getText))
			.displayText(JTextComponent.class, 	textComponentInfo -> getDisplayText(textComponentInfo.getObject(), JTextComponent::getText));
	}

	public static void addDefaultViews(VisualSettingsBuilder builder) {
		builder
			.objectView(Object.class, FieldView::new)
			.objectView(Object.class, MethodView::new)
			.objectView(Object.class, VisualSettingsUtils::createIterableView)
			.objectView(Object.class, VisualSettingsUtils::createMapView);
	}

	private static String getObjectHashText(Object object) {
		return object.getClass().getSimpleName() + "@" + System.identityHashCode(object);
	}

	private static String getObjectDisplayText(Object object) {
		return object != null && !ReflectionUtils.isObjectInspectable(object)
				? object.toString()
				: getDisplayText(object, Object::toString);
	}

	private static <T> String getDisplayText(T object, Function<T, String> textExtractionFunction) {
		String text = textExtractionFunction.apply(object);
		return getObjectHashText(object)
			+ " ("
			+ (	text == null	? "<null>" :
				text.isEmpty()	? "<empty>"
								: '"' + text + "'")
			+ ")";
	}

	private static ObjectView createIterableView(ObjectInfo objectInfo, InspectionContext context) {
		return UniformView.canViewAsIterable(objectInfo)
				? new IterableView(UniformView.asIterable(objectInfo), context)
				: null;
	}

	private static ObjectView createMapView(ObjectInfo objectInfo, InspectionContext context) {
		return objectInfo.getObject() instanceof Map
				? new MapView(new TypedObjectInfo<>(objectInfo), context)
				: null;
	}
}
