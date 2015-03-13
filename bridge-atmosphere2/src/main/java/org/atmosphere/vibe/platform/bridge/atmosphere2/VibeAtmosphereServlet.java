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
package org.atmosphere.vibe.platform.bridge.atmosphere2;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.handler.AtmosphereHandlerAdapter;
import org.atmosphere.vibe.platform.action.Action;
import org.atmosphere.vibe.platform.action.Actions;
import org.atmosphere.vibe.platform.action.ConcurrentActions;
import org.atmosphere.vibe.platform.http.ServerHttpExchange;
import org.atmosphere.vibe.platform.websocket.ServerWebSocket;

/**
 * Servlet to process {@link AtmosphereResource} into {@link ServerHttpExchange}
 * and {@link ServerWebSocket}. When you configure servlet, you must set
 * <strong><code>asyncSupported</code></strong> to <strong><code>true</code>
 * </strong> and set a init param, <strong>
 * <code>org.atmosphere.cpr.AtmosphereInterceptor.disableDefaults</code>
 * </strong>, to <strong><code>true</code></strong>.
 * <p>
 * 
 * <pre>
 * Servlet servlet = new VibeAtmosphereServlet().httpAction(http -&gt {}).websocketAction(ws -&gt {});
 * ServletRegistration.Dynamic reg = context.addServlet(VibeAtmosphereServlet.class.getName(), servlet);
 * <strong>reg.setAsyncSupported(true);</strong>
 * <strong>reg.setInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, Boolean.TRUE.toString())</strong>
 * reg.addMapping("/vibe");
 * </pre>
 *
 * @author Donghwan Kim
 */
@SuppressWarnings("serial")
public class VibeAtmosphereServlet extends AtmosphereServlet {
    
    private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();
    private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();

    @Override
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        framework().addAtmosphereHandler("/", new AtmosphereHandlerAdapter() {
            @Override
            public void onRequest(AtmosphereResource resource) throws IOException {
                if (isWebSocketResource(resource)) {
                    if (resource.getRequest().getMethod().equals("GET")) {
                        wsActions.fire(new AtmosphereServerWebSocket(resource));
                    }
                } else {
                    httpActions.fire(new AtmosphereServerHttpExchange(resource));
                }
            }
        });
    }

    /**
     * Does the given {@link AtmosphereResource} represent WebSocket resource?
     */
    protected boolean isWebSocketResource(AtmosphereResource resource) {
        // As HttpServletResponseWrapper, AtmosphereResponse returns itself on
        // its getResponse method when there was no instance of ServletResponse
        // given by the container. That's exactly the case of WebSocket.
        return resource.getResponse().getResponse() instanceof AtmosphereResponse;
    }

    /**
     * Registers an action to be called when {@link ServerHttpExchange} is
     * available.
     */
    public VibeAtmosphereServlet httpAction(Action<ServerHttpExchange> action) {
        httpActions.add(action);
        return this;
    }

    /**
     * Registers an action to be called when {@link ServerWebSocket} is
     * available.
     */
    public VibeAtmosphereServlet websocketAction(Action<ServerWebSocket> action) {
        wsActions.add(action);
        return this;
    }

}
