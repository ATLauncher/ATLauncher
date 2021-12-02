package com.atlauncher.utils.sort;

import com.atlauncher.data.Instance;

public enum InstanceSortingStrategies implements InstanceSortingStrategy{
    BY_NAME("By Name"){
        @Override
        public int compare(Instance lhs, Instance rhs){
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };

    private final String name;

    InstanceSortingStrategies(final String name){
        this.name = name;
    }

    @Override
    public String getName(){
        return this.name;
    }

    @Override
    public String toString(){
        return this.name;
    }
}