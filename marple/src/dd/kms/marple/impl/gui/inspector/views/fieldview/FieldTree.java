package dd.kms.marple.impl.gui.inspector.views.fieldview;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.settings.visual.ObjectView;
import dd.kms.marple.impl.gui.actionproviders.ActionProviderListeners;
import dd.kms.marple.impl.gui.actionprovidertree.ActionProviderTreeNodes;
import dd.kms.marple.impl.gui.actionprovidertree.inspectiontree.InspectionTreeNodes;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.util.stream.IntStream;

import static dd.kms.marple.impl.gui.common.GuiCommons.DEFAULT_INSETS;

public class FieldTree extends JPanel implements ObjectView
{
	private final JTree			tree			= new JTree();
	private final JScrollPane	treeScrollPane	= new JScrollPane(tree);

	public FieldTree(Object object, InspectionContext context) {
		super(new GridBagLayout());

		TreeModel model = InspectionTreeNodes.createModel(null, object, context);
		tree.setModel(model);

		add(treeScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, DEFAULT_INSETS, 0, 0));

		ActionProviderListeners.addMouseListeners(tree);
		ActionProviderTreeNodes.enableFullTextToolTips(tree);
	}

	@Override
	public String getViewName() {
		return "Quick Field View";
	}

	@Override
	public Component getViewComponent() {
		return this;
	}

	@Override
	public Object getViewSettings() {
		int[] expandedRows = getExpandedRows();
		int[] selectionRows = tree.getSelectionRows();
		return new FieldTreeSettings(expandedRows, selectionRows);
	}

	@Override
	public void applyViewSettings(Object settings, ViewSettingsOrigin origin) {
		if (settings instanceof FieldTreeSettings) {
			FieldTreeSettings treeSettings = (FieldTreeSettings) settings;
			if (origin == ViewSettingsOrigin.SAME_CONTEXT) {
				expandRows(treeSettings.getExpandedRows());
				tree.setSelectionRows(treeSettings.getSelectionRows());
			}
		}
	}

	private int[] getExpandedRows() {
		int numRows = tree.getRowCount();
		return IntStream.range(0, numRows)
			.filter(tree::isExpanded)
			.toArray();
	}

	private void expandRows(int[] expandedRows) {
		int numRows = tree.getRowCount();
		int nextExpandedRowIndex = 0;
		for (int row = 0; row < numRows; row++) {
			if (nextExpandedRowIndex < expandedRows.length && row == expandedRows[nextExpandedRowIndex]) {
				tree.expandRow(row);
				nextExpandedRowIndex++;
			} else {
				tree.collapseRow(row);
			}
		}
	}

	@Override
	public void dispose() {
		tree.setModel(null);
	}

	private static class FieldTreeSettings
	{
		private final int[] expandedRows;
		private final int[] selectionRows;

		private FieldTreeSettings(int[] expandedRows, int[] selectionRows) {
			this.expandedRows = expandedRows;
			this.selectionRows = selectionRows;
		}

		int[] getExpandedRows() {
			return expandedRows;
		}

		int[] getSelectionRows() {
			return selectionRows;
		}
	}
}
