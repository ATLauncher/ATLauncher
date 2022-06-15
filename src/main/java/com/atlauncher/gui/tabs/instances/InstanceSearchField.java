package com.atlauncher.gui.tabs.instances;

import com.atlauncher.AppEventBus;
import com.atlauncher.gui.components.search.SearchField;

public final class InstanceSearchField extends SearchField<InstanceSearchEvent> {
    public InstanceSearchField(){
        super();
        AppEventBus.registerToUIOnly(this);
    }

    @Override
    protected InstanceSearchEvent createSearchEvent(){
        return InstanceSearchEvent.forSearchField(this);
    }
}