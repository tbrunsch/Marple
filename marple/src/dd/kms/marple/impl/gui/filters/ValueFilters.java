package dd.kms.marple.impl.gui.filters;

import dd.kms.marple.api.InspectionContext;

import java.awt.*;
import java.util.function.Function;

public class ValueFilters
{
	public static final ValueFilter NONE	= new NoTableValueFilter();

	public static ValueFilter createSelectionFilter(InspectionContext context) {
		return new ValueFilterSelection(context);
	}

	public static ValueFilter createWildcardFilter() {
		return createWildcardFilter(o -> o.toString());
	}

	public static ValueFilter createWildcardFilter(Function<Object, String> stringRepresentationProvider) {
		return new ValueFilterWildcard(stringRepresentationProvider);
	}

	public static ValueFilter createModifierFilter(boolean configureStaticMode) { return new ValueFilterModifier(configureStaticMode); }

	private static class NoTableValueFilter implements ValueFilter
	{
		@Override
		public boolean isActive() {
			return false;
		}

		@Override
		public void addAvailableValue(Object o) {
			/* do nothing */
		}

		@Override
		public Component getEditor() {
			return null;
		}

		@Override
		public Object getSettings() {
			return null;
		}

		@Override
		public void applySettings(Object settings) {
			/* nothing to do */
		}

		@Override
		public void addFilterChangedListener(Runnable listener) {
			/* do nothing */
		}

		@Override
		public void removeFilterChangedListener(Runnable listener) {
			/* do nothing */
		}

		@Override
		public boolean test(Object o) {
			return true;
		}
	}
}
