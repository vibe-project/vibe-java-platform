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
import org.atmosphere.vibe.platform.HttpStatus;
import org.atmosphere.vibe.platform.Wrapper;

/**
 * Represents a server-side HTTP request-response exchange.
 * <p/>
 * Implementations are not thread-safe.
 *
 * @author Donghwan Kim
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">RFC2616 -
 *      Hypertext Transfer Protocol -- HTTP/1.1</a>
 */
public interface ServerHttpExchange extends Wrapper {

    /**
     * The request URI.
     */
    String uri();

    /**
     * The name of the request method.
     */
    String method();

    /**
     * The names of the request headers. HTTP header is not case-sensitive but
     * {@link Set} is case-sensitive.
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
     * Reads the request body. If the request header, {@code content-type},
     * starts with {@code text/}, the body is read as text, and if not, as
     * binary.
     * <p>
     * The read data will be passed to event handlers attached through
     * {@link ServerHttpExchange#chunkAction(Action)} or
     * {@link ServerHttpExchange#bodyAction(Action)}. In the following cases,
     * this method must be executed after adding chunk or body event handler.
     * <p />
     * <ul>
     * <li>When the response has completed by {@link ServerHttpExchange#end()}</li>
     * <li>When the underlying platform can't read the request body
     * asynchronously</li>
     * </ul>
     * <p />
     * This method has no side effect if called more than once.
     */
    ServerHttpExchange read();
    
    /**
     * Attaches an action to be called with a chunk from the request body. The
     * allowed data type is {@link String} for text body and {@link ByteBuffer}
     * for binary body.
     */
    ServerHttpExchange chunkAction(Action<?> action);
    
    /**
     * Attaches an action to be called when the request is fully read. It's the
     * end of the request.
     */
    ServerHttpExchange endAction(Action<Void> action);

    /**
     * Attaches an action to be called with the whole request body. The allowed
     * data type is {@link String} for text body and {@link ByteBuffer} for
     * binary body. If the body is quite big, it may drain memory quickly.
     */
    ServerHttpExchange bodyAction(Action<?> action);

    /**
     * Sets the HTTP status for the response.
     */
    ServerHttpExchange setStatus(HttpStatus status);

    /**
     * Sets a response header.
     */
    ServerHttpExchange setHeader(String name, String value);

    /**
     * Sets response headers.
     */
    ServerHttpExchange setHeader(String name, Iterable<String> value);

    /**
     * Writes a text chunk to the response body.
     */
    ServerHttpExchange write(String data);

    /**
     * Writes a binary chunk to the response body.
     */
    ServerHttpExchange write(ByteBuffer byteBuffer);

    /**
     * Completes the response. Each exchange's response must be finished with
     * this method when done. It's the end of the response. This method has no
     * side effect if called more than once.
     */
    ServerHttpExchange end();

    /**
     * Writes a text chunk to the response body and completes the response
     * through {@link ServerHttpExchange#end()}.
     */
    ServerHttpExchange end(String data);

    /**
     * Writes a binary chunk to the response body and completes the response
     * through {@link ServerHttpExchange#end()}.
     */
    ServerHttpExchange end(ByteBuffer byteBuffer);

    /**
     * Attaches an action to be called when this exchange gets an error. It may
     * or may not accompany the closure of connection. Its exact behavior is
     * platform-specific and error created by the platform is propagated.
     */
    ServerHttpExchange errorAction(Action<Throwable> action);

    /**
     * Attaches an action to be called when both request and response end
     * successfully or the underlying connection is aborted for some reason like
     * an error. After this event, all the other event will be
     * disabled.
     */
    ServerHttpExchange closeAction(Action<Void> action);

}
