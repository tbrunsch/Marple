package dd.kms.marple.api;

import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;

import java.util.List;

import static dd.kms.zenodot.api.DirectoryCompletions.CompletionTarget;

public interface DirectoryCompletionsUI
{
	static DirectoryCompletionsUI create() {
		return new dd.kms.marple.impl.DirectoryCompletionsSettings();
	}

	DirectoryCompletionsUI setCompletionTargets(List<CompletionTarget> completionTargets);
	DirectoryCompletionsUI setFavoritePaths(List<String> favoritePaths);
	DirectoryCompletionsUI setFavoriteUris(List<String> favoriteUris);
	DirectoryCompletionsUI setCacheFileSystemAccess(boolean cacheFileSystemAccess);
	DirectoryCompletionsUI setFileSystemAccessCacheTimeMs(long cacheTimeMs);

	EvaluationSettingsBuilder register(EvaluationSettingsBuilder evaluationSettingsBuilder);
}
