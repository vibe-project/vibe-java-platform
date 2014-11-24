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
import org.atmosphere.vibe.platform.server.ServerHttpExchange;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Handler to process {@link HttpServerRequest} into
 * {@link VertxServerHttpExchange}. You need to configure this handler and
 * provide your action to receive {@link ServerHttpExchange} by overriding
 * {@link VibeRequestHandler#httpAction()} like the following usage.
 * <p>
 * 
 * <pre>
 * httpServer.requestHandler(new VibeRequestHandler() {
 *     {@literal @}Override
 *     protected Action&ltServerHttpExchange&gt httpAction() {
 *         return server.httpAction();
 *     }
 * });
 * </pre>
 *
 * @author Donghwan Kim
 */
public class VibeRequestHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        httpAction().on(new VertxServerHttpExchange(request));
    }

    /**
     * An {@link Action} to consume {@link ServerHttpExchange}. By default, it
     * throws {@link IllegalStateException} so you should provide your action by
     * overriding it.
     */
    protected Action<ServerHttpExchange> httpAction() {
        throw new IllegalStateException("Actiont to receive ServerHttpExchange is not set");
    }

}
