package dd.kms.marple.impl.instancesearch.settings;

import java.util.function.Predicate;

class SearchSettingsImpl implements SearchSettings
{
	private final boolean				extendPathsBeyondAcceptedInstances;
	private final boolean				searchOnlyNonStaticFields;
	private final boolean				searchOnlyPureFields;
	private final Predicate<Class<?>>	classFilter;
	private final boolean				limitSearchDepth;
	private final int					maximumSearchDepth;

	SearchSettingsImpl(boolean extendPathsBeyondAcceptedInstances, boolean searchOnlyNonStaticFields, boolean searchOnlyPureFields, Predicate<Class<?>> classFilter, boolean limitSearchDepth, int maximumSearchDepth) {
		this.extendPathsBeyondAcceptedInstances = extendPathsBeyondAcceptedInstances;
		this.searchOnlyNonStaticFields = searchOnlyNonStaticFields;
		this.searchOnlyPureFields = searchOnlyPureFields;
		this.classFilter = classFilter;
		this.limitSearchDepth = limitSearchDepth;
		this.maximumSearchDepth = maximumSearchDepth;
	}

	@Override
	public boolean isExtendPathsBeyondAcceptedInstances() {
		return extendPathsBeyondAcceptedInstances;
	}

	@Override
	public boolean isSearchOnlyNonStaticFields() {
		return searchOnlyNonStaticFields;
	}

	@Override
	public boolean isSearchOnlyPureFields() {
		return searchOnlyPureFields;
	}

	@Override
	public Predicate<Class<?>> getClassFilter() {
		return classFilter;
	}

	@Override
	public boolean isLimitSearchDepth() {
		return limitSearchDepth;
	}

	@Override
	public int getMaximumSearchDepth() {
		return maximumSearchDepth;
	}

	@Override
	public int getEffectiveMaxSearchDepth() {
		return isLimitSearchDepth() ? getMaximumSearchDepth() : Integer.MAX_VALUE;
	}
}
