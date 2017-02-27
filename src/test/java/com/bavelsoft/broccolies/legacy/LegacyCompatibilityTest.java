package com.bavelsoft.broccolies.legacy;


import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.annotation.FluentExpecter;
import com.bavelsoft.broccolies.annotation.FluentNestedSender;
import com.bavelsoft.broccolies.annotation.FluentSender;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@FluentActor(value = "LegacyActor", legacyCompatible = true)
public class LegacyCompatibilityTest {

    @FluentSender(value = Request.class)
    @FluentNestedSender(value = Header.class, containers = Request.class)
    @FluentExpecter(value = Response.class)
    public void setUp() {

    }

    @Test
    public void test() {
        // given
        LegacyActor legacyActor = new LegacyActor();
        Consumer<Request> mockConsumer = mock(Consumer.class);
        ArgumentCaptor<Request> argument = ArgumentCaptor.forClass(Request.class);
        legacyActor._RequestToSystemUnderTest = mockConsumer;

        RequestSender requestSender = legacyActor.sendRequest();
        requestSender = requestSender.id("1");
        HeaderSender headerSender = requestSender.header();
        headerSender = headerSender.senderId("2");
        requestSender = headerSender.back();

        // when
        requestSender.send();

        // then
        verify(mockConsumer).accept(argument.capture());
        Request actualMsg = argument.getValue();
        assertNotNull(actualMsg);
        assertEquals("1", actualMsg.getId());
        assertNotNull(actualMsg.getHeader());
        assertEquals("2", actualMsg.getHeader().getSenderId());

    }

}
