# Initial Setup

The fluent jar should be in your classpath,
or its project should be listed as one of your build dependencies.

If you are already explicitly specifying other annotation processors,
e.g. with javac -processor or maven \<annotationProcessors>,
then you also need to explicitly specify the fluent annotation processor,
com.bavelsoft.broccolies.FluentProcessor.

# Actor

First, choose an "actor" to be generated, which represents a user of the system to be tested,
whether that user is some kind of actual person, or another system.

For example, if you are testing travel agency software,
one actor for your tests might be a customer and another might be an airline system.

Create a class which will wire up your test actor, and annotate it with @FluentActor
and the name of your test actor, e.g.
```
@FluentActor("Customer")
public class CustomerTestWiring {
 ...
}
```

# Sender

Actors send messages in to the system-under-test,
and receive mesages sent out from the system-under-test.

Choose the existing class of a message that that your system can receive,
and that you'd like to send from a test.

In the simplest case, your message objects are java beans with getters and setters,
but the framework can handle flexibly populating and matching against any
message object methods which accept zero or one parameter respectively.

Inside the actor wiring class that you created for the previous section,
create an annotation using @FluentSender and the class of the message that you're sending, e.g.
```
@FluentActor("Customer")
public class CustomerTestWiring {

 @FluentSender(ReserveFlightCommand.class)
 public void flightWiring() {}
 ...
}
```

The class and methods you annotate don't matter so much,
but the @FluentSender annotated methods must be members of the @FluentActor annotated class,
and it's best to annotate methods that contain the corresponding initialization.

After you do this and build your project,
you should then see a generated class for your actor,
with a method to fluently send the message, e.g.
```
$ cat target/generated-sources/annotations/Customer.java
...
public class Customer {
...
	ReserveFlightCommandSender sendReserveFlightCommand() {
		...
	}
}
```

# Sender wiring

Now let's wire up the "send" method;
in the actor wiring class that you created above,
add code to actually get the message in to the system-under-test
and make sure this code gets called before you run your test
(e.g. call it from junit5 @BeforeAll annotated method).

Do this by initializing a java8 method reference on the actor,
a field named with a leading underscore and a "ToSystemUnderTest" suffix, e.g.
```
	customer = new Customer();
	customer._ReserveFlightCommandToSystemUnderTest = () -> { ... };
```

The "..." can either involve sending the message using your real messaging software,
or it can instead involve dropping a message directly into your application context.
For example, if your application has "handleXXX()" methods that are called by the
messaging framework, you could call these methods directly, e.g.

```
	customer._ReserveFlightCommandToSystemUnderTest = appInstance::handleReservation;
```

# Expecter

Now let's wire up an "expect" method, which works like a traditional "assert" method.
The call to the expect method asserts that the system-under-test has emitted a message
that has fields matching each of the values specified in the chain of fluent parameters.
If there is no matching message, the test will fail.

Just like for the Sender above,
create an annotation for an Expecter for a message,
using @FluentExpecter and the class, e.g.
```
 ...
 @FluentExpecter(ReservationReceipt.class)
 public CustomerWiring() {}
```

and create wiring such that messages emitted by the system-under-test
are passed to a method "fromSystemUnderTest()" on the fluent actor. E.g.
```
	appInstance.onReservationReceipt(message->customer.fromSystemUnderTest(message));
```

Just as for the sender, you can either
receive the message using your real messaging software,
or you can mock out the interfaces that your application calls to have
it publish messages, using e.g. mockito.

# Running a test

Use the fluent actor instance that you've set up above to write fluent tests!

E.g.
```
	Customer customer;

	@Test
	public void testSimple() {
		customer.sendReserveFlightCommand().destination("New York").budget(10000).send();
		customer.expectReservationReceipt().hotel("Hilton").expect();
	}
```

# More to come...

You can always consult the automated tests for _this_ project to see examples of how to use it.
Use that to learn how to use any other features which haven't been documented yet here...

* [nested object sends](../src/test/java//com/bavelsoft/broccolies/NestedTest.java)
* [references across events](../src/test/java/com/bavelsoft/broccolies/RefTest.java)
* [scenarios for succintly sending multiple events](../src/test/java/com/bavelsoft/broccolies/ScenarioTest.java)
* [initializers for non-default constructors](../src/test/java/com/bavelsoft/broccolies/EarlyTest.java)
* [only the last relevant message subject to expectations](../src/test/java/com/bavelsoft/broccolies/OnlyLastOfTest.java)
* [overriding the name of the actor methods](../src/test/java/com/bavelsoft/broccolies/OverrideTest.java)
* [easily calling all init methods](../src/test/java/com/bavelsoft/broccolies/InitTest.java)
