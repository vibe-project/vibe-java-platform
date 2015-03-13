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
import org.atmosphere.vibe.platform.http.ServerHttpExchange;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Handler to process {@link HttpServerRequest} into
 * {@link VertxServerHttpExchange}.
 * <p>
 * 
 * <pre>
 * httpServer.requestHandler(new VibeRequestHandler().onhttp(http -&gt {}));
 * </pre>
 *
 * @author Donghwan Kim
 */
public class VibeRequestHandler implements Handler<HttpServerRequest> {

    private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();

    @Override
    public void handle(HttpServerRequest request) {
        httpActions.fire(new VertxServerHttpExchange(request));
    }

    /**
     * Registers an action to be called when {@link ServerHttpExchange} is
     * available.
     */
    public VibeRequestHandler onhttp(Action<ServerHttpExchange> action) {
        httpActions.add(action);
        return this;
    }


}
