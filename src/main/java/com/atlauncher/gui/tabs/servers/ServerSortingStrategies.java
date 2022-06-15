package com.atlauncher.gui.tabs.servers;

import com.atlauncher.AppEventBus;
import com.atlauncher.data.Server;
import org.mini2Dx.gettext.GetText;

public enum ServerSortingStrategies implements ServerSortingStrategy{
    BY_NAME(GetText.tr("By Name")) {
        @Override
        public int compare(final Server lhs, final Server rhs) {
            return lhs.name.compareToIgnoreCase(rhs.name);
        }
    };

    private final String name;

    ServerSortingStrategies(final String name){
        this.name = name;
        AppEventBus.register(this);
    }

    @Override
    public final String getName() {
        return this.name;
    }
}