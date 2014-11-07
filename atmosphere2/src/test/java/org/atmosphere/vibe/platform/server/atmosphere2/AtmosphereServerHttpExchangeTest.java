package org.atmosphere.vibe.platform.server.atmosphere2;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.vibe.platform.Action;
import org.atmosphere.vibe.platform.server.ServerHttpExchange;
import org.atmosphere.vibe.platform.test.server.ServerHttpExchangeTestTemplate;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Ignore;
import org.junit.Test;

public class AtmosphereServerHttpExchangeTest extends ServerHttpExchangeTestTemplate {

    Server server;

    @Override
    protected void startServer() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        // ServletContext
        ServletContextHandler handler = new ServletContextHandler();
        server.setHandler(handler);
        ServletContextListener listener = new ServletContextListener() {
            @Override
            public void contextInitialized(ServletContextEvent event) {
                ServletContext context = event.getServletContext();
                @SuppressWarnings("serial")
                ServletRegistration.Dynamic reg = context.addServlet(VibeAtmosphereServlet.class.getName(), new VibeAtmosphereServlet() {
                    @Override
                    protected Action<ServerHttpExchange> httpAction() {
                        return new Action<ServerHttpExchange>() {
                            @Override
                            public void on(ServerHttpExchange http) {
                                performer.serverAction().on(http);
                            }
                        };
                    }
                });
                reg.setAsyncSupported(true);
                reg.setInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, Boolean.TRUE.toString());
                reg.addMapping("/test");
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
            }
        };
        handler.addEventListener(listener);

        server.start();
    }

    @Override
    protected void stopServer() throws Exception {
        server.stop();
    }

    @Test
    public void unwrap() {
        performer.serverAction(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                assertThat(http.unwrap(AtmosphereResource.class), instanceOf(AtmosphereResource.class));
                performer.start();
            }
        })
        .send();
    }

    @Override
    @Test
    @Ignore
    public void closeAction_abnormal() {}

}
