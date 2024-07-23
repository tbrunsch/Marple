package dd.kms.marple.impl;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.framework.common.PreferenceUtils;
import dd.kms.marple.framework.common.UniformDocumentListener;
import dd.kms.zenodot.api.DirectoryCompletionExtension.CompletionTarget;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.awt.GridBagConstraints.*;

class DirectoryCompletionsSettingsPanel extends JPanel implements Disposable
{
	private final DirectoryCompletionsSettings	settings;
	private final InspectionContext				context;

	private final JPanel					completionTargetsPanel		= new JPanel(new FlowLayout(FlowLayout.LEFT));
	private final JCheckBox					completeFileConstructorsCB	= new JCheckBox("new File()");
	private final JCheckBox					completePathConstructorsCB	= new JCheckBox("Paths.get()");
	private final JCheckBox					completePathResolutionsCB	= new JCheckBox("Path.resolve()");
	private final JCheckBox					completeUriConstructorsCB	= new JCheckBox("new URI()");

	private final JPanel					favoritePathsPanel			= new JPanel(new GridBagLayout());
	private final DefaultListModel<String>	favoritePathsListModel		= new DefaultListModel<>();
	private final JList<String>				favoritePathsList			= new JList<>(favoritePathsListModel);
	private final JScrollPane				favoritePathsScrollPane		= new JScrollPane(favoritePathsList);
	private final JLabel					favoritePathLabel			= new JLabel("new favorite path:");
	private final JTextField				favoritePathTF				= new JTextField();
	private final JButton					addFavoritePathButton		= new JButton("+");
	private final JButton					removeFavoritePathsButton	= new JButton("-");

	private final JPanel					favoriteUrisPanel			= new JPanel(new GridBagLayout());
	private final DefaultListModel<String>	favoriteUrisListModel		= new DefaultListModel<>();
	private final JList<String>				favoriteUrisList			= new JList<>(favoriteUrisListModel);
	private final JScrollPane				favoriteUrisScrollPane		= new JScrollPane(favoriteUrisList);
	private final JLabel					favoriteUriLabel			= new JLabel("new favorite URI:");
	private final JTextField				favoriteUriTF				= new JTextField();
	private final JButton					addFavoriteUriButton		= new JButton("+");
	private final JButton					removeFavoriteUrisButton	= new JButton("-");

	private final JPanel					fileSystemAccessPanel		= new JPanel(new FlowLayout(FlowLayout.LEFT));
	private final JCheckBox					cacheFileSystemAccessesCB	= new JCheckBox("cache access:");
	private final JTextField				fileSystemAccessCacheTimeTF	= new JTextField(5000);
	private final JLabel					cacheTimeUnitLabel			= new JLabel("ms");

	private final ActionListener			settingsChangedActionListener	= e -> updateSettings();
	private final ActionListener			addFavoritePathListener			= e -> addFavoritePath();
	private final ActionListener			removeFavoritePathsListener		= e -> removeFavoritePaths();
	private final ActionListener			addFavoriteUriListener			= e -> addFavoriteUri();
	private final ActionListener			removeFavoriteUrisListener		= e -> removeFavoriteUris();
	private final ListSelectionListener		favoriteSelectionListener		= e -> updateEnabilities();
	private final DocumentListener			favoriteDocumentListener		= UniformDocumentListener.create(this::updateEnabilities);
	private final DocumentListener			fileSystemCacheDocumentListener	= UniformDocumentListener.create(this::updateSettings);

	public DirectoryCompletionsSettingsPanel(DirectoryCompletionsSettings settings, InspectionContext context) {
		super(new GridBagLayout());

		this.settings = settings;
		this.context = context;

		init();
		addListeners();
		initWithValues();
	}

