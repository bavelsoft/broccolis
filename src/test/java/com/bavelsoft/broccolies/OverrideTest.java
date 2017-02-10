package com.bavelsoft.broccolies;

import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.annotation.FluentSender;
import org.junit.Test;

@FluentActor("OverrideActor")
public class OverrideTest {

    @FluentSender(Event.class)
    public void setUp() {
    }


    @Test
    public void testSender() {
        OverrideTest_EventSender sender = new OverrideTest_EventSender(x -> {}, () -> {}, null);
        sender.num(1).str("str").send();
    }

    static class Event {
        private int num;
        private String str;

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

    }
}