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

import java.time.OffsetDateTime;
import java.util.Date;

import com.apollographql.apollo.api.CustomTypeAdapter;
import com.apollographql.apollo.api.CustomTypeValue;
import com.apollographql.apollo.api.CustomTypeValue.GraphQLString;

/** Handles ISO timestamps with optional fractional seconds without a shared mutable formatter. */
final class GraphqlDateTimeAdapter implements CustomTypeAdapter<Date> {
    @Override
    public Date decode(CustomTypeValue<?> value) {
        return Date.from(OffsetDateTime.parse(value.value.toString()).toInstant());
    }

    @Override
    public CustomTypeValue<?> encode(Date value) {
        return new GraphQLString(value.toInstant().toString());
    }
}
