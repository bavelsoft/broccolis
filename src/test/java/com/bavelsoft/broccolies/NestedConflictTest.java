package com.bavelsoft.broccolies;

import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.annotation.FluentNestedSender;
import com.bavelsoft.broccolies.annotation.FluentSender;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@FluentActor("ConflictActor")
public class NestedConflictTest {
	static Outer outer;

	@FluentSender(Outer.class)
	@FluentNestedSender(value=Inner.class, containers={Outer.class})
	@BeforeClass
	public static void setup() {
		outer = new Outer();
	}

	static class Inner {
		int fu;

		public void setFu(int fu) {
			this.fu = fu;
		}
	}

	static class Outer {
		Inner inner;	
		int fu;

		public void setFu(int fu) {
			this.fu = fu;
		}

		public void setInner(Inner inner) {
			this.inner = inner;
		}
	}

	class OuterHolder { Outer value; }

	@Test
	public void newTest() {
		ConflictActor actor = new ConflictActor();
		OuterHolder holder = new OuterHolder();
		actor._NestedConflictTest_OuterToSystemUnderTest = x->{holder.value = x;};
		actor.sendOuter().fu(123).inner().fu(456).send();
		assertTrue(holder.value.fu == 123);
		assertTrue(holder.value.inner.fu == 456);
	}
}
