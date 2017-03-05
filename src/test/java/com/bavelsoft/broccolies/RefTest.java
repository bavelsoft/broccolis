package com.bavelsoft.broccolies;

import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.annotation.FluentKey;
import com.bavelsoft.broccolies.annotation.FluentNestedSender;
import com.bavelsoft.broccolies.annotation.FluentSender;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@FluentActor(value="RefTestActor")
public class RefTest {
	static MySender sender;
	static RefTestActor actor;
	static int x, y;

	@FluentSender(value=MySender.class, reference=MyReference.class)
	@BeforeClass
	public static void setup() {
		sender = new MySender();
		actor = new RefTestActor();
		actor._RefTest_MySenderToSystemUnderTest = s->{x=s.x; y=s.y;};
	}

	@org.junit.After public void clearReference() { actor.clearReferences(); }

	static class MySender {
		int x;
		int y;

		public void setX(int x) { this.x = x; }
		public void setY(int y) { this.y = y; }
	}

	static class MyReference {
		@FluentKey public int x = 123;
		int y;
		MyReference() { y=1; }
		MyReference(MyReference prev) { y=prev.y+1; }
	}

	@Test
	public void simpleReference() {
		actor.sendMySender().send();
		assertEquals(123, x);
		assertEquals(1, y);
	}

	@Test
	public void simpleReferenceAcrossTests() {
		actor.sendMySender().send();
		assertEquals(123, x);
		assertEquals(1, y);
	}

	@Test
	public void chainedReference() {
		actor.sendMySender().send();
		actor.sendMySender().send();
		assertEquals(2, y);
	}
}
