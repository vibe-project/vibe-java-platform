/*
 * Copyright 2014 The Vibe Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.atmosphere.vibe.platform.server;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.Data;
import org.atmosphere.vibe.platform.HttpStatus;
import org.atmosphere.vibe.platform.Wrapper;

/**
 * Represents a server-side HTTP request-response exchange.
 * <p/>
 * Implementations are not thread-safe and decide whether and which event is
 * fired in asynchronous manner.
 *
 * @author Donghwan Kim
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">RFC2616 -
 * Hypertext Transfer Protocol -- HTTP/1.1</a>
 */
public interface ServerHttpExchange extends Wrapper {

    /**
     * The request URI used to connect.
     */
    String uri();

    /**
     * The name of the request method.
     */
    String method();

    /**
     * The names of the request headers. HTTP header is not case-sensitive but
     * {@link Set} is case-sensitive. When iterating the set unlike getting the
     * header value, you should make it lower-case or upper-case and use it.
     */
    Set<String> headerNames();

    /**
     * Returns the first request header associated with the given name.
     */
    String header(String name);

    /**
     * Returns the request headers associated with the given name or empty list
     * if no header is found.
     */
    List<String> headers(String name);

    /**
     * Attaches an action to be called with the whole request body where the
     * request ends. If the body is quite big, it may drain memory quickly.
     */
    ServerHttpExchange bodyAction(Action<Data> action);

    /**
     * Sets a response header.
     */
    ServerHttpExchange setHeader(String name, String value);

    /**
     * Sets response headers.
     */
    ServerHttpExchange setHeader(String name, Iterable<String> value);

    /**
     * Writes a text to the response body.
     */
    ServerHttpExchange write(String data);

    /**
     * Writes a byte body to the response body.
     */
    ServerHttpExchange write(ByteBuffer byteBuffer);

    /**
     * Completes the response. The response must be finished with this method
     * when done. This method has no side effect if called more than once.
     */
    ServerHttpExchange end();

    /**
     * Writes a string to the response body and completes the response.
     */
    ServerHttpExchange end(String data);

    /**
     * Writes a byte to the response body and completes the response.
     */
    ServerHttpExchange end(ByteBuffer byteBuffer);

    /**
     * Sets the HTTP status for the response.
     */
    ServerHttpExchange setStatus(HttpStatus status);

    /**
     * Attaches an action to be called when both request and response end or the
     * underlying connection is aborted for some reason like an error. If the
     * connection is already closed, the handler will be executed on addition.
     * After this event, all the other event will be disabled.
     */
    ServerHttpExchange closeAction(Action<Void> action);

    /**
     * Attaches an action to be called when this exchange gets an error. It may
     * or may not accompany the closure of connection. Its exact behavior is
     * platform-specific.
     */
    ServerHttpExchange errorAction(Action<Throwable> action);

}
