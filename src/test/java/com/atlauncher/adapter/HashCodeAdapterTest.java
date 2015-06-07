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
package com.atlauncher.adapter;

import com.atlauncher.Gsons;
import com.atlauncher.utils.Hashing;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStreamReader;

public class HashCodeAdapterTest {
    @Test
    public void test() throws Exception {
        HashCodeTest test = Gsons.DEFAULT.fromJson(new InputStreamReader(System.class.getResourceAsStream("/hashcode" +
                ".json")), HashCodeTest.class);
        Assert.assertEquals(test.hash, Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5"));
        System.out.println(test.hash.toString());
    }

    private static final class HashCodeTest {
        public final Hashing.HashCode hash;

        private HashCodeTest(Hashing.HashCode hash) {
            this.hash = hash;
        }
    }
}