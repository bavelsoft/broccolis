package com.bavelsoft.broccolies;

import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.annotation.FluentNestedSender;
import com.bavelsoft.broccolies.annotation.FluentSender;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@FluentActor("Actor")
public class NestedTest {
	static Outer outer;

	@FluentSender(Outer.class)
	@FluentNestedSender(value=Inner.class, containers={Outer.class})
	@BeforeClass
	public static void setup() {
		outer = new Outer();
	}

	static class Inner {
		public int z;
	}

	static class Outer {
		Inner inner;	
		int y;

		public void setY(int y) {
			this.y = y;
		}

		public void setInner(Inner inner) {
			this.inner = inner;
		}
	}

	class OuterHolder { Outer value; }

	@Test
	public void newTest() {
		Actor actor = new Actor();
		OuterHolder holder = new OuterHolder();
		actor._NestedTest_OuterToSystemUnderTest = x->{holder.value = x;};
		actor.sendOuter().inner().z(123).y(456).send();
		assertTrue(holder.value.inner.z == 123);
	}
}
