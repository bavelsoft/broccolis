package com.bavelsoft.broccolies.util;

public class LastRunnable {
	private static Runnable last;

	public static void set(Runnable next) {
		if (last != null) {
			last.run();
		}
		last = next;
	}

	public static void unset() {
		last = null;
	}
}
