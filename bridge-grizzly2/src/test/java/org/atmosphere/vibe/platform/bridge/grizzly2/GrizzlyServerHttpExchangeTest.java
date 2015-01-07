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
package org.atmosphere.vibe.platform.bridge.grizzly2;

import org.atmosphere.vibe.platform.action.Action;
import org.atmosphere.vibe.platform.http.ServerHttpExchange;
import org.atmosphere.vibe.platform.test.ServerHttpExchangeTestTemplate;
import org.glassfish.grizzly.http.server.HttpServer;

public class GrizzlyServerHttpExchangeTest extends ServerHttpExchangeTestTemplate {
    
    HttpServer server;

    @Override
    protected void startServer() throws Exception {
        server = HttpServer.createSimpleServer(null, port);
        server.getServerConfiguration().addHttpHandler(new VibeHttpHandler() {
            @Override
            protected Action<ServerHttpExchange> httpAction() {
                return new Action<ServerHttpExchange>() {
                    @Override
                    public void on(ServerHttpExchange http) {
                        performer.serverAction().on(http);
                    }
                };
            }
        }, "/test");
        server.start();
    }

    @Override
    protected void stopServer() throws Exception {
        server.shutdownNow();
    }

}
