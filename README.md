This project supports an internal DSL for testing message-based or event-based
Java applications. You can write acceptance tests using fluent builders,
in combination with e.g. junit and mockito.

[Example usage](src/test/java/com/bavelsoft/broccolies/bank/SimpleTest.java):

```
@Test
public void myTest() {
	banker.sendDeposit().depositQty(10);
	banker.sendDeposit().depositQty(10);
	banker.expectReceipt().totalQty(20);
}
```

It generates three kinds of classes:

* fluent builders for populating objects to send to your system-under-test
* fluent builders for matching objects sent from your system-under-test
* actors that return the above

You can either wire up this project to exchange real messages with your app,
or you can use mockito to fake your app's message send and receive to connect
directly to the fluent api in memory.

There's some basic [HOWTO documentation](doc/HOWTO.md) and [design documentation](doc/DESIGN.md).
