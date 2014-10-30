/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

/**
 * Response to an API call made to ATLauncher servers. This contains information including if there was an error, the
 * response code of the request, the error message (if any) and the data received (if any) from the API.
 */
public class APIResponse {
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
    private Object data;

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
    public Object getData() {
        return this.data;
    }

    /**
     * Gets the data returned by the API as an Integer.
     *
     * @return the Integer representation of the data
     */
    public int getDataAsInt() {
        return (Integer) this.data;
    }

    /**
     * Gets the data returned by the API as a String.
     *
     * @return the String representation of the data
     */
    public String getDataAsString() {
        return (String) this.data;
    }
}
