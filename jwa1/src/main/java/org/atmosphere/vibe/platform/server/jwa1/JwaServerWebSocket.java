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
package org.atmosphere.vibe.platform.server.jwa1;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import javax.websocket.MessageHandler;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;

import org.atmosphere.vibe.platform.server.AbstractServerWebSocket;
import org.atmosphere.vibe.platform.server.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ServerWebSocket} for Java WebSocket API 1.
 *
 * @author Donghwan Kim
 */
public class JwaServerWebSocket extends AbstractServerWebSocket {

    private final Logger logger = LoggerFactory.getLogger(JwaServerWebSocket.class);

    private final Session session;
    // https://issues.apache.org/bugzilla/show_bug.cgi?id=56026
    private final Semaphore semaphore = new Semaphore(1, true);

    public JwaServerWebSocket(Session session) {
        this.session = session;
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                textActions.fire(message);
            }
        });
        session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
            @Override
            public void onMessage(ByteBuffer message) {
                binaryActions.fire(message);
            }
        });
    }
    
    void onError(Throwable e) {
        errorActions.fire(e);
    }

    void onClose() {
        closeActions.fire();
    }

    @Override
    public String uri() {
        // session.getRequestURI() returns the full URI starting with protocol
        // not request URI starting with path
        URI uri = session.getRequestURI();
        return uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
    }

    @Override
    protected void doClose() {
        try {
            session.close();
        } catch (IOException e) {
            errorActions.fire(e);
        }
    }

    @Override
    protected void doSend(ByteBuffer byteBuffer) {
        try {
            semaphore.acquireUninterruptibly();
            session.getAsyncRemote().sendBinary(byteBuffer, new WriteResult(byteBuffer));
        } catch (IllegalStateException e) {
            // TODO: The message will be lost, need a cache.
            semaphore.release();
            errorActions.fire(e);
        }
    }

    @Override
    protected void doSend(String data) {
        try {
            semaphore.acquireUninterruptibly();
            session.getAsyncRemote().sendText(data, new WriteResult(data));
        } catch (IllegalStateException e) {
            // TODO: The message will be lost, need a cache.
            semaphore.release();
            errorActions.fire(e);
        }
    }

    /**
     * {@link Session} is available.
     */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        return Session.class.isAssignableFrom(clazz) ? clazz.cast(session) : null;
    }

    private final class WriteResult implements SendHandler {

        private final Object message;

        private WriteResult(Object message) {
            this.message = message;
        }

        @Override
        public void onResult(SendResult result) {
            semaphore.release();
            if (!result.isOK() || result.getException() != null) {
                logger.warn("WebSocket {} failed to write {}", session, message);
            }
        }
    }

}
