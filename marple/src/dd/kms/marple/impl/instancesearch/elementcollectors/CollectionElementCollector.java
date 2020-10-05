package dd.kms.marple.impl.instancesearch.elementcollectors;

import dd.kms.marple.impl.instancesearch.InstancePath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionElementCollector extends AbstractElementCollector
{
	private static final CollectionElementCollector COLLECTOR	= new CollectionElementCollector();

	/**
	 * Warning: not thread-safe
	 */
	public static List<InstancePath> collect(Collection<?> collection, InstancePath parent) {
		COLLECTOR.collection = collection;
		COLLECTOR.parent = parent;
		return COLLECTOR.collect();
	}

	private Collection<?>	collection;
	private InstancePath	parent;

	@Override
	void doCollect() {
		boolean isList = collection instanceof List;
		children = new ArrayList<>(collection.size());
		int i = 0;
		for (Object object : collection) {
			String name = isList ? ".get(" + i + ")" : "{" + i + "}";
			InstancePath child = new InstancePath(object, name, parent);
			children.add(child);
			i++;
		}
	}
}
