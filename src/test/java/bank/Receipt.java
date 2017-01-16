package bank;

public class Receipt {
	int totalQty;

	public void setTotalQty(int totalQty) {
		this.totalQty = totalQty;
	}

	public int getTotalQty() {
		return totalQty;
	}

	public String getBar() {
		return null;
	}

	public String toString() {
		return "BankReceipt[totalQty="+totalQty+"]";
	}
}
