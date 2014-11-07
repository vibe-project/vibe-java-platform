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

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.server.ServerWebSocket;

/**
 * Endpoint to process {@link Session} into {@link ServerWebSocket}. Once
 * {@link Session} is opened, {@link JwaServerWebSocket} is created and passed
 * to {@link VibeServerEndpoint#wsAction()}. So what you need to do is to
 * configure this endpoint and to provide your action to receive
 * {@link ServerWebSocket} by overriding {@link VibeServerEndpoint#wsAction()}
 * like the following usage.
 * <p>
 * <pre>
 * ServerEndpointConfig config = ServerEndpointConfig.Builder.create(VibeServerEndpoint.class, "/vibe")
 * .configurator(new Configurator() {
 *     {@literal @}Override
 *     protected &ltT&gt T getEndpointInstance(Class&ltT&gt endpointClass) throws InstantiationException {
 *         return endpointClass.cast(new VibeServerEndpoint() {
 *             {@literal @}Override
 *             public Action&ltServerWebSocket&gt wsAction() {
 *                 return server.wsAction();
 *             }
 *         });
 *     }
 * })
 * .build();
 * </pre>
 * <p>
 * With CDI, the following usage is also available.
 * <p>
 * <pre>
 * {@literal @}ServerEndpoint("/vibe")
 * public class MyVibeServerEndpoint extends VibeServerEndpoint {
 *     {@literal @}Inject
 *     private Server server;
 *     
 *     {@literal @}Override
 *     protected Action&ltServerWebSocket&gt wsAction() {
 *         return server.wsAction();
 *     }
 * }
 * </pre>
 *
 * @author Donghwan Kim
 */
public class VibeServerEndpoint extends Endpoint {

    private JwaServerWebSocket ws;

    @Override
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        ws = new JwaServerWebSocket(session);
        wsAction().on(ws);
    }

    /**
     * An {@link Action} to consume {@link ServerWebSocket}. By default, it
     * throws {@link IllegalStateException} so you should provide your action by
     * overriding it.
     */
    protected Action<ServerWebSocket> wsAction() {
        throw new IllegalStateException("Actiont to receive ServerWebSocket is not set");
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

}
