package dd.kms.marple.impl.instancesearch.elementcollectors;

import dd.kms.marple.impl.instancesearch.InstancePath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MapElementCollector extends AbstractElementCollector
{
	private static final MapElementCollector	COLLECTOR	= new MapElementCollector();

	/**
	 * Warning: not thread-safe
	 */
	public static List<InstancePath> collect(Map<?,?> map, InstancePath parentPath, Function<Object, String> toStringFunction) {
		COLLECTOR.map = map;
		COLLECTOR.parentPath = parentPath;
		COLLECTOR.toStringFunction = toStringFunction;
		return COLLECTOR.collect();
	}

	private Map<?,?>					map;
	private InstancePath				parentPath;
	private Function<Object, String>	toStringFunction;

	@Override
	void doCollect() {
		children = new ArrayList<>();
		int i = 0;
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object key = entry.getKey();
			InstancePath keyChild = new InstancePath(key, ".keySet(){" + i + "}", parentPath);
			children.add(keyChild);
			InstancePath valueChild = new InstancePath(entry.getValue(), ".get(" + toStringFunction.apply(key) + ")", parentPath);
			children.add(valueChild);
			i++;
		}
	}
}
