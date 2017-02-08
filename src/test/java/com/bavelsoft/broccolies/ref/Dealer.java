package com.bavelsoft.broccolies.ref;

import java.util.UUID;

public class Dealer {
	Publisher publisher;

	public void handleNewOrderSingle(NewOrderSingle newOrderSingle) {
		ExecutionReport executionReport = new ExecutionReport();
		executionReport.setClOrdID(newOrderSingle.getClOrdID());
		executionReport.setOrderID(UUID.randomUUID().hashCode());
		executionReport.setOrderQty(newOrderSingle.getOrderQty());
		publisher.publish(executionReport);
	}

	public void handleOrderCancelReplaceRequest(OrderCancelReplaceRequest request) {
		ExecutionReport executionReport = new ExecutionReport();
		executionReport.setClOrdID(request.getClOrdID());
		executionReport.setOrderID(request.getOrderID());
		executionReport.setOrderQty(request.getOrderQty());
		publisher.publish(executionReport);
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public interface Publisher {
		void publish(ExecutionReport receipt);
	}
}
