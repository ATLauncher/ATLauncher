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

import com.atlauncher.MemorySpy;
import com.atlauncher.annot.Subscribe;
import org.junit.Test;

public class EventHandlerTest {
    @Test
    public void testGet() throws Exception {
        MemorySpy memorySpy = new MemorySpy();
        EventHandler.EVENT_BUS.subscribe(new Handler());
        System.out.println(memorySpy.used() + "b");
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.ConsoleCloseEvent.class));
        System.out.println(memorySpy.used() + "b");
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.ConsoleOpenEvent.class));
        System.out.println(memorySpy.used() + "b");
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.ConsoleCloseEvent.class));
        System.out.println(memorySpy.used() + "b");
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.ConsoleOpenEvent.class));
        System.out.println(memorySpy.used() + "b");
    }

    private final class Handler {
        @Subscribe
        private void onConsoleOpen(EventHandler.ConsoleOpenEvent e) {
            System.out.println("Console Opened");
        }

        @Subscribe
        private void onConsoleClosed(EventHandler.ConsoleCloseEvent e) {
            System.out.println("Console Closed");
        }
    }
}