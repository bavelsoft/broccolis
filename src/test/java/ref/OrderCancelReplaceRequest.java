package ref;

public class OrderCancelReplaceRequest {
	String clOrdID;
	int orderQty;
	int orderID;

	public String getClOrdID() {
		return clOrdID;
	}

	public void setClOrdID(String clOrdID) {
		this.clOrdID = clOrdID;
	}

	public int getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(int orderQty) {
		this.orderQty = orderQty;
	}

	public int getOrderID() {
		return orderID;
	}

	public void setOrderID(int orderID) {
		this.orderID = orderID;
	}
}
