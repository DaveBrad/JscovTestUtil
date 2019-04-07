/* Copyright (c) 2019 dbradley.
 */
package tstsvr;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 *
 * @author dbradley
 */
public class ContextRoot extends JscovContextHandler {

    public ContextRoot() {
        super("/");
    }

    @Override
    public void handleRequest() throws IOException {
        
        System.err.println("xxxxxxxxxxxxxx");
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
