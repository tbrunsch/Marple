package dd.kms.marple.impl;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.DirectoryCompletionsUI;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.settings.evaluation.AdditionalEvaluationSettings;
import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;
import dd.kms.marple.framework.common.XmlUtils;
import dd.kms.marple.framework.common.XmlUtils.ParseException;
import dd.kms.zenodot.api.DirectoryCompletions.CompletionTarget;
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

public class DirectoryCompletionsSettings implements AdditionalEvaluationSettings, DirectoryCompletionsUI
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
	public DirectoryCompletionsUI setCompletionTargets(List<CompletionTarget> completionTargets) {
		this.completionTargets = ImmutableList.copyOf(completionTargets);
		return this;
	}

	public List<String> getFavoritePaths() {
		return favoritePaths;
	}

	@Override
	public DirectoryCompletionsUI setFavoritePaths(List<String> favoritePaths) {
		this.favoritePaths = ImmutableList.copyOf(favoritePaths);
		return this;
	}

	public List<String> getFavoriteUris() {
		return favoriteUris;
	}

	@Override
	public DirectoryCompletionsUI setFavoriteUris(List<String> favoriteUris) {
		this.favoriteUris = ImmutableList.copyOf(favoriteUris);
		return this;
	}

	public boolean isCacheFileSystemAccess() {
		return cacheFileSystemAccess;
	}

	@Override
	public DirectoryCompletionsUI setCacheFileSystemAccess(boolean cacheFileSystemAccess) {
		this.cacheFileSystemAccess = cacheFileSystemAccess;
		return this;
	}

	public long getFileSystemAccessCacheTimeMs() {
		return fileSystemAccessCacheTimeMs;
	}

	@Override
	public DirectoryCompletionsUI setFileSystemAccessCacheTimeMs(long cacheTimeMs) {
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
	public void readSettings(Element settingsElement) throws ParseException {
		List<CompletionTarget> completionTargets = XmlUtils.readList(settingsElement, "CompletionTargets", "CompletionTarget", node -> XmlUtils.readEnumAttribute(node, CompletionTarget.class));
		setCompletionTargets(completionTargets);

		List<String> favoritePaths = XmlUtils.readList(settingsElement, "FavoritePaths", "Path", Node::getTextContent);
		setFavoritePaths(favoritePaths);

		List<String> favoriteUris = XmlUtils.readList(settingsElement, "FavoriteUris", "Uri", Node::getTextContent);
		setFavoriteUris(favoriteUris);

		Element fileSystemAccessElement = XmlUtils.getUniqueChild(settingsElement, "FileSystemAccess");
		boolean cacheFileSystemAccess = Boolean.parseBoolean(fileSystemAccessElement.getAttribute("cache"));
		setCacheFileSystemAccess(cacheFileSystemAccess);

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
		dd.kms.zenodot.api.DirectoryCompletions directoryCompletions = dd.kms.zenodot.api.DirectoryCompletions.create();
		directoryCompletions.completionTargets(completionTargets.toArray(new CompletionTarget[0]));

		FileDirectoryStructure fileDirectoryStructure = getFileDirectoryStructure(fileSystemAccessCacheTimeMs);
		PathDirectoryStructure pathDirectoryStructure = getPathDirectoryStructure(fileSystemAccessCacheTimeMs);
		directoryCompletions
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
		directoryCompletions
			.favoritePaths(favoritePaths)
			.favoriteUris(favoriteUris);

		ExpressionEvaluator evaluator = context.getEvaluator();
		ParserSettingsBuilder builder = evaluator.getParserSettings().builder();
		directoryCompletions.configure(builder);
		evaluator.setParserSettings(builder.build());
	}

	@Override
	public JPanel createSettingsPanel(InspectionContext context) {
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
