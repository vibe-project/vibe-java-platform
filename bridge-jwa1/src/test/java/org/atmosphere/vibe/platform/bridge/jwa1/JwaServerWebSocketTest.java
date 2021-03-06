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

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.atmosphere.vibe.platform.action.Action;
import org.atmosphere.vibe.platform.test.ServerWebSocketTest;
import org.atmosphere.vibe.platform.websocket.ServerWebSocket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.junit.Test;

public class JwaServerWebSocketTest extends ServerWebSocketTest {

    Server server;

    @Override
    protected void startServer() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);
        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);
        ServerContainer container = WebSocketServerContainerInitializer.configureContext(handler);
        ServerEndpointConfig config = ServerEndpointConfig.Builder.create(VibeServerEndpoint.class, "/test")
        .configurator(new Configurator() {
            @Override
            public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                return endpointClass.cast(new VibeServerEndpoint().onwebsocket(performer.serverAction()));
            }
        })
        .build();
        container.addEndpoint(config);
        server.start();
    }

    @Test
    public void unwrap() {
        performer.onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                assertThat(ws.unwrap(Session.class), instanceOf(Session.class));
                performer.start();
            }
        })
        .connect();
    }

    @Override
    protected void stopServer() throws Exception {
        server.stop();
    }

}
