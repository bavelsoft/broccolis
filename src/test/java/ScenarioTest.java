import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import java.util.Collection;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import broccolies.annotation.FluentExpecter;
import broccolies.annotation.FluentScenario;
import broccolies.annotation.FluentActor;
import ref.OrderReference;
import ref.NewOrderSingle;
import ref.OrderCancelReplaceRequest;
import ref.ExecutionReport;
import ref.Client;
import ref.Dealer;
import ref.ReferenceTest;
import ref.OrderReference;

@FluentActor("Given")
public class ScenarioTest {
	static Client client;
	static Given given;

	@FluentScenario(ExampleScenario.class)
	@BeforeClass
	public static void setup() {
		ReferenceTest.setup();
		client = ReferenceTest.client;
		given = new Given() {
		  public ScenarioTest_ExampleScenarioSender sendExampleScenario() {
    		    return new ScenarioTest_ExampleScenarioSender() {
		      public void send() {
		        OrderReference o = new OrderReference();
		        underlying.withClient.sendNewOrderSingle().reference(o).orderQty(underlying.initially).send();
		        underlying.withClient.sendOrderCancelReplaceRequest().reference(o).orderQty(underlying.then).send();
		      }
		    };
		  }
		};
	}

	static class ExampleScenario {
		public int initially, then;
	
		/*
		 * Note! References to generated classes in scenarios must be fully qualified.
		 */
		public ref.Client withClient;
	}

	@Test
	public void newTest() {
		given.sendExampleScenario().withClient(client).initially(100).then(200).send();
		assertTrue(true);
	}
}
