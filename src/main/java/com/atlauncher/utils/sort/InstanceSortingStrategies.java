/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.strings.Noun;
import com.atlauncher.strings.Sentence;
import com.atlauncher.strings.Verb;

public enum InstanceSortingStrategies implements InstanceSortingStrategy, RelocalizationListener {
    BY_NAME {
        @Override
        public int compare(Instance lhs, Instance rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }

        @Override
        public String getName() {
            return Sentence.PRT_BY_X.capitalize()
                .insert(Noun.NAME)
                .toString();
        }
    },
    BY_LAST_PLAYED {
        @Override
        public int compare(Instance lhs, Instance rhs) {
            long lhsEpoch = lhs.getLastPlayedOrEpoch().toEpochMilli();
            long rhsEpoch = rhs.getLastPlayedOrEpoch().toEpochMilli();
            if (lhsEpoch > rhsEpoch) {
                return -1;
            } else if (lhsEpoch < rhsEpoch) {
                return +1;
            }
            return 0;
        }

        @Override
        public String getName() {
            return Sentence.PRT_BY_X.capitalize()
                .insert(Sentence.PRT_LAST_X.insert(Verb.PLAY, Verb.PAST))
                .toString();
        }
    },
    BY_NUMBER_OF_PLAYS {
        @Override
        public int compare(Instance lhs, Instance rhs) {
            if (lhs.getNumberOfPlays() > rhs.getNumberOfPlays()) {
                return -1;
            } else if (lhs.getNumberOfPlays() < rhs.getNumberOfPlays()) {
                return +1;
            }
            return 0;
        }

        @Override
        public String getName() {
            return Sentence.PRT_BY_X.capitalize()
                .insert(Noun.PLAYCOUNT)
                .toString();
        }
    };

    InstanceSortingStrategies() {
        RelocalizationManager.addListener(this);
    }

    @Override
    public String getName() {
        // should be implemented by each enum constant
        throw new AbstractMethodError();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void onRelocalization() {
        // do nothing
    }
}
