package dd.kms.marple.gui;

import dd.kms.marple.gui.inspector.views.FieldView;
import dd.kms.marple.gui.inspector.views.MethodView;

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
			.displayText(Component.class, 		VisualSettingsUtils::getComponentDefaultDisplayText)
			.displayText(Frame.class, 			frame -> getComponentDisplayText(frame, Frame::getTitle))
			.displayText(AbstractButton.class,	button -> getComponentDisplayText(button, AbstractButton::getText))
			.displayText(JLabel.class,			label -> getComponentDisplayText(label, JLabel::getText))
			.displayText(JTextComponent.class, 	textComponent -> getComponentDisplayText(textComponent, JTextComponent::getText))
			;
		// TODO: Add special functions for special objects
	}

	public static void addDefaultViews(VisualSettingsBuilder builder) {
		builder
			.objectView(Object.class, FieldView::new)
			.objectView(Object.class, MethodView::new);
	}

	private static String getComponentDefaultDisplayText(Component component) {
		return component.getClass().getSimpleName() + "@" + System.identityHashCode(component);
	}

	private static <C extends Component> String getComponentDisplayText(C component, Function<C, String> textExtractionFunction) {
		String text = textExtractionFunction.apply(component);
		return text == null || text.isEmpty()
			? getComponentDefaultDisplayText(component)
			: text + " (" + getComponentDefaultDisplayText(component) + " )";
	}
}
