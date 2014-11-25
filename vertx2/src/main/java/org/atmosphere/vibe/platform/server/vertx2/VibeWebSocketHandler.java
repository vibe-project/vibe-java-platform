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
package org.atmosphere.vibe.platform.server.vertx2;

import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.server.ServerWebSocket;
import org.vertx.java.core.Handler;

/**
 * Handler to process {@link org.vertx.java.core.http.ServerWebSocket} into
 * {@link VertxServerWebSocket}.
 * <p>
 * 
 * <pre>
 * httpServer.websocketHandler(new VibeWebSocketHandler() {
 *     {@literal @}Override
 *     protected Action&ltServerWebSocket&gt wsAction() {
 *         return server.wsAction();
 *     }
 * });
 * </pre>
 *
 * @author Donghwan Kim
 */
public abstract class VibeWebSocketHandler implements Handler<org.vertx.java.core.http.ServerWebSocket> {

    @Override
    public void handle(org.vertx.java.core.http.ServerWebSocket ws) {
        wsAction().on(new VertxServerWebSocket(ws));
    }

    /**
     * An {@link Action} to consume {@link ServerWebSocket}.
     */
    protected abstract Action<ServerWebSocket> wsAction();

}
