package dd.kms.marple.impl.instancesearch.settings;

import java.util.function.Predicate;

public interface SearchSettingsBuilder
{
	 SearchSettingsBuilder extendPathsBeyondAcceptedInstances(boolean extendPathsBeyondAcceptedInstances);
	 SearchSettingsBuilder searchOnlyNonStaticFields(boolean searchOnlyNonStaticFields);
	 SearchSettingsBuilder searchOnlyPureFields(boolean searchOnlyPureFields);
	 SearchSettingsBuilder addClassesToExclude(Class<?>... classesToExclude);
	 SearchSettingsBuilder addExclusionFilter(Predicate<Class<?>> exclusionFilter);
	 SearchSettingsBuilder limitSearchDepth(boolean limitSearchDepth);
	 SearchSettingsBuilder maximumSearchDepth(int maximumSearchDepth);

	 SearchSettings build();
}
