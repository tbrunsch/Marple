package dd.kms.marple.settings.visual;

import dd.kms.marple.InspectionContext;
import dd.kms.marple.common.ReflectionUtils;
import dd.kms.marple.common.UniformView;
import dd.kms.marple.gui.inspector.views.fieldview.FieldView;
import dd.kms.marple.gui.inspector.views.iterableview.IterableView;
import dd.kms.marple.gui.inspector.views.methodview.MethodView;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.function.Function;

public class VisualSettingsUtils
{
	public static VisualSettingsBuilder createBuilder() {
		return new VisualSettingsBuilderImpl();
	}

	public static void addDefaultDisplayTextFunctions(VisualSettingsBuilder builder) {
		builder
			.displayText(String.class,			s -> '"' + s + '"')
			.displayText(char.class,			c -> "'" + c + "'")
			.displayText(Character.class,		c -> "'" + c + "'")
			.displayText(Object.class, 			object -> getObjectDisplayText(object))
			.displayText(Frame.class, 			frame -> getDisplayText(frame, Frame::getTitle))
			.displayText(AbstractButton.class,	button -> getDisplayText(button, AbstractButton::getText))
			.displayText(JLabel.class,			label -> getDisplayText(label, JLabel::getText))
			.displayText(JTextComponent.class, 	textComponent -> getDisplayText(textComponent, JTextComponent::getText));
	}

	public static void addDefaultViews(VisualSettingsBuilder builder) {
		builder
			.objectView(Object.class, FieldView::new)
			.objectView(Object.class, MethodView::new)
			.objectView(Object.class, VisualSettingsUtils::createIterableView);
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
			+ (text == null ? "null" : text)
			+ ")";
	}

	private static ObjectView createIterableView(Object object, InspectionContext context) {
		return UniformView.canViewAsIterable(object)
				? new IterableView(UniformView.asIterable(object), context)
				: null;
	}
}
