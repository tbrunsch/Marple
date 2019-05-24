package dd.kms.marple.instancesearch.elementcollectors;

import com.google.common.collect.Multimap;
import dd.kms.marple.instancesearch.InstancePath;

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
	public static List<InstancePath> collect(Multimap<?,?> multimap, InstancePath parentPath, Function<Object, String> toStringFunction) {
		COLLECTOR.multimap = multimap;
		COLLECTOR.parentPath = parentPath;
		COLLECTOR.toStringFunction = toStringFunction;
		return COLLECTOR.collect();
	}

	private Multimap<?,?>				multimap;
	private InstancePath				parentPath;
	private Function<Object, String>	toStringFunction;

	@Override
	void doCollect() {
		children = new ArrayList<>();
		int i = 0;
		for (Map.Entry<?, ? extends Collection<?>> entry : multimap.asMap().entrySet()) {
			Object key = entry.getKey();
			InstancePath keyChild = new InstancePath(key, ".keySet(){" + i + "}", parentPath);
			children.add(keyChild);
			String namePrefix = ".get(" + toStringFunction.apply(key) + ")";
			int j = 0;
			for (Object value : entry.getValue()) {
				InstancePath valueChild = new InstancePath(value, namePrefix + "{" + j + "}", parentPath);
				children.add(valueChild);
				j++;
			}
			i++;
		}
	}
}
