package com.bavelsoft.broccolies.bank;

import org.junit.Before;
import org.junit.Test;
import com.bavelsoft.broccolies.annotation.FluentExpecter;
import com.bavelsoft.broccolies.annotation.FluentSender;
import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.reg.J4RegBase;

@FluentActor("Banker")
public class SimpleTest extends J4RegBase {
	static Banker banker;

	@FluentSender(Deposit.class)
	@FluentExpecter(Receipt.class)
	@Before
	public void setup() {
		banker = new Banker();
		Bank bank = new Bank();
		bank.setPublisher(r->banker.fromSystemUnderTest(r));
		banker._DepositToSystemUnderTest = bank::handleDeposit;
	}

	@Test
	public void myTest() {
		banker.sendDeposit().depositQty(10);
		banker.sendDeposit().depositQty(10);
		banker.expectReceipt().totalQty(20);
	}

	@Test
	public void myExplicitTest() {
		banker.sendDeposit().depositQty(10).send();
		banker.sendDeposit().depositQty(10).send();
		banker.expectReceipt().totalQty(20).expect();
	}
}
