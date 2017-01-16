import org.junit.Test;
import org.junit.Before;
import broccolies.annotation.FluentExpecter;
import broccolies.annotation.FluentSender;
import broccolies.annotation.FluentActor;
import bank.Bank;

@FluentActor("Banker")
public class OnlyLastOfTest {
	static Banker banker;

	public static class Deposit extends bank.Deposit {}
	public static class Receipt extends bank.Receipt {
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
