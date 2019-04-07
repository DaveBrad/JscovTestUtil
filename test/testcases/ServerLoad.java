/* Copyright (c) 2019 dbradley.
 */
package testcases;

import com.sun.net.httpserver.HttpServer;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;
import sun.net.www.http.HttpClient;
import tstsvr.ContextRoot;
import tstsvr.HttpSvr;

/**
 *
 * @author dbradley
 */
public class ServerLoad {
    
     private final static String STOPELEID_MSECOND_REMOVE_TIMER = "3000";

    /**
     * ID of a DOM element that will cause JscovTestUtil to know that the
     * jscoverage_report(...) has completed.
     */
    private final static String STOPELEID_STR = "stopEleId";
    /**
     * Javascript request for report create with on complete callback function
     * which will append a 'stopEleId' to the DOM. The presence of the
     * stop-element will cause the stopping of the JSCover proxy server.
     */
    final private static String REPORT_COMPLETE_AND_CALLBACK_SCRIPT
            = "return jscoverage_report('%s', function(){"
            // callback actions
            // 1) once report is complete, append to DOM stopEleId element
            // 2) remove the element from DOM after N milliseconds
            // - - - by doing this ensures the DOM has no lagging element
            // - - - in the event JscovTestUtil is interrupted.
            //
            + "var bodyEle = document.getElementsByTagName('body')[0];"
            + "var doStopEle = document.createElement('p');"
            + "doStopEle.setAttribute('id', '" + STOPELEID_STR + "');"
            + "bodyEle.appendChild(doStopEle);"
            + ""
            // 2) remove element action
            //
            // setTimeout( 
            //  function(){document.getElementById('stopEleId').remove(); },
            //  1000);
            //
            + "setTimeout("
            + " function(){document.getElementById('" + STOPELEID_STR + "').remove(); },"
            + STOPELEID_MSECOND_REMOVE_TIMER
            + ");"
            + "});";

    HttpClient hc;

    @Test
    public void launchServer() {

        HttpServer server = HttpSvr.makeServer(8500,
                new ContextRoot());

        server.start();
        pause(2000);

//        Application.launch(FxWebView.class);
        pause(3000);


        int a = 1;

        DesiredCapabilities cap = new DesiredCapabilities();

        Proxy hudProxy = new Proxy();
        hudProxy.setHttpProxy("localhost:3128")
                .setProxyType(Proxy.ProxyType.MANUAL);

        cap.setCapability(CapabilityType.PROXY, hudProxy);

        HtmlUnitDriver dddddddd = new HtmlUnitDriver(); // (cap);
        dddddddd.setProxySettings(hudProxy);
        dddddddd.setJavascriptEnabled(true);

//        dddddddd.setProxy("http://localhost", 3128);
        dddddddd.get("http://localhost:8500/");

       
                pause(3000);

        JavascriptExecutor js = (JavascriptExecutor) dddddddd;
        Object result = js.executeScript("return a");

        int b = 1;
        
        String generateReportScript = String.format(REPORT_COMPLETE_AND_CALLBACK_SCRIPT,
                "ddddd");
        
        
        ((JavascriptExecutor) dddddddd).executeScript(generateReportScript);
    }

    // - - - - - - - - - - - - - - - - - - - - - 
    private void pause(int msecs) {
        try {
            Thread.sleep(msecs);

        } catch (InterruptedException ex) {
            //            Logger.getLogger(ctrl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


