package dd.kms.marple.instancesearch.settings;

import com.google.common.base.Predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

class SearchSettingsBuilderImpl implements SearchSettingsBuilder
{
	private boolean						extendPathsBeyondAcceptedInstances	= false;
	private boolean						searchOnlyNonStaticFields			= true;
	private boolean						searchOnlyPureFields				= false;
	private List<Predicate<Class<?>>>	exclusionFilters					= new ArrayList<>();
	private boolean						limitSearchDepth					= true;
	private int							maximumSearchDepth					= 10;

	@Override
	public SearchSettingsBuilder extendPathsBeyondAcceptedInstances(boolean extendPathsBeyondAcceptedInstances) {
		this.extendPathsBeyondAcceptedInstances = extendPathsBeyondAcceptedInstances;
		return this;
	}

	@Override
	public SearchSettingsBuilder searchOnlyNonStaticFields(boolean searchOnlyNonStaticFields) {
		this.searchOnlyNonStaticFields = searchOnlyNonStaticFields;
		return this;
	}

	@Override
	public SearchSettingsBuilder searchOnlyPureFields(boolean searchOnlyPureFields) {
		this.searchOnlyPureFields = searchOnlyPureFields;
		return this;
	}

	@Override
	public SearchSettingsBuilder addClassesToExclude(Class<?>... classesToExclude) {
		Predicate<Class<?>> exclusionFilter = clazz -> Arrays.stream(classesToExclude).parallel()
			.anyMatch(clazzToExclude -> clazzToExclude.isAssignableFrom(clazz));
		addExclusionFilter(exclusionFilter);
		return this;
	}

	@Override
	public SearchSettingsBuilder addExclusionFilter(Predicate<Class<?>> exclusionFilter) {
		this.exclusionFilters.add(exclusionFilter);
		return this;
	}

	@Override
	public SearchSettingsBuilder limitSearchDepth(boolean limitSearchDepth) {
		this.limitSearchDepth = limitSearchDepth;
		return this;
	}

	@Override
	public SearchSettingsBuilder maximumSearchDepth(int maximumSearchDepth) {
		this.maximumSearchDepth = maximumSearchDepth;
		return this;
	}

	@Override
	public SearchSettings build() {
		return new SearchSettingsImpl(extendPathsBeyondAcceptedInstances, searchOnlyNonStaticFields, searchOnlyPureFields, disjunction(exclusionFilters).negate(), limitSearchDepth, maximumSearchDepth);
	}

	private Predicate<Class<?>> disjunction(List<Predicate<Class<?>>> predicates) {
		return predicates.stream().reduce(Predicates.alwaysFalse(), Predicate::or);
	}
}
