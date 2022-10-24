package dd.kms.marple.impl.gui.actionprovidertree;

import dd.kms.marple.api.settings.visual.VisualSettingsUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.util.Objects;

public class ActionProviderTreeNodes
{
	private static final int	MAX_TOOLTIP_LENGTH	= 1000;

	public static void enableFullTextToolTips(JTree tree) {
		ToolTipManager.sharedInstance().registerComponent(tree);

		final TreeCellRenderer oldRenderer = tree.getCellRenderer();

		tree.setCellRenderer(new TreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				Component rendererComponent = oldRenderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				setToolTipText(rendererComponent, null);
				if (value instanceof ActionProviderTreeNode) {
					ActionProviderTreeNode node = (ActionProviderTreeNode) value;
					String fullText = node.getFullText();
					if (fullText.length() > MAX_TOOLTIP_LENGTH) {
						fullText = fullText.substring(0, MAX_TOOLTIP_LENGTH - 3) + "...";
					}
					if (!Objects.equals(VisualSettingsUtils.getText(rendererComponent), fullText)) {
						setToolTipText(rendererComponent, splitToHtmlLines(fullText, 100));
					}
				}
				return rendererComponent;
			}

			private void setToolTipText(Component component, String toolTipText) {
				if (component instanceof JComponent) {
					((JComponent) component).setToolTipText(toolTipText);
				}
			}

			private String splitToHtmlLines(String text, int numCharactersPerLine) {
				StringBuilder builder = new StringBuilder("<html>");
				int charCount = 0;
				for (int i = 0; i < text.length(); i++) {
					if (charCount == numCharactersPerLine) {
						builder.append("<br/>");
						charCount = 0;
					}
					builder.append(text.charAt(i));
					charCount++;
				}
				return builder.append("</html>").toString();
			}
		});
	}
}
