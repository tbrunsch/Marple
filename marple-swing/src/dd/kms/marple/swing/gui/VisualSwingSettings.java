package dd.kms.marple.swing.gui;

import dd.kms.marple.gui.VisualSettingsBuilder;
import dd.kms.marple.swing.gui.views.FieldView;
import dd.kms.marple.swing.gui.views.MethodView;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.function.Function;

public class VisualSwingSettings
{
	public static void addDefaultDisplayTextFunctions(VisualSettingsBuilder<Component> builder) {
		builder
			.displayText(Component.class, 		VisualSwingSettings::getComponentDefaultDisplayText)
			.displayText(Frame.class, 			frame -> getComponentDisplayText(frame, Frame::getTitle))
			.displayText(AbstractButton.class,	button -> getComponentDisplayText(button, AbstractButton::getText))
			.displayText(JLabel.class,			label -> getComponentDisplayText(label, JLabel::getText))
			.displayText(JTextComponent.class, 	textComponent -> getComponentDisplayText(textComponent, JTextComponent::getText))
			;
		// TODO: Add special functions for special objects
	}

	public static void addDefaultViews(VisualSettingsBuilder<Component> builder) {
		builder
			.objectView(Object.class, FieldView::new)
			.objectView(Object.class, MethodView::new)
			;
		// TODO: Add special view for collections
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
