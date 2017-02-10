package com.bavelsoft.broccolies;

import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.annotation.FluentExpecter;
import com.bavelsoft.broccolies.annotation.FluentSender;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

@FluentActor("OverrideActor")
public class OverrideTest {

    @FluentSender(Event.class)
    @FluentExpecter(Event.class)
    public void setUp() {
    }


    @Test
    public void testSender() {
        List<Event> events = Lists.newArrayList();
        OverrideTest_EventSender sender = new OverrideTest_EventSender(events::add, () -> {}, null);
        sender.num(1).str("str").send();
        new OverrideTest_EventExpecter(events).str("str").num(1).expect();
    }

    static class Event {
        private int num;
        private String str;

        // ignore final fields
        public final long longField = 1L;

        // should be added in EventSender
        public void setNum(int num) {
            this.num = num;
        }
        // should be added in EventSender
        public final void setStr(String str) {
            this.str = str;
        }

        // should skip because no parameters
        public void noArgsMethod() {

        }

        // should skip because static
        public static void staticMethod() {

        }

        // should skip because private
        private void privateMethod(){

        }

        public int getNum() {
            return num;
        }

        public String getStr() {
            return str;
        }

        public long getLongField() {
            return longField;
        }
    }
}