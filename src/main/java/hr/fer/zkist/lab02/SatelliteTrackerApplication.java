package hr.fer.zkist.lab02;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class SatelliteTrackerApplication implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, 0);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        path = path.substring(1);

        byte[] response = new byte[]{};
        try {
            response = Files.readAllBytes(
                    Path.of(
                            Objects.requireNonNull(
                                    this.getClass().getClassLoader().getResource(path)
                            ).toURI()
                    )
            );
        } catch (URISyntaxException e) {
            exchange.sendResponseHeaders(503, 0);
        }

        String contentType = path.endsWith("html") ? "text/html" : path.endsWith("css") ? "text/css" : "text/javascript";
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        HttpHandler httpHandler = new SatelliteTrackerApplication();
        httpServer.createContext("/", httpHandler);
        httpServer.setExecutor(null);
        httpServer.start();
    }
}