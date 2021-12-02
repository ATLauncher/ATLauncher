package com.atlauncher.utils.sort;

import com.atlauncher.data.Instance;

public enum InstanceSortingStrategies implements InstanceSortingStrategy{
    BY_NAME("By Name"){
        @Override
        public int compare(Instance lhs, Instance rhs){
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    },
    BY_LAST_PLAYED_ASC("By Last Played (Asc)"){
        @Override
        public int compare(Instance lhs, Instance rhs){
            long lhsEpoch = lhs.getLastPlayedOrEpoch().toEpochMilli();
            long rhsEpoch = rhs.getLastPlayedOrEpoch().toEpochMilli();
            if(lhsEpoch < rhsEpoch){
                return -1;
            } else if(lhsEpoch > rhsEpoch){
                return +1;
            }
            return 0;
        }
    },
    BY_LAST_PLAYED_DESC("By Last Played (Desc)"){
        @Override
        public int compare(Instance lhs, Instance rhs){
            long lhsEpoch = lhs.getLastPlayedOrEpoch().toEpochMilli();
            long rhsEpoch = rhs.getLastPlayedOrEpoch().toEpochMilli();
            if(lhsEpoch > rhsEpoch){
                return -1;
            } else if(lhsEpoch < rhsEpoch){
                return +1;
            }
            return 0;
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