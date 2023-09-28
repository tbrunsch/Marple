package dd.kms.marple.impl.gui.evaluator.completion;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CompletionListPopupMouseListener extends MouseAdapter
{
	private final JList<?>				list;
	private final ParserMediator		parserMediator;

	CompletionListPopupMouseListener(JList<?> list, ParserMediator parserMediator) {
		this.list = list;
		this.parserMediator = parserMediator;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		handlePopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		handlePopup(e);
	}

	private void handlePopup(MouseEvent e) {
		if (!e.isPopupTrigger()) {
			return;
		}
		CustomCompletion completion = getCompletionForPosition(e.getPoint());
		if (completion == null) {
			return;
		}
		List<JMenuItem> popupMenuItems = getPopupMenuItems(completion.getCodeCompletion());
		if (popupMenuItems.isEmpty()) {
			return;
		}
		JPopupMenu popup = new JPopupMenu();
		popupMenuItems.forEach(popup::add);
		popup.show(e.getComponent(), e.getX(), e.getY());
	}

	private CustomCompletion getCompletionForPosition(Point position) {
		int index = list.locationToIndex(position);
		if (index < 0) {
			return null;
		}
		Rectangle cellBounds = list.getCellBounds(index, index);
		if (!cellBounds.contains(position)) {
			return null;
		}
		Object element = list.getModel().getElementAt(index);
		return element instanceof CustomCompletion ? (CustomCompletion) element : null;
	}

	private List<JMenuItem> getPopupMenuItems(CodeCompletion codeCompletion) {
		if (codeCompletion.getType() != CodeCompletionType.CLASS) {
			return Collections.emptyList();
		}
		CodeCompletionClass classCompletion = (CodeCompletionClass) codeCompletion;
		Class<?> clazz;
		try {
			clazz = classCompletion.getClassInfo().asClass();
		} catch (IllegalStateException ignored) {
			/* nothing we can do here */
			return Collections.emptyList();
		}

		List<JMenuItem> popupMenuItems = new ArrayList<>();
		if (parserMediator.isClassImportedTemporarily(clazz.getName())) {
			AbstractAction action = createAction(
				"Remove temporary import",
				"Remove this class from the list of temporarily imported classes",
				() -> parserMediator.removeClassFromTemporaryImports(clazz)
			);
			JMenuItem unimportClassMenuItem = new JMenuItem(action);
			popupMenuItems.add(unimportClassMenuItem);
		}

		return popupMenuItems;
	}

	private AbstractAction createAction(String name, String description, Runnable runnable) {
		AbstractAction action = new AbstractAction(name) {
			@Override
			public void actionPerformed(ActionEvent e) {
				runnable.run();
				list.invalidate();
				list.repaint();
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, description);
		return action;
	}
}
