/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
    },
    BY_NUMBER_OF_PLAYS_ASC("By Number of Plays (Asc)"){
        @Override
        public int compare(Instance lhs, Instance rhs){
            if(lhs.getNumberOfPlays() < rhs.getNumberOfPlays()){
                return -1;
            } else if(lhs.getNumberOfPlays() > rhs.getNumberOfPlays()){
                return +1;
            }
            return 0;
        }
    },
    BY_NUMBER_OF_PLAYS("By Number of Plays (Desc)"){
        @Override
        public int compare(Instance lhs, Instance rhs){
            if(lhs.getNumberOfPlays() > rhs.getNumberOfPlays()){
                return -1;
            } else if(lhs.getNumberOfPlays() < rhs.getNumberOfPlays()){
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