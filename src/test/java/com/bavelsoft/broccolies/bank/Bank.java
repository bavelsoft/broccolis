package com.bavelsoft.broccolies.bank;

public class Bank {
	int totalQty;
	Publisher publisher;

	public void handleDeposit(Deposit deposit) {
		totalQty += deposit.getDepositQty();
		Receipt receipt = createReceipt();
		receipt.setTotalQty(totalQty);
		publisher.publish(receipt);
	}

	protected Receipt createReceipt() {
		return new Receipt();
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public interface Publisher {
		void publish(Receipt receipt);
	}
}
