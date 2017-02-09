package com.bavelsoft.broccolies;

import com.bavelsoft.broccolies.annotation.FluentActor;
import com.bavelsoft.broccolies.annotation.FluentSender;

// todo fix
//@FluentActor("OverrideActor")
public class OverrideTest {

    //@FluentSender(Event.class)
    public void setUp() {
    }


    static class Event {
        private int num;

        // should be added in EventSender
        public void setNum(int num) {
            this.num = num;
        }

        // should skip because no parameters
        public void noArgsMethod() {

        }

        // should skip because static
        public static void staticMethod() {

        }

        // should skip because final
        public final void finalMethod() {

        }

        // should skip because private
        private void privateMethod(){

        }

    }
}