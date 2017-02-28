package com.bavelsoft.broccolies;

import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.annotation.FluentNestedSender;
import com.bavelsoft.broccolies.annotation.FluentSender;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class InitTest {
	static PerMethodActor actor;
	static int x;

	@BeforeClass
	public static void setup() {
		actor = new PerMethodActor();
		PerMethodActor.initialize(new Senders(), "foo");
	}

	@FluentActor(value="PerMethodActor")
	static class Senders {
		@FluentSender(MySender.class)
		public void init1(String foo) {
			actor._InitTest_MySenderToSystemUnderTest = s->{x = s.x;};
		}

		@FluentSender(MyOtherSender.class)
		public void init2(String foo) {
			actor._InitTest_MyOtherSenderToSystemUnderTest = s->{x = s.x;};
		}
	}

	static class MySender {
		public int x;
	}

	static class MyOtherSender {
		public int x;
	}

	@Test
	public void newTest() {
		actor.sendMySender().x(123).send();
		assertTrue(x == 123);
	}
}
