package com.bavelsoft.broccolies;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;

import static org.junit.Assert.assertEquals;

import com.bavelsoft.broccolies.annotation.FluentScenario;
import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.ref.OrderReference;
import com.bavelsoft.broccolies.ref.Client;
import com.bavelsoft.broccolies.ref.ReferenceTest;

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
		public com.bavelsoft.broccolies.ref.Client withClient;
	}

	@Test
	public void newTest() {
		given.sendExampleScenario().withClient(client).initially(100).then(200).send();
		assertTrue(true);
	}
}
