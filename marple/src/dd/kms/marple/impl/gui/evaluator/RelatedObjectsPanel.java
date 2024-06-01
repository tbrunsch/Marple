package dd.kms.marple.impl.gui.evaluator;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.api.settings.InspectionSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettings;
import dd.kms.marple.api.settings.evaluation.NamedObject;
import dd.kms.marple.impl.actions.ActionProvider;
import dd.kms.marple.impl.actions.ActionProviderBuilder;
import dd.kms.marple.impl.gui.actionproviders.ActionProviderListeners;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;

class RelatedObjectsPanel extends JPanel implements Disposable
{
	private final InspectionContext				context;

	RelatedObjectsPanel(InspectionContext context) {
		super(new GridBagLayout());

		this.context = context;

		setBorder(BorderFactory.createTitledBorder("Related Objects"));
	}

	void setCurrentObject(Object object) {
		InspectionSettings settings = context.getSettings();
		EvaluationSettings evaluationSettings = settings.getEvaluationSettings();
		Collection<NamedObject> relatedObjects = evaluationSettings.getRelatedObjects(object);

		setVisible(!relatedObjects.isEmpty());

		removeAll();

		int y = 0;
		for (NamedObject relatedObject : relatedObjects) {
			String name = relatedObject.getName();
			Object relatedObjectValue = relatedObject.getObject();
			JLabel nameLabel = new JLabel(name + ":");
			String displayText = context.getDisplayText(relatedObjectValue);
			JLabel objectLabel = new JLabel(displayText);
			ActionProviderListeners.addMouseListeners(objectLabel, e -> createActionProvider(displayText, relatedObjectValue));

			add(nameLabel,		new GridBagConstraints(0, y, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, DEFAULT_INSETS, 0, 0));
			add(objectLabel,	new GridBagConstraints(1, y, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, DEFAULT_INSETS, 0, 0));

			y++;
		}
	}

	private ActionProvider createActionProvider(String displayText, Object object) {
		return new ActionProviderBuilder(displayText, object, context).build();
	}

	@Override
	public void dispose() {
		removeAll();
	}
}
