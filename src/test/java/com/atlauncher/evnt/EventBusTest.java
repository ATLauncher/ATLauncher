/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.evnt;

import com.atlauncher.OrderedRunner;
import com.atlauncher.anno.ExecutionOrder;
import com.atlauncher.annot.Subscribe;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(OrderedRunner.class)
public class EventBusTest {
    private static EventBus bus;
    private static TestHandler handler;

    @BeforeClass
    public static void init() {
        bus = new EventBus();
        handler = new TestHandler();
    }

    @AfterClass
    public static void pause() throws InterruptedException {
    }

    @Test
    @ExecutionOrder(2)
    public void testPublish() throws Exception {
        bus.publish(new TestEvent("Hello World"));
    }

    @Test
    @ExecutionOrder(3)
    public void testUnsubscribe() throws Exception {
        bus.unsubscribe(handler);
    }

    @Test
    @ExecutionOrder(1)
    public void testSubscribe() throws Exception {
        bus.subscribe(handler);
        bus.subscribe(handler);
    }

    private static final class TestHandler {
        @Subscribe
        public void onTest(TestEvent e) {
            System.out.println(e.message);
        }
    }

    private final class TestEvent {
        public final String message;

        private TestEvent(String message) {
            this.message = message;
        }
    }
}