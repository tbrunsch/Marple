package dd.kms.marple.impl.settings.visual;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.settings.visual.UniformView;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class UniformViewImpl implements UniformView {
	private final List<UniformViewData<List<?>>>		listViews;
	private final List<UniformViewData<Iterable<?>>>	iterableViews;
	private final List<UniformViewData<Map<?, ?>>>		mapViews;

	UniformViewImpl(List<UniformViewData<List<?>>> listViews, List<UniformViewData<Iterable<?>>> iterableViews, List<UniformViewData<Map<?, ?>>> mapViews) {
		this.listViews = ImmutableList.copyOf(listViews);
		this.iterableViews = ImmutableList.copyOf(iterableViews);
		this.mapViews = ImmutableList.copyOf(mapViews);
	}

	@Override
	public boolean canViewAsList(Object object, boolean considerOnlyPreferredViews) {
		if (object == null) {
			return false;
		}
		return object instanceof List<?>
			|| object.getClass().isArray()
			|| hasView(object, listViews, considerOnlyPreferredViews);
	}

	@Override
	public List<?> asList(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Null cannot be converted to a List.");
		}

		if (object instanceof List<?>) {
			return (List<?>) object;
		}

		if (object.getClass().isArray()) {
			return getListViewOfArray(object);
		}

		return getView(object, listViews);
	}

	@Override
	public boolean canViewAsIterable(Object object, boolean considerOnlyPreferredViews) {
		if (object == null) {
			return false;
		}
		return object instanceof Iterable<?>
			|| canViewAsList(object, considerOnlyPreferredViews)
			|| hasView(object, iterableViews, considerOnlyPreferredViews);
	}

	@Override
	public Iterable<?> asIterable(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Null cannot be converted to an Iterable.");
		}

		if (object instanceof Iterable<?>) {
			return (Iterable<?>) object;
		}

		if (canViewAsList(object, false)) {
			return asList(object);
		}

		return getView(object, iterableViews);
	}

	@Override
	public boolean canViewAsMap(Object object, boolean considerOnlyPreferredViews) {
		if (object == null) {
			return false;
		}
		return object instanceof Map
			|| hasView(object, mapViews, considerOnlyPreferredViews);
	}

	@Override
	public Map<?, ?> asMap(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Null cannot be converted to a Map.");
		}

		if (object instanceof Map<?, ?>) {
			return (Map<?, ?>) object;
		}

		return getView(object, mapViews);
	}

	private static List<?> getListViewOfArray(Object object) {
		return new AbstractList<Object>() {
			@Override
			public Object get(int index) {
				return Array.get(object, index);
			}

			@Override
			public int size() {
				return Array.getLength(object);
			}
		};
	}

	private static boolean hasView(Object object, List<? extends UniformViewData<?>> views, boolean considerOnlyPreferredViews) {
		for (UniformViewData<?> view : views) {
			if (considerOnlyPreferredViews && !view.isPreferredView()){
				continue;
			}
			Object objectView = view.getView(object);
			if (objectView != null) {
				return true;
			}
		}
		return false;
	}

	private static <T> T getView(Object object, List<? extends UniformViewData<T>> views) {
		for (UniformViewData<T> view : views) {
			T objectView = view.getView(object);
			if (objectView != null) {
				return objectView;
			}
		}
		throw new IllegalArgumentException("Object '" + object + "' cannot be converted to the desired view.");
	}

	static class UniformViewData<T>
	{
		private final Class<?>				objectClass;
		private final Function<Object, T>	viewProvider;
		private final boolean				preferredView;

		<S> UniformViewData(Class<S> objectClass, Function<S, ? extends T> viewFunction, boolean preferredView) {
			this.objectClass = objectClass;
			this.viewProvider = object -> viewFunction.apply(objectClass.cast(object));
			this.preferredView = preferredView;
		}

		@Nullable
		T getView(Object object) {
			return objectClass.isInstance(object) ? viewProvider.apply(object) : null;
		}

		boolean isPreferredView() {
			return preferredView;
		}
	}
}
