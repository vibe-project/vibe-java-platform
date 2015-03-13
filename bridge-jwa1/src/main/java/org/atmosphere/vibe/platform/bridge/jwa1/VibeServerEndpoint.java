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
package org.atmosphere.vibe.platform.bridge.jwa1;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.atmosphere.vibe.platform.action.Action;
import org.atmosphere.vibe.platform.action.Actions;
import org.atmosphere.vibe.platform.action.ConcurrentActions;
import org.atmosphere.vibe.platform.websocket.ServerWebSocket;

/**
 * Endpoint to process {@link Endpoint} and {@link Session} into {@link ServerWebSocket}.
 * <p>
 * 
 * <pre>
 * ServerEndpointConfig config = ServerEndpointConfig.Builder.create(VibeServerEndpoint.class, "/vibe")
 * .configurator(new Configurator() {
 *     {@literal @}Override
 *     protected &ltT&gt T getEndpointInstance(Class&ltT&gt endpointClass) throws InstantiationException {
 *         return endpointClass.cast(new VibeServerEndpoint().websocketAction(ws -&gt {}));
 *     }
 * })
 * .build();
 * </pre>
 *
 * @author Donghwan Kim
 */
public class VibeServerEndpoint extends Endpoint {

    private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();
    private JwaServerWebSocket ws;

    @Override
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        ws = new JwaServerWebSocket(session);
        wsActions.fire(ws);
    }

    @Override
    @OnError
    public void onError(Session session, Throwable throwable) {
        ws.onError(throwable);
    }

    @Override
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        ws.onClose();
    }

    /**
     * Registers an action to be called when {@link ServerWebSocket} is
     * available.
     */
    public VibeServerEndpoint websocketAction(Action<ServerWebSocket> action) {
        wsActions.add(action);
        return this;
    }

}