	private void init() {
		completionTargetsPanel.setBorder(BorderFactory.createTitledBorder("Suggest code completions for:"));
		favoritePathsPanel.setBorder(BorderFactory.createTitledBorder("Favorite paths"));
		favoriteUrisPanel.setBorder(BorderFactory.createTitledBorder("Favorite URIs"));
		fileSystemAccessPanel.setBorder(BorderFactory.createTitledBorder("File system access"));

		int x;
		int y = 0;

		add(completionTargetsPanel,		new GridBagConstraints(0, y++, REMAINDER, 1, 1.0, 0.0, WEST, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		add(favoritePathsPanel,			new GridBagConstraints(0, y++, REMAINDER, 1, 1.0, 0.5, WEST, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		add(favoriteUrisPanel,			new GridBagConstraints(0, y++, REMAINDER, 1, 1.0, 0.5, WEST, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		add(fileSystemAccessPanel,	new GridBagConstraints(0, y++, REMAINDER, 1, 1.0, 0.0, WEST, BOTH, new Insets(3, 3, 3, 3), 0, 0));

		completionTargetsPanel.add(completeFileConstructorsCB);
		completionTargetsPanel.add(completePathConstructorsCB);
		completionTargetsPanel.add(completePathResolutionsCB);
		completionTargetsPanel.add(completeUriConstructorsCB);

		favoritePathsPanel.add(favoritePathsScrollPane,	new GridBagConstraints(0, y++, REMAINDER, 1, 1.0, 1.0, WEST, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		x = 0;
		favoritePathsPanel.add(favoritePathLabel,			new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(3, 3, 3, 3), 0, 0));
		favoritePathsPanel.add(favoritePathTF,				new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, new Insets(3, 3, 3, 3), 0, 0));
		favoritePathsPanel.add(addFavoritePathButton,		new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(3, 3, 3, 3), 0, 0));
		favoritePathsPanel.add(removeFavoritePathsButton,	new GridBagConstraints(x++, y++, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(3, 3, 3, 3), 0, 0));

		favoriteUrisPanel.add(favoriteUrisScrollPane,		new GridBagConstraints(0, y++, REMAINDER, 1, 1.0, 1.0, WEST, BOTH, new Insets(3, 3, 3, 3), 0, 0));
		x = 0;
		favoriteUrisPanel.add(favoriteUriLabel,			new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(3, 3, 3, 3), 0, 0));
		favoriteUrisPanel.add(favoriteUriTF,				new GridBagConstraints(x++, y, 1, 1, 1.0, 0.0, WEST, HORIZONTAL, new Insets(3, 3, 3, 3), 0, 0));
		favoriteUrisPanel.add(addFavoriteUriButton,		new GridBagConstraints(x++, y, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(3, 3, 3, 3), 0, 0));
		favoriteUrisPanel.add(removeFavoriteUrisButton,	new GridBagConstraints(x++, y++, 1, 1, 0.0, 0.0, WEST, NONE, new Insets(3, 3, 3, 3), 0, 0));

		fileSystemAccessCacheTimeTF.setColumns(10);
		fileSystemAccessCacheTimeTF.setMinimumSize(fileSystemAccessCacheTimeTF.getPreferredSize());

		fileSystemAccessPanel.add(cacheFileSystemAccessesCB);
		fileSystemAccessPanel.add(fileSystemAccessCacheTimeTF);
		fileSystemAccessPanel.add(cacheTimeUnitLabel);
	}

	private void addListeners() {
		completeFileConstructorsCB.addActionListener(settingsChangedActionListener);
		completePathConstructorsCB.addActionListener(settingsChangedActionListener);
		completePathResolutionsCB.addActionListener(settingsChangedActionListener);
		completeUriConstructorsCB.addActionListener(settingsChangedActionListener);

		favoritePathsList.addListSelectionListener(favoriteSelectionListener);
		favoritePathTF.getDocument().addDocumentListener(favoriteDocumentListener);
		addFavoritePathButton.addActionListener(addFavoritePathListener);
		removeFavoritePathsButton.addActionListener(removeFavoritePathsListener);

		favoriteUrisList.addListSelectionListener(favoriteSelectionListener);
		favoriteUriTF.getDocument().addDocumentListener(favoriteDocumentListener);
		addFavoriteUriButton.addActionListener(addFavoriteUriListener);
		removeFavoriteUrisButton.addActionListener(removeFavoriteUrisListener);

		cacheFileSystemAccessesCB.addActionListener(settingsChangedActionListener);
		fileSystemAccessCacheTimeTF.getDocument().addDocumentListener(fileSystemCacheDocumentListener);
	}

	private void removeListeners() {
		completeFileConstructorsCB.removeActionListener(settingsChangedActionListener);
		completePathConstructorsCB.removeActionListener(settingsChangedActionListener);
		completePathResolutionsCB.removeActionListener(settingsChangedActionListener);
		completeUriConstructorsCB.removeActionListener(settingsChangedActionListener);

		favoritePathTF.getDocument().removeDocumentListener(favoriteDocumentListener);
		addFavoritePathButton.removeActionListener(addFavoritePathListener);
		removeFavoritePathsButton.removeActionListener(removeFavoritePathsListener);

		favoriteUriTF.getDocument().removeDocumentListener(favoriteDocumentListener);
		addFavoriteUriButton.removeActionListener(addFavoriteUriListener);
		removeFavoriteUrisButton.removeActionListener(removeFavoriteUrisListener);

		cacheFileSystemAccessesCB.removeActionListener(settingsChangedActionListener);
		fileSystemAccessCacheTimeTF.getDocument().removeDocumentListener(fileSystemCacheDocumentListener);
	}

	private void initWithValues() {
		List<CompletionTarget> completionTargets = settings.getCompletionTargets();
		completeFileConstructorsCB.setSelected(completionTargets.contains(CompletionTarget.FILE_CREATION));
		completePathConstructorsCB.setSelected(completionTargets.contains(CompletionTarget.PATH_CREATION));
		completePathResolutionsCB.setSelected(completionTargets.contains(CompletionTarget.PATH_RESOLUTION));
		completeUriConstructorsCB.setSelected(completionTargets.contains(CompletionTarget.URI_CREATION));

		for (String favoritePath : settings.getFavoritePaths()) {
			favoritePathsListModel.addElement(favoritePath);
		}

		for (String favoriteUri : settings.getFavoriteUris()) {
			favoriteUrisListModel.addElement(favoriteUri);
		}

		cacheFileSystemAccessesCB.setSelected(settings.isCacheFileSystemAccess());

		long cacheTimeMs = settings.getFileSystemAccessCacheTimeMs();
		if (cacheTimeMs < 0) {
			cacheTimeMs = DirectoryCompletionsSettings.DEFAULT_CACHE_TIME_MS;
		}
		fileSystemAccessCacheTimeTF.setText(String.valueOf(cacheTimeMs));

		updateEnabilities();
	}

	@Override
	public void dispose() {
		removeListeners();
	}

	private void updateSettings() {
		settings.setCompletionTargets(getCompletionTargets());

		boolean cacheFileSystemAccess = cacheFileSystemAccessesCB.isSelected();
		settings.setCacheFileSystemAccess(cacheFileSystemAccess);

		long cacheTimeMs = parseFileSystemAccessCacheTime();
		settings.setFileSystemAccessCacheTimeMs(cacheTimeMs);

		List<String> favoritePaths = getElements(favoritePathsListModel);
		settings.setFavoritePaths(favoritePaths);

		List<String> favoriteUris = getElements(favoriteUrisListModel);
		settings.setFavoriteUris(favoriteUris);

		updateEnabilities();
		settings.applySettings(context);
		PreferenceUtils.writeSettings(context);
	}

	private void updateEnabilities() {
		addFavoritePathButton.setEnabled(!favoritePathTF.getText().trim().isEmpty());
		removeFavoritePathsButton.setEnabled(favoritePathsList.getSelectedIndices().length > 0);
		addFavoriteUriButton.setEnabled(!favoriteUriTF.getText().trim().isEmpty());
		removeFavoriteUrisButton.setEnabled(favoriteUrisList.getSelectedIndices().length > 0);
		boolean cacheFileAccess = cacheFileSystemAccessesCB.isSelected();
		fileSystemAccessCacheTimeTF.setEnabled(cacheFileAccess);
		cacheTimeUnitLabel.setEnabled(cacheFileAccess);
	}

	private long parseFileSystemAccessCacheTime() {
		String s = fileSystemAccessCacheTimeTF.getText().trim();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < '0' || c > '9') {
				return -1;
			}
		}
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private List<CompletionTarget> getCompletionTargets() {
		List<CompletionTarget> completionTargets = new ArrayList<>();
		if (completeFileConstructorsCB.isSelected()) {
			completionTargets.add(CompletionTarget.FILE_CREATION);
		}
		if (completePathConstructorsCB.isSelected()) {
			completionTargets.add(CompletionTarget.PATH_CREATION);
		}
		if (completePathResolutionsCB.isSelected()) {
			completionTargets.add(CompletionTarget.PATH_RESOLUTION);
		}
		if (completeUriConstructorsCB.isSelected()) {
			completionTargets.add(CompletionTarget.URI_CREATION);
		}
		return completionTargets;
	}

	private List<String> getElements(DefaultListModel<String> listModel) {
		int numImports = listModel.getSize();
		List<String> elements = new ArrayList<>(numImports);
		for (int i = 0; i < numImports; i++) {
			elements.add(listModel.get(i));
		}
		return elements;
	}

	private void addFavoritePath() {
		String favoritePath = favoritePathTF.getText().trim();
		String error = getPathInterpretationError(favoritePath);
		if (error != null) {
			displayError("The string does not represent a valid Path: " + error);
			return;
		}
		favoritePathsListModel.addElement(favoritePath);
		favoritePathTF.setText("");
		updateSettings();
	}

	@Nullable
	private String getPathInterpretationError(String s) {
		try {
			Path ignored = Paths.get(s);
			return null;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private void removeFavoritePaths() {
		int[] selectedRows = favoritePathsList.getSelectedIndices();
		for (int i = selectedRows.length - 1; i >= 0; i--) {
			int row = selectedRows[i];
			favoritePathsListModel.remove(row);
		}
		updateSettings();
	}
	private void addFavoriteUri() {
		String favoriteUri = favoriteUriTF.getText().trim();
		String error = getUriInterpretationError(favoriteUri);
		if (error != null) {
			displayError("The string does not represent a valid URI: " + error);
			return;
		}
		favoriteUrisListModel.addElement(favoriteUri);
		favoriteUriTF.setText("");
		updateSettings();
	}

	@Nullable
	private String getUriInterpretationError(String s) {
		try {
			URI ignored = URI.create(s);
			return null;
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	private void removeFavoriteUris() {
		int[] selectedRows = favoriteUrisList.getSelectedIndices();
		for (int i = selectedRows.length - 1; i >= 0; i--) {
			int row = selectedRows[i];
			favoriteUrisListModel.remove(row);
		}
		updateSettings();
	}

	private void displayError(String error) {
		JOptionPane.showMessageDialog(this, error, "Interpretation Error", JOptionPane.ERROR_MESSAGE);
	}
}
