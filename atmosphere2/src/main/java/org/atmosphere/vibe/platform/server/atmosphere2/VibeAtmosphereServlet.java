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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResource.TRANSPORT;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.handler.AtmosphereHandlerAdapter;
import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.server.ServerHttpExchange;
import org.atmosphere.vibe.platform.server.ServerWebSocket;

/**
 * Servlet to process {@link AtmosphereResource} into {@link ServerHttpExchange}
 * and {@link ServerWebSocket}. You need to configure this servlet and provide
 * your action to receive {@link ServerHttpExchange} and {@link ServerWebSocket}
 * by overriding {@link VibeAtmosphereServlet#httpAction()} and
 * {@link VibeAtmosphereServlet#wsAction()} like the following usage. When you configure
 * servlet, you must set <strong><code>asyncSupported</code></strong> to
 * <strong><code>true</code></strong> and set a init param, <strong>
 * <code>org.atmosphere.cpr.AtmosphereInterceptor.disableDefaults</code>
 * </strong>, to <strong><code>true</code></strong>.
 * <p>
 * <pre>
 * ServletRegistration.Dynamic reg = context.addServlet(VibeAtmosphereServlet.class.getName(), new VibeAtmosphereServlet() {
 *     {@literal @}Override
 *     protected Action&ltServerHttpExchange&gt httpAction() {
 *         return server.httpAction();
 *     }
 *     
 *     {@literal @}Override
 *     protected Action&ltServerWebSocket&gt wsAction() {
 *         return server.wsAction();
 *     }
 * });
 * <strong>reg.setAsyncSupported(true);</strong>
 * <strong>reg.setInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, Boolean.TRUE.toString())</strong>
 * reg.addMapping("/vibe");
 * </pre>
 * <p>
 * With CDI, the following usage is also available.
 * <p>
 * <pre>
 * {@literal @}WebServlet(value = "/vibe", <strong>asyncSupported = true</strong>, initParams = { <strong>{@literal @}WebInitParam(name = "org.atmosphere.cpr.AtmosphereInterceptor.disableDefaults", value = "true")</strong> })
 * public class MyVibeAtmosphereServlet extends VibeAtmosphereServlet {
 *     {@literal @}Inject
 *     private Server server;
 *     
 *     {@literal @}Override
 *     protected Action&ltServerHttpExchange&gt httpAction() {
 *         return server.httpAction();
 *     }
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
@SuppressWarnings("serial")
public class VibeAtmosphereServlet extends AtmosphereServlet {

    @Override
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        framework.addAtmosphereHandler("/", new AtmosphereHandlerAdapter() {
            @Override
            public void onRequest(AtmosphereResource resource) throws IOException {
                if (resource.transport() == TRANSPORT.WEBSOCKET) {
                    if (resource.getRequest().getMethod().equals("GET")) {
                        wsAction().on(new AtmosphereServerWebSocket(resource));
                    }
                } else {
                    httpAction().on(new AtmosphereServerHttpExchange(resource));
                }
            }
        });
    }

    /**
     * An {@link Action} to consume {@link ServerHttpExchange}. By default, it
     * throws {@link IllegalStateException} so you should provide your action by
     * overriding it.
     */
    protected Action<ServerHttpExchange> httpAction() {
        throw new IllegalStateException("Actiont to receive ServerHttpExchange is not set");
    }

    /**
     * An {@link Action} to consume {@link ServerWebSocket}. By default, it
     * throws {@link IllegalStateException} so you should provide your action by
     * overriding it.
     */
    protected Action<ServerWebSocket> wsAction() {
        throw new IllegalStateException("Actiont to receive ServerWebSocket is not set");
    }

}
