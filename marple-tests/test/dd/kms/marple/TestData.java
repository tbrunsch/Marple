package dd.kms.marple;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class TestData
{
	private static Stream<Double> randomStream(int size) {
		Random generator = new Random(1234567890L);
		return IntStream.range(0, size).mapToObj(i -> generator.nextDouble());
	}

	private final List<Double>				demoList		= randomStream(12345).collect(Collectors.toList());
	private final double[]					demoArray 		= randomStream(12345).mapToDouble(d -> d).toArray();
	private final Map<Double, String>		demoMap			= randomStream(32).collect(Collectors.toMap(d -> d, d -> d.toString()));
	private final Map<Double, String>		largeDemoMap	= randomStream(12345).collect(Collectors.toMap(d -> d, d -> d.toString()));
	private final Set<String>				demoSet			= randomStream(32).map(String::valueOf).collect(Collectors.toSet());
	private final Set<String>				largeDemoSet	= randomStream(12345).map(String::valueOf).collect(Collectors.toSet());
	private final Object					testObject		= demoList;
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
