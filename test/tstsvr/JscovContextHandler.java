/* Copyright (c) 2019 dbradley.
 */
package tstsvr;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;

/**
 *
 * @author dbradley
 */
abstract public class JscovContextHandler implements HttpHandler {

    static String webpageDir = String.format("%s/test/webpage/",
            System.getProperty("user.dir"));

    public String path;
    public HttpExchange exchange;

    public JscovContextHandler(String path) {
        this.path = path;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        this.exchange = he;

        InputStream body = he.getRequestBody();

        Headers hdr = he.getRequestHeaders();

        String mthd = he.getRequestMethod();

        HttpContext contxt = he.getHttpContext();

        URI uridata = he.getRequestURI();

        int a = 1;

        if (mthd.equals("GET")) {

            String uriPath = uridata.getPath();
            if (uriPath.equals("/")) {
                // request for root
                uriPath = "/TestPage.html";
            }
            String getpathFull = String.format("%s%s", this.path.substring(1), uriPath);
            System.err.printf("YYYY: %s\n", getpathFull);
            getFileRequest(getpathFull);
            
            he.close();

            return;
        }

        handleRequest();

//        httpExchange.getRequestURI().getQuery()
    }

    abstract public void handleRequest() throws IOException;

    public void send200(String response) throws IOException {
        sendHdrCode(200, response);
    }

    public void sendHdrCode(int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public void getFileRequest(String fileName) throws IOException {

        // prepare the context of the file
        String encoding = "UTF-8";
        this.exchange.getResponseHeaders().set("Content-Type",
                String.format("%s; charset=%s",
                        fileContext(fileName), encoding));
        this.exchange.getResponseHeaders().set("Accept-Ranges", "bytes");

        // the file needs to be stored in the webpage folder of this
        // testing environment
        File file = new File(webpageDir, fileName);
        this.exchange.sendResponseHeaders(200, file.length());

        try (OutputStream os = this.exchange.getResponseBody()) {
            Files.copy(file.toPath(), os);
        }
    }

    private String fileContext(String fileName) {
        String fileNameLC = fileName.toLowerCase();
        if (fileNameLC.endsWith(".html") || fileNameLC.endsWith(".htm")) {
            return "text/html";
        }
        if (fileNameLC.endsWith(".js")) {
            return "text/javascript";
        }
        if (fileNameLC.endsWith(".css")) {
            return "text/css";
        }
        return "text/html";
    }
}
