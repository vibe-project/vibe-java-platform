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
package org.atmosphere.vibe.platform.server.atmosphere2;

import static org.atmosphere.websocket.WebSocketEventListener.WebSocketEvent.TYPE.CLOSE;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResource.TRANSPORT;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.handler.AtmosphereHandlerAdapter;
import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.Actions;
import org.atmosphere.vibe.platform.SimpleActions;
import org.atmosphere.vibe.platform.server.ServerHttpExchange;
import org.atmosphere.vibe.platform.server.ServerWebSocket;
import org.atmosphere.websocket.DefaultWebSocketProcessor;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketEventListener;
import org.atmosphere.websocket.WebSocketProcessor;

/**
 * Convenient class to install Atmosphere bridge.
 *
 * @author Donghwan Kim
 */
public class AtmosphereBridge {

    private Actions<ServerHttpExchange> httpActions = new SimpleActions<>();
    private Actions<ServerWebSocket> wsActions = new SimpleActions<>();

    public AtmosphereBridge(ServletContext context, String path) {
        // Configure Atmosphere
        AtmosphereServlet servlet = new AtmosphereServlet();
        AtmosphereFramework framework = servlet.framework();
        framework.addAtmosphereHandler("/", new AtmosphereHandlerAdapter() {
            @Override
            public void onRequest(AtmosphereResource resource) throws IOException {
                if (resource.transport() == TRANSPORT.WEBSOCKET) {
                    if (resource.getRequest().getMethod().equals("GET")) {
                        wsActions.fire(new AtmosphereServerWebSocket(resource));
                    }
                } else {
                    httpActions.fire(new AtmosphereServerHttpExchange(resource));
                }
            }
        });
        framework.setWebsocketProcessorClassName(VibePlatformWebSocketProcessor.class.getName());
        framework.objectFactory(new AtmosphereObjectFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T, U extends T> T newClassInstance(AtmosphereFramework framework, Class<T> classType, Class<U> defaultType)
                    throws InstantiationException, IllegalAccessException {
                // Intercepts WebSocketProcessor's instantiation to provide framework instance
                if (classType == WebSocketProcessor.class && defaultType == VibePlatformWebSocketProcessor.class) {
                    return (T) new VibePlatformWebSocketProcessor(framework);
                }
                return defaultType.newInstance();
            }
        });

        // Registers the servlet programmatically
        ServletRegistration.Dynamic reg = context.addServlet("vibe#" + UUID.randomUUID(), servlet);
        reg.setAsyncSupported(true);
        reg.addMapping(path);
        reg.setInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, Boolean.TRUE.toString());
    }

    /**
     * Adds an action to be called on HTTP request with
     * {@link ServerHttpExchange}.
     */
    public AtmosphereBridge httpAction(Action<ServerHttpExchange> action) {
        httpActions.add(action);
        return this;
    }

    /**
     * Adds an action to be called on WebSocket connection with
     * {@link ServerWebSocket} in open state.
     */
    public AtmosphereBridge websocketAction(Action<ServerWebSocket> action) {
        wsActions.add(action);
        return this;
    }
    
    // Ugly fix for https://github.com/Atmosphere/atmosphere/issues/1623
    @SuppressWarnings("serial")
    public static class VibePlatformWebSocketProcessor extends DefaultWebSocketProcessor {

        public VibePlatformWebSocketProcessor(AtmosphereFramework framework) {
            super(framework);
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public void executeClose(WebSocket webSocket, int closeCode) {
            // Notifies listeners first because super.asynchronousProcessor.endRequest empties them
            boolean isClosedByClient = webSocket.resource() == null ? true : webSocket.resource().getAtmosphereResourceEvent().isClosedByClient();
            if (!isClosedByClient) {
                notifyListener(webSocket, new WebSocketEventListener.WebSocketEvent(closeCode, CLOSE, webSocket));
            }
            // call super.executeClose for the rest of things
            super.executeClose(webSocket, closeCode);
        }
        
        @Override
        public void close(WebSocket webSocket, int closeCode) {
            boolean done = false;
            AtmosphereResource resource = webSocket.resource();
            if (resource != null) {
                // Intercepts and call executeClose only if this condition which is the case meets
                if (!resource.getAtmosphereResourceEvent().isClosedByClient() && resource.getAtmosphereResourceEvent().isClosedByApplication() && resource.isCancelled()) {
                    executeClose(webSocket, closeCode);
                    done = true;
                }
            }
            // If it's not our case, call super.close
            if (!done) {
                super.close(webSocket, closeCode);
            }
        }
        
    }

}
