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
package org.atmosphere.vibe.platform.bridge.servlet3;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.vibe.platform.action.Action;
import org.atmosphere.vibe.platform.http.ServerHttpExchange;

/**
 * Servlet to process {@link HttpServletRequest} and {@link HttpServletResponse}
 * into {@link ServerHttpExchange}. When you configure servlet, you must set
 * <strong><code>asyncSupported</code></strong> to <strong><code>true</code>
 * </strong>.
 * <p>
 * 
 * <pre>
 * ServletRegistration.Dynamic reg = context.addServlet(VibeServlet.class.getName(), new VibeServlet() {
 *     {@literal @}Override
 *     protected Action&ltServerHttpExchange&gt httpAction() {
 *         return server.httpAction();
 *     }
 * });
 * <strong>reg.setAsyncSupported(true);</strong>
 * reg.addMapping("/vibe");
 * </pre>
 *
 * @author Donghwan Kim
 */
@SuppressWarnings("serial")
public abstract class VibeServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        httpAction().on(new ServletServerHttpExchange(req, resp));
    }

    /**
     * An {@link Action} to consume {@link ServerHttpExchange}.
     */
    protected abstract Action<ServerHttpExchange> httpAction();

}
