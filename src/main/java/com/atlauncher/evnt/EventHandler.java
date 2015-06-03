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

import com.atlauncher.App;

public final class EventHandler {
    public static final EventBus EVENT_BUS = new EventBus();

    public static <T extends Event> Event get(Class<T> tClass) {
        return App.INJECTOR.getInstance(tClass);
    }

    private interface Event {
    }

    // Changes
    public static final class AccountsChangeEvent implements Event {
    }

    public static final class InstancesChangeEvent implements Event {
    }

    public static final class SettingsChangeEvent implements Event {
    }

    public static final class PacksChangeEvent implements Event {
        public final boolean reload;

        public PacksChangeEvent(boolean reload) {
            this.reload = reload;
        }
    }

    // Console
    public static final class ConsoleCloseEvent implements Event {
    }

    public static final class ConsoleOpenEvent implements Event {
    }

    // Misc
    public static final class TabChangeEvent implements Event {
    }

    public static final class RelocalizationEvent implements Event {
    }
}