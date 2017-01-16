# Main Motivation

A testing DSL helps writing tests that are specifications, not scripts. An _internal_ DSL allows using the same great tooling for tests that we use for java code.

One big advantage of the fluent API is that it's discoverable;
test writers can always use tab completion to discover what can be tested and how.

Generally, you shouldn't have to make any changes to your test wiring in order
to add support for new fields, and you should be able to change a single
piece of code in order to add support for new messages.

# Other Details

## Code Generation

There are a few advantages to generating actual java source code (at build time),
instead of generating byte code or using reflection:

* The user can easily understand and debug the tests.
* The user can extend the generated classes if necessary.
* The framework can't accidentally get too dynamic.

## Annotation Processing

Java annotation processing is a nice way to kick off code code generation, because:

* It integrates well with other java code.
* It's part of the standard of Java platform,
    * and therefore has good tooling support, e.g. in Intellij IDEA.

The annotations in this project intentionally don't derive any information
from the program element which they annotate; this is useful because:

* we don't always have the ability to make changes (e.g. annotate) to our message classes, and
* we can use the flexibility to help keep all of the code for handling a new message class in one place.

## Miscellaneous

Tests should generally use only literals, and not logic.

Every "send" clears out the messages queued from the system-under-test.
This discourages test writers from making assertions regarding conditions that were caused by
messages sent much earlier in the test.

Negative tests are brittle, and are not supported.

# Unit testing

We generally use something like junit or testng for automatically running our tests,
but they are _not_ technically unit tests. Unit tests should test a single class,
and fluent tests should test as much of the system as can be dynamically allocated,
and mock out only clunkier resources like a relational database.


