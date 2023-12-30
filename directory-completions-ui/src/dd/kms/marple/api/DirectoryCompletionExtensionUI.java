package dd.kms.marple.api;

import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;

import java.util.List;

import static dd.kms.zenodot.api.DirectoryCompletionExtension.CompletionTarget;

public interface DirectoryCompletionExtensionUI
{
	static DirectoryCompletionExtensionUI create() {
		return new dd.kms.marple.impl.DirectoryCompletionsSettings();
	}

	DirectoryCompletionExtensionUI setCompletionTargets(List<CompletionTarget> completionTargets);
	DirectoryCompletionExtensionUI setFavoritePaths(List<String> favoritePaths);
	DirectoryCompletionExtensionUI setFavoriteUris(List<String> favoriteUris);
	DirectoryCompletionExtensionUI setCacheFileSystemAccess(boolean cacheFileSystemAccess);
	DirectoryCompletionExtensionUI setFileSystemAccessCacheTimeMs(long cacheTimeMs);

	EvaluationSettingsBuilder register(EvaluationSettingsBuilder evaluationSettingsBuilder);
}
