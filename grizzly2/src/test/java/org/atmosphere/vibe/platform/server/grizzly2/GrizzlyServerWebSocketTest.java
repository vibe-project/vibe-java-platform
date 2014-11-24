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
package org.atmosphere.vibe.platform.server.grizzly2;

import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.server.ServerWebSocket;
import org.atmosphere.vibe.platform.test.server.ServerWebSocketTestTemplate;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;

public class GrizzlyServerWebSocketTest extends ServerWebSocketTestTemplate {

    HttpServer server;

    @Override
    protected void startServer() throws Exception {
        server = HttpServer.createSimpleServer(null, port);
        server.getListener("grizzly").registerAddOn(new WebSocketAddOn());
        WebSocketEngine.getEngine().register("", "/test", new VibeWebSocketApplication() {
            @Override
            protected Action<ServerWebSocket> wsAction() {
                return new Action<ServerWebSocket>() {
                    @Override
                    public void on(ServerWebSocket ws) {
                        performer.serverAction().on(ws);
                    }
                };
            }
        });
        server.start();
    }

    @Override
    protected void stopServer() throws Exception {
        server.shutdownNow();
    }

}
