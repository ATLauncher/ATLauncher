package com.atlauncher.gui.tabs.instances;

import com.atlauncher.utils.sort.InstanceSortingStrategy;

import java.util.EventObject;

public final class InstancesSortEvent extends EventObject{
    private final InstanceSortingStrategy strategy;

    public InstancesSortEvent(final Object source, final InstanceSortingStrategy strategy){
        super(source);
        this.strategy = strategy;
    }

    public InstanceSortingStrategy getStrategy(){
        return this.strategy;
    }
}