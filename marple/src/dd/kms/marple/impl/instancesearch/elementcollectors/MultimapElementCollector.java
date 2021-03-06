package dd.kms.marple.impl.instancesearch.elementcollectors;

import com.google.common.collect.Multimap;
import dd.kms.marple.impl.instancesearch.InstancePath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MultimapElementCollector extends AbstractElementCollector
{
	private static final MultimapElementCollector	COLLECTOR	= new MultimapElementCollector();

	/**
	 * Warning: not thread-safe
	 */
	public static List<InstancePath> collect(Multimap<?,?> multimap, InstancePath parent, Function<Object, String> toStringFunction) {
		COLLECTOR.multimap = multimap;
		COLLECTOR.parent = parent;
		COLLECTOR.toStringFunction = toStringFunction;
		return COLLECTOR.collect();
	}

	private Multimap<?,?>				multimap;
	private InstancePath				parent;
	private Function<Object, String>	toStringFunction;

	@Override
	void doCollect() {
		children = new ArrayList<>();
		int i = 0;
		for (Map.Entry<?, ? extends Collection<?>> entry : multimap.asMap().entrySet()) {
			Object key = entry.getKey();
			InstancePath keyChild = new InstancePath(key, ".keySet(){" + i + "}", parent);
			children.add(keyChild);
			String namePrefix = ".get(" + toStringFunction.apply(key) + ")";
			int j = 0;
			for (Object value : entry.getValue()) {
				InstancePath valueChild = new InstancePath(value, namePrefix + "{" + j + "}", parent);
				children.add(valueChild);
				j++;
			}
			i++;
		}
	}
}
