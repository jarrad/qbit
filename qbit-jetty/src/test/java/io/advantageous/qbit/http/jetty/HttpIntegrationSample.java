package io.advantageous.qbit.http.jetty;
import io.advantageous.qbit.http.HttpServer;


import static io.advantageous.qbit.http.HttpServerBuilder.httpServerBuilder;

/**
 * Created by rhightower on 2/13/15.
 */
public class HttpIntegrationSample {

    public static void main(String... args) throws Exception {
        RegisterJettyWithQBit.registerJettyWithQBit();

        final HttpServer httpServer = httpServerBuilder().setPort(9999).build();

        httpServer.setHttpRequestConsumer(request -> {
            request.getResponse().response(200, "text/html", "<html><body>Hello World!</body></html>");
        });

        httpServer.setWebSocketMessageConsumer(webSocketMessage -> webSocketMessage.getSender().sendText("HI"));
        httpServer.start();
    }
}
