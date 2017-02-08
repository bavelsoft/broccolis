package com.bavelsoft.broccolies.ref;

import com.bavelsoft.broccolies.annotation.FluentKey;

public class OrderReference {
	private static int counter;
	@FluentKey String clOrdID = String.valueOf(counter++);
	public int orderID;
}
