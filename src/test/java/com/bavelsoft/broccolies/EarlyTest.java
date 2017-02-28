package com.bavelsoft.broccolies;

import com.bavelsoft.broccolies.annotation.FluentExpecter;
import com.bavelsoft.broccolies.annotation.FluentSender;
import com.google.auto.value.AutoValue;
import org.junit.Test;

import java.util.ArrayList;

public class EarlyTest {
	@FluentSender(value=Animal.Writable.class, initializer="new AutoValue_EarlyTest_Animal.Builder()/*$T*/") void f() {}
	@FluentExpecter(Animal.class) void g() {}

	@AutoValue
	static abstract class Animal {
		public abstract String getName();
		public abstract int getNumberOfLegs();

		//this could be a java bean with setters...
		//just using autovalue's builder for testing convenience
		@AutoValue.Builder
		abstract static class Writable {
			public abstract Writable setName(String value);
			public abstract Writable setNumberOfLegs(int value);
			public abstract Animal build();
		}
	}

	@Test
	public void testMatch() {
		ArrayList list = new ArrayList();
		new EarlyTest_Animal_WritableSender(x->{ list.add(x.build());}, ()->{;}, null)
			.name("test").numberOfLegs(2).send();
		new EarlyTest_AnimalExpecter(list)
			.name("test").numberOfLegs(2).expect();
	}

	@Test(expected=AssertionError.class)
	public void testMismatch() {
		ArrayList list = new ArrayList();
		new EarlyTest_Animal_WritableSender(x->{ list.add(x.build());}, ()->{;}, null)
			.name("test").numberOfLegs(2).send();
		new EarlyTest_AnimalExpecter(list)
			.name("test").numberOfLegs(1).expect();
	}

}
