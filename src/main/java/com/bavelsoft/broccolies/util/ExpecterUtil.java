package com.bavelsoft.broccolies.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Comparator.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

public class ExpecterUtil {
	public static <T> void match(Collection<Predicate<T>> conditions, Collection<T> objects) {
		Map<T, Long> mismatchCounts = new IdentityHashMap<>();
		for (T o : objects) {
			mismatchCounts.put(o, getMismatches(conditions, o));
		}
		if (mismatchCounts.values().stream().allMatch(x->x>0)) {
			Stream<Map.Entry<T, Long>> stream = mismatchCounts.entrySet().stream();
			T best = stream.sorted(comparingByValue(reverseOrder())).findFirst().get().getKey();
			throw new AssertionError("no match, best candidate was "+best.toString());

			//TODO prefer first conditions from the test
			//TODO report the condition that didn't match
			//TODO report all the conditions
		}
	}

	public static <T> T matchOne(Collection<Predicate<T>> conditions, Collection<T> objects) {
		Set<T> matches = new HashSet<>();
		for (T o : objects) {
			if (getMismatches(conditions, o) == 0) {
				matches.add(o);
			}
		}
		if (matches.size() != 0) {
			throw new AssertionError("not one match :"+matches);
		}
		return matches.iterator().next();
	}
	
	public static <T> long getMismatches(Collection<Predicate<T>> conditions, T o) {
		return conditions.stream().filter(x->!x.test(o)).count();
	}
}
