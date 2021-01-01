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
package com.atlauncher.data;

/**
 * Response to an API call made to ATLauncher servers. This contains information including if there was an error, the
 * response code of the request, the error message (if any) and the data received (if any) from the API.
 */
public class APIResponse<T> {
    /**
     * If this request is an error or not.
     */
    private boolean error;

    /**
     * The response code returned.
     *
     * @see <a href="http://wiki.atlauncher.com/api:response_code">http://wiki.atlauncher.com/api:response_code</a>
     */
    private int code;

    /**
     * The error message sent back by the API. Returns null if there was no error.
     */
    private String message;

    /**
     * The data sent back by the API (if applicable). Can be of various types and may not be specified at all.
     */
    private T data;

    /**
     * Checks if this response was an error or not.
     *
     * @return if there was an error or not
     */
    public boolean wasError() {
        return this.error;
    }

    /**
     * Gets the response code for this API response.
     *
     * @return the response code
     * @see <a href="http://wiki.atlauncher.com/api:response_code">http://wiki.atlauncher.com/api:response_code</a>
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Gets the error message returned by the API.
     *
     * @return the error message received from the API if there was an error, or null if there was no error
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the data returned by the API.
     *
     * @return the data returned from the API. Please note that this may not be set or be null.
     */
    public T getData() {
        return this.data;
    }
}
