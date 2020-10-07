package dd.kms.marple.api.settings.visual;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Map;
import java.util.function.Function;

public class VisualSettingsUtils
{
	public static void addDefaultDisplayTextFunctions(VisualSettingsBuilder builder) {
		builder
			.displayText(String.class,			s -> '"' + s + '"')
			.displayText(char.class,			c -> "'" + c + "'")
			.displayText(Character.class,		c -> "'" + c + "'")
			.displayText(Object.class, 			o -> getObjectDisplayText(o))
			.displayText(Frame.class, 			frame -> getDisplayText(frame, Frame::getTitle))
			.displayText(AbstractButton.class,	button -> getDisplayText(button, AbstractButton::getText))
			.displayText(JLabel.class,			label -> getDisplayText(label, JLabel::getText))
			.displayText(JTextComponent.class, 	textComponent -> getDisplayText(textComponent, JTextComponent::getText));
	}

	public static void addDefaultViews(VisualSettingsBuilder builder) {
		builder
			.objectView(Object.class, dd.kms.marple.impl.gui.inspector.views.fieldview.FieldView::new)
			.objectView(Object.class, dd.kms.marple.impl.gui.inspector.views.methodview.MethodView::new)
			.objectView(Object.class, VisualSettingsUtils::createIterableView)
			.objectView(Object.class, VisualSettingsUtils::createMapView);
	}

	private static String getObjectHashText(Object object) {
		return object.getClass().getSimpleName() + "@" + System.identityHashCode(object);
	}

	private static String getObjectDisplayText(Object object) {
		return object != null && !dd.kms.marple.impl.common.ReflectionUtils.isObjectInspectable(object)
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
		return dd.kms.marple.impl.common.UniformView.canViewAsIterable(objectInfo)
				? new dd.kms.marple.impl.gui.inspector.views.iterableview.IterableView(dd.kms.marple.impl.common.UniformView.asIterable(objectInfo), context)
				: null;
	}

	private static ObjectView createMapView(ObjectInfo objectInfo, InspectionContext context) {
		return objectInfo.getObject() instanceof Map
				? new dd.kms.marple.impl.gui.inspector.views.mapview.MapView(new dd.kms.marple.impl.common.TypedObjectInfo<>(objectInfo), context)
				: null;
	}
}