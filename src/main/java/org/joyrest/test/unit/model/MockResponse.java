/*
 * Copyright 2015 Petr Bouda
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyrest.test.unit.model;

import org.joyrest.model.http.HeaderName;
import org.joyrest.model.http.HttpStatus;
import org.joyrest.model.response.InternalResponse;
import org.joyrest.model.response.Response;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MockResponse<E> extends InternalResponse<E>{

    private Map<HeaderName, String> headers = new HashMap<>();

    private HttpStatus status;

    private OutputStream outputStream;

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public Map<HeaderName, String> getHeaders() {
        return headers;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public Response<E> header(HeaderName name, String value) {
        headers.put(name, value);
        return this;
    }

    @Override
    public Response<E> status(HttpStatus status) {
        this.status = status;
        return this;
    }

    public void setHeaders(Map<HeaderName, String> headers) {
        this.headers = headers;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

}
