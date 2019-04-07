/* Copyright (c) 2019 dbradley.
 */
package tstsvr;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Class that is the heart for simple-server for testing JscovTestUtil.
 *
 * @author dbradley
 */
public class HttpSvr {
    
    private HttpSvr(){
        
    }
    
    static public HttpServer makeServer(int svrPort,
            JscovContextHandler... contextArr) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(svrPort), 0);

            for(JscovContextHandler ctxt : contextArr){
                server.createContext(ctxt.path, ctxt);
            }
            return server;
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("\nCRITICAL: server launch issue: %s\n",
                            ex.getMessage()));
        }
    }
}
