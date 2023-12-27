package dd.kms.marple.api;

import dd.kms.marple.api.settings.evaluation.EvaluationSettingsBuilder;

import java.util.List;

import static dd.kms.zenodot.api.DirectoryCompletions.CompletionTarget;

public interface DirectoryCompletions
{
	static DirectoryCompletions create() {
		return new dd.kms.marple.impl.DirectoryCompletionsSettings();
	}

	DirectoryCompletions setCompletionTargets(List<CompletionTarget> completionTargets);
	DirectoryCompletions setFavoritePaths(List<String> favoritePaths);
	DirectoryCompletions setFavoriteUris(List<String> favoriteUris);
	DirectoryCompletions setCacheFileSystemAccess(boolean cacheFileSystemAccess);
	DirectoryCompletions setFileSystemAccessCacheTimeMs(long cacheTimeMs);

	EvaluationSettingsBuilder register(EvaluationSettingsBuilder evaluationSettingsBuilder);
}
