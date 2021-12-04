package com.atlauncher.gui.tabs.instances;

import java.util.EventObject;
import java.util.regex.Pattern;

public final class InstancesSearchEvent extends EventObject{
    private final Pattern searchPattern;

    public InstancesSearchEvent(final Object source, final Pattern searchPattern){
        super(source);
        this.searchPattern = searchPattern;
    }

    public Pattern getSearchPattern(){
        return this.searchPattern;
    }
}
