/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2026 ATLauncher
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
package com.atlauncher.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.apollographql.apollo.api.CustomTypeValue.GraphQLString;

class GraphqlDateTimeAdapterTest {
    private final GraphqlDateTimeAdapter adapter = new GraphqlDateTimeAdapter();

    @Test
    void decodesNewsTimestampsWithoutMilliseconds() {
        assertEquals(Date.from(Instant.parse("2024-11-15T12:55:33Z")),
            adapter.decode(new GraphQLString("2024-11-15T12:55:33Z")));
    }

    @Test
    void acceptsFractionalSecondsAndTimezoneOffsets() {
        Date expected = Date.from(Instant.parse("2026-11-03T14:00:00.123Z"));
        assertEquals(expected, adapter.decode(new GraphQLString("2026-11-03T14:00:00.123Z")));
        assertEquals(expected, adapter.decode(new GraphQLString("2026-11-04T01:00:00.123+11:00")));
    }

    @Test
    void encodesInstantsAsUtcAndRejectsInvalidDates() {
        Date value = Date.from(Instant.parse("2026-11-03T14:00:00Z"));
        assertEquals("2026-11-03T14:00:00Z", adapter.encode(value).value);
        assertThrows(DateTimeParseException.class, () -> adapter.decode(new GraphQLString("not-a-date")));
    }
}
