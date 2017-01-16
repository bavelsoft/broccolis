import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import broccolies.annotation.FluentExpecter;
import broccolies.annotation.FluentSender;
import broccolies.annotation.FluentNestedSender;
import broccolies.annotation.FluentActor;

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
