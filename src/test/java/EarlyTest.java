import com.google.auto.value.AutoValue;
import org.junit.Test;
import java.util.Collection;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import broccolies.annotation.FluentExpecter;
import broccolies.annotation.FluentSender;
import broccolies.annotation.FluentSenders;

public class EarlyTest {
	@FluentSender(value=Animal.Writable.class, initializer="new AutoValue_EarlyTest_Animal.Builder()/*$T*/") Object x;
	@FluentExpecter(Animal.class) Object y;

	@AutoValue
	static abstract class Animal {
		abstract String getName();
		abstract int getNumberOfLegs();

		//this could be a java bean with setters...
		//just using autovalue's builder for testing convenience
		@AutoValue.Builder
		abstract static class Writable {
			abstract Writable setName(String value);
			abstract Writable setNumberOfLegs(int value);
			abstract Animal build();
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
