package dd.kms.marple.impl.gui.actionprovidertree;

import java.awt.*;
import java.util.Objects;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ActionProviderTreeNodes
{
	public static void enableFullTextToolTips(JTree tree) {
		ToolTipManager.sharedInstance().registerComponent(tree);

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				JLabel rendererComponent = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				rendererComponent.setToolTipText(null);
				if (value instanceof ActionProviderTreeNode) {
					ActionProviderTreeNode node = (ActionProviderTreeNode) value;
					String fullText = node.getFullText();
					if (!Objects.equals(rendererComponent.getText(), fullText)) {
						rendererComponent.setToolTipText(splitToHtmlLines(fullText, 100));
					}
				}
				return rendererComponent;
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
