package dd.kms.marple.impl;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.DirectoryCompletionExtensionUI;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.settings.evaluation.AdditionalEvaluationSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;
import dd.kms.marple.framework.common.PreferenceUtils;
import dd.kms.marple.framework.common.XmlUtils;
import dd.kms.marple.framework.common.XmlUtils.ParseException;
import dd.kms.zenodot.api.DirectoryCompletionExtension;
import dd.kms.zenodot.api.DirectoryCompletionExtension.CompletionTarget;
import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import javax.swing.*;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DirectoryCompletionsSettings implements AdditionalEvaluationSettings, DirectoryCompletionExtensionUI
{
	static final long	DEFAULT_CACHE_TIME_MS	= 5000;

	private List<CompletionTarget>	completionTargets			= ImmutableList.of();
	private List<String>			favoritePaths				= ImmutableList.of();
	private List<String>			favoriteUris				= ImmutableList.of();
	private boolean					cacheFileSystemAccess		= true;
	private long					fileSystemAccessCacheTimeMs	= DEFAULT_CACHE_TIME_MS;

	public List<CompletionTarget> getCompletionTargets() {
		return completionTargets;
	}

	@Override
	public DirectoryCompletionExtensionUI setCompletionTargets(List<CompletionTarget> completionTargets) {
		this.completionTargets = ImmutableList.copyOf(completionTargets);
		return this;
	}

	public List<String> getFavoritePaths() {
		return favoritePaths;
	}

	@Override
	public DirectoryCompletionExtensionUI setFavoritePaths(List<String> favoritePaths) {
		this.favoritePaths = ImmutableList.copyOf(favoritePaths);
		return this;
	}

	public List<String> getFavoriteUris() {
		return favoriteUris;
	}

	@Override
	public DirectoryCompletionExtensionUI setFavoriteUris(List<String> favoriteUris) {
		this.favoriteUris = ImmutableList.copyOf(favoriteUris);
		return this;
	}

	public boolean isCacheFileSystemAccess() {
		return cacheFileSystemAccess;
	}

	@Override
	public DirectoryCompletionExtensionUI setCacheFileSystemAccess(boolean cacheFileSystemAccess) {
		this.cacheFileSystemAccess = cacheFileSystemAccess;
		return this;
	}

	public long getFileSystemAccessCacheTimeMs() {
		return fileSystemAccessCacheTimeMs;
	}

	@Override
	public DirectoryCompletionExtensionUI setFileSystemAccessCacheTimeMs(long cacheTimeMs) {
		this.fileSystemAccessCacheTimeMs = Math.max(cacheTimeMs, 0L);
		return this;
	}

	@Override
	public EvaluationSettingsBuilder register(EvaluationSettingsBuilder evaluationSettingsBuilder) {
		return evaluationSettingsBuilder
				.setAdditionalSettings("Directory/File Completions", this);
	}

	@Nullable
	@Override
	public String getXmlElementName() {
		return "DirectoryCompletionsSettings";
	}

	@Override
	public void writeSettings(Element settingsElement) {
		XmlUtils.writeList(settingsElement, "CompletionTargets", "CompletionTarget", completionTargets, XmlUtils::writeEnumAttribute);
		XmlUtils.writeList(settingsElement, "FavoritePaths", "Path", favoritePaths, Element::setTextContent);
		XmlUtils.writeList(settingsElement, "FavoriteUris", "Uri", favoriteUris, Element::setTextContent);

		Element fileSystemAccessElement = XmlUtils.createChildElement(settingsElement, "FileSystemAccess");
		fileSystemAccessElement.setAttribute("cache", Boolean.toString(cacheFileSystemAccess));
		fileSystemAccessElement.setAttribute("cacheTimeMs", Long.toString(fileSystemAccessCacheTimeMs));
	}

	@Override
	public void readSettings(Element settingsElement) {
		List<CompletionTarget> completionTargets = XmlUtils.readListOrNull(settingsElement, "CompletionTargets", "CompletionTarget", node -> XmlUtils.readEnumAttribute(node, CompletionTarget.class));
		if (completionTargets == null) {
			PreferenceUtils.logParseExceptionAsWarning(new ParseException("Directory completions: Cannot read completion targets"));
		} else {
			setCompletionTargets(completionTargets);
		}

		List<String> favoritePaths = XmlUtils.readListOrNull(settingsElement, "FavoritePaths", "Path", Node::getTextContent);
		if (favoritePaths == null) {
			PreferenceUtils.logParseExceptionAsWarning(new ParseException("Directory completions: Cannot read favorite paths"));
		} else {
			setFavoritePaths(favoritePaths);
		}

		List<String> favoriteUris = XmlUtils.readListOrNull(settingsElement, "FavoriteUris", "Uri", Node::getTextContent);
		if (favoriteUris == null) {
			PreferenceUtils.logParseExceptionAsWarning(new ParseException("Directory completions: Cannot read favorite URIs"));
		} else {
			setFavoriteUris(favoriteUris);
		}

		Element fileSystemAccessElement = XmlUtils.getUniqueChildOrNull(settingsElement, "FileSystemAccess");
		if (fileSystemAccessElement != null) {
			boolean cacheFileSystemAccess = Boolean.parseBoolean(fileSystemAccessElement.getAttribute("cache"));
			setCacheFileSystemAccess(cacheFileSystemAccess);
		}

		long cacheTimeMs;
		try {
			cacheTimeMs = Long.parseLong(fileSystemAccessElement.getAttribute("cacheTimeMs"));
		} catch (Exception e) {
			cacheTimeMs = DEFAULT_CACHE_TIME_MS;
		}
		setFileSystemAccessCacheTimeMs(cacheTimeMs);
	}

	@Override
	public void applySettings(InspectionContext context) {
		DirectoryCompletionExtension directoryCompletionExtension = DirectoryCompletionExtension.create();
		directoryCompletionExtension.completionTargets(completionTargets.toArray(new CompletionTarget[0]));

		FileDirectoryStructure fileDirectoryStructure = getFileDirectoryStructure(fileSystemAccessCacheTimeMs);
		PathDirectoryStructure pathDirectoryStructure = getPathDirectoryStructure(fileSystemAccessCacheTimeMs);
		directoryCompletionExtension
			.fileDirectoryStructure(fileDirectoryStructure)
			.pathDirectoryStructure(pathDirectoryStructure);

		List<URI> favoriteUris = this.favoriteUris.stream()
			.map(s -> {
				try {
					return URI.create(s);
				} catch (Exception e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		directoryCompletionExtension
			.favoritePaths(favoritePaths)
			.favoriteUris(favoriteUris);

		ExpressionEvaluator evaluator = context.getEvaluator();
		ParserSettingsBuilder builder = evaluator.getParserSettings().builder();
		directoryCompletionExtension.configure(builder);
		evaluator.setParserSettings(builder.build());
	}

	@Override
	public JPanel createSettingsComponent(InspectionContext context) {
		return new DirectoryCompletionsSettingsPanel(this, context);
	}

	private FileDirectoryStructure getFileDirectoryStructure(long cacheTimeMs) {
		FileDirectoryStructure fileDirectoryStructure = FileDirectoryStructure.DEFAULT;
		return cacheTimeMs <= 0
			? fileDirectoryStructure
			: FileDirectoryStructure.cache(fileDirectoryStructure, cacheTimeMs);
	}

	private PathDirectoryStructure getPathDirectoryStructure(long cacheTimeMs) {
		PathDirectoryStructure pathDirectoryStructure = PathDirectoryStructure.DEFAULT;
		return cacheTimeMs <= 0
			? pathDirectoryStructure
			: PathDirectoryStructure.cache(pathDirectoryStructure, cacheTimeMs);
	}
}
