package dd.kms.marple;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class TestData
{
	private static Stream<Double> randomStream(int size) {
		Random generator = new Random(1234567890L);
		return IntStream.range(0, size).mapToObj(i -> generator.nextDouble());
	}

	public static int getPublicStaticInt() {
		return PUBLIC_STATIC_INT;
	}

	protected static int getProtectedStaticInt() {
		return PROTECTED_STATIC_INT;
	}

	static int getPackagePrivateStaticInt() {
		return PACKAGE_PRIVATE_STATIC_INT;
	}

	private static int getPrivateStaticInt() {
		return PRIVATE_STATIC_INT;
	}

	public static final int		PUBLIC_STATIC_INT			= 1;
	protected static final int	PROTECTED_STATIC_INT		= 2;
	static final int			PACKAGE_PRIVATE_STATIC_INT	= 3;
	private static final int	PRIVATE_STATIC_INT			= 4;

	public final int			publicInt					= 5;
	protected final int			protectedInt				= 6;
	final int					packagePrivateInt			= 7;
	private final int			privateInt					= 8;

	private final List<Double>				demoList		= randomStream(12345).collect(Collectors.toList());
	private final double[]					demoArray 		= randomStream(12345).mapToDouble(d -> d).toArray();
	private final Iterable<Integer>			demoIterable	= new InfiniteIterable();
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

	public int getPublicInt() {
		return publicInt;
	}

	protected int getProtectedInt() {
		return protectedInt;
	}

	int getPackagePrivateInt() {
		return packagePrivateInt;
	}

	private int getPrivateInt() {
		return privateInt;
	}

	private static class InfiniteIterable implements Iterable<Integer>
	{
		@Override
		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {
				private int value = 0;

				@Override
				public boolean hasNext() {
					return true;
				}

				@Override
				public Integer next() {
					return ++value;
				}
			};
		}
	}
}
