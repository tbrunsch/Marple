package dd.kms.marple;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class TestData
{
	private final List<Double>				demoList		= IntStream.range(0, 12345).mapToObj(i -> Math.random()).collect(Collectors.toList());
	private final double[]					demoArray 		= IntStream.range(0, 12345).mapToDouble(i -> Math.random()).toArray();
	private final Map<Double, String>		demoMap			= IntStream.range(0, 32).mapToObj(i -> Math.random()).collect(Collectors.toMap(d -> d, d -> d.toString()));
	private final Map<Double, String>		largeDemoMap	= IntStream.range(0, 12345).mapToObj(i -> Math.random()).collect(Collectors.toMap(d -> d, d -> d.toString()));
	private final Set<String>				demoSet			= IntStream.range(0, 32).mapToObj(i -> String.valueOf(Math.random())).collect(Collectors.toSet());
	private final Set<String>				largeDemoSet	= IntStream.range(0, 12345).mapToObj(i -> String.valueOf(Math.random())).collect(Collectors.toSet());
	private final Multimap<String, Integer> demoMultimap;

	TestData() {
		this.demoMultimap = ArrayListMultimap.create();
		demoMultimap.put("A", 1);
		demoMultimap.put("A", 2);
		demoMultimap.put("B", 3);
		demoMultimap.put("C", 4);
		demoMultimap.put("C", 5);
		demoMultimap.put("C", 6);
	}
}
