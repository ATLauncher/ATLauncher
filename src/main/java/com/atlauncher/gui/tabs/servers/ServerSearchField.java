package com.atlauncher.gui.tabs.servers;

import com.atlauncher.AppEventBus;
import com.atlauncher.gui.components.search.SearchField;

public final class ServerSearchField extends SearchField<ServerSearchEvent>{
    public ServerSearchField(){
        super();
        AppEventBus.registerToUIOnly(this);
    }

    @Override
    protected ServerSearchEvent createSearchEvent(){
        return ServerSearchEvent.forSearchField(this);
    }
}