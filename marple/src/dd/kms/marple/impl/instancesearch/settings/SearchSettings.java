package dd.kms.marple.impl.instancesearch.settings;

import java.util.function.Predicate;

public interface SearchSettings
{
	boolean isExtendPathsBeyondAcceptedInstances();
	boolean isSearchOnlyNonStaticFields();
	boolean isSearchOnlyPureFields();
	Predicate<Class<?>> getClassFilter();
	boolean isLimitSearchDepth();
	int getMaximumSearchDepth();
	int getEffectiveMaxSearchDepth();
}
