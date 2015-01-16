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
package org.atmosphere.vibe.platform.bridge.vertx2;

import org.atmosphere.vibe.platform.action.Action;
import org.atmosphere.vibe.platform.action.Actions;
import org.atmosphere.vibe.platform.action.ConcurrentActions;
import org.atmosphere.vibe.platform.ws.ServerWebSocket;
import org.vertx.java.core.Handler;

/**
 * Handler to process {@link org.vertx.java.core.http.ServerWebSocket} into
 * {@link VertxServerWebSocket}.
 * <p>
 * 
 * <pre>
 * httpServer.websocketHandler(new VibeWebSocketHandler().wsAction(http -&gt {}));
 * </pre>
 *
 * @author Donghwan Kim
 */
public class VibeWebSocketHandler implements Handler<org.vertx.java.core.http.ServerWebSocket> {

    private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();

    @Override
    public void handle(org.vertx.java.core.http.ServerWebSocket ws) {
        wsActions.fire(new VertxServerWebSocket(ws));
    }

    /**
     * Registers an action to be called when {@link ServerWebSocket} is
     * available.
     */
    public VibeWebSocketHandler wsAction(Action<ServerWebSocket> action) {
        wsActions.add(action);
        return this;
    }

}
