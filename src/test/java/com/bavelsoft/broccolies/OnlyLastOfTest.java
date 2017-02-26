package com.bavelsoft.broccolies;

import org.junit.Test;
import org.junit.Before;
import com.bavelsoft.broccolies.annotation.FluentExpecter;
import com.bavelsoft.broccolies.annotation.FluentSender;
import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.bank.Bank;

@FluentActor("Banker")
public class OnlyLastOfTest {
	static Banker banker;

	public static class Deposit extends com.bavelsoft.broccolies.bank.Deposit {}
	public static class Receipt extends com.bavelsoft.broccolies.bank.Receipt {
		public int getId() {
			return 0;
		}
	}

	@FluentSender(Deposit.class)
	@FluentExpecter(value=Receipt.class, onlyLastOf="getId()")
	@Before
	public void setup() {
		banker = new Banker();
		Bank bank = new Bank() {
			@Override protected Receipt createReceipt() {
				return new Receipt();
			}
		};
		bank.setPublisher(r->banker.fromSystemUnderTest((Receipt)r));
		banker._OnlyLastOfTest_DepositToSystemUnderTest = bank::handleDeposit;
	}

	@Test
	public void myTest() {
		banker.sendDeposit().depositQty(10);
		banker.sendDeposit().depositQty(10);
		banker.expectReceipt().totalQty(20);
	}
}
