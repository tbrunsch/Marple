package dd.kms.marple.api.settings.visual;

import java.util.List;
import java.util.Map;

public interface UniformView
{
	boolean canViewAsList(Object object, boolean considerOnlyPreferredViews);
	List<?> asList(Object object);
	boolean canViewAsIterable(Object object, boolean considerOnlyPreferredViews);
	Iterable<?> asIterable(Object object);
	boolean canViewAsMap(Object object, boolean considerOnlyPreferredViews);
	Map<?, ?> asMap(Object object);
}
