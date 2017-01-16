package ref;

import org.junit.Test;
import org.junit.BeforeClass;
import java.util.Collection;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import broccolies.annotation.FluentExpecter;
import broccolies.annotation.FluentSender;
import broccolies.annotation.FluentActor;

@FluentActor(value="Client", reference=OrderReference.class)
public class ReferenceTest {
	public static Client client;

	@FluentSender(value=NewOrderSingle.class, reference=OrderReference.class)
	@FluentSender(value=OrderCancelReplaceRequest.class, reference=OrderReference.class)
	@FluentExpecter(value=ExecutionReport.class, reference=OrderReference.class)
	@BeforeClass
	public static void setup() {
		client = new Client();
		Dealer dealer = new Dealer();
		dealer.setPublisher(r->client.fromSystemUnderTest(r));
		client._NewOrderSingleToSystemUnderTest = dealer::handleNewOrderSingle;
		client._OrderCancelReplaceRequestToSystemUnderTest = dealer::handleOrderCancelReplaceRequest;
	}

	@Test
	public void newTest() {
		OrderReference o = new OrderReference();
		client.sendNewOrderSingle().orderQty(1).reference(o).send();
		client.expectExecutionReport().orderQty(1).expect();
		System.err.println(o.orderID);
	}

	@Test
	public void replaceTest() {
		OrderReference o = new OrderReference();
		client.sendNewOrderSingle().orderQty(1).reference(o).send();
		client.sendOrderCancelReplaceRequest().orderQty(2).reference(o).send();
		client.expectExecutionReport().orderQty(2).expect();
	}
}
