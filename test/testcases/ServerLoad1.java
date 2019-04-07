/* Copyright (c) 2019 dbradley.
 */
package testcases;

import com.sun.net.httpserver.HttpServer;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtestdb.aid.console.CcCapture;
import org.jtestdb.selenium.jscov.JscovBrowserKind;
import static org.jtestdb.selenium.jscov.JscovBrowserKind.CHROME;
import static org.jtestdb.selenium.jscov.JscovBrowserKind.FIREFOX;
import org.jtestdb.selenium.jscov.JscovTestUtil;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import tstsvr.ContextRoot;
import tstsvr.HttpSvr;

/**
 *
 * @author dbradley
 */
public class ServerLoad1 {

    JscovTestUtil proxySvrPersist;

    private static ArrayList<JscovTestUtil> proxySvrArr = new ArrayList<>();
    private static ArrayList<WebDriver> webDriverArr = new ArrayList<>();

    @AfterClass
    public void cleanAfterClass() {
        for (JscovTestUtil proxySvrItem : proxySvrArr) {
            proxySvrItem.tearDownProxyServer(true);
        }
        for (WebDriver webDriverItem : webDriverArr) {
            webDriverItem.quit();
        }
    }

    @Test
    public void interact4() {
        // launch the proxy using JscovTestutil
        proxySvrPersist = new JscovTestUtil(3129,
                "dd",
                String.format("%s/test/dpends/jscoverInst",
                        System.getProperty("user.dir")));

        proxySvrPersist.removeAllReports();

        String repName = proxySvrPersist.getLastTimeStampReport();
        Assert.assertNull(repName);

        proxySvrPersist.storeJscoverReport(30);
    }

    @Test(dependsOnMethods = {"interact4"})
    public void launchServer() {

        int simpleServerPort = 8500;

        HttpServer server = HttpSvr.makeServer(simpleServerPort,
                new ContextRoot());

        server.start();
        pause(2000);

        // launch the proxy using JscovTestutil
        JscovTestUtil proxySvr = new JscovTestUtil(3130,
                "dd",
                String.format("%s/test/dpends/jscoverInst",
                        System.getProperty("user.dir")));

        proxySvrArr.add(proxySvr);

        proxySvr.setOnCodeCoverage()
                .setOnViewReport()
                .reportIntoTimeStampDir(3)
                .startProxyServer();

        pause(3000);

        int a = 1;

        DesiredCapabilities cap = new DesiredCapabilities();

        Proxy hudProxy = new Proxy();
        hudProxy.setHttpProxy(proxySvr.getProxyServerString())
                .setProxyType(Proxy.ProxyType.MANUAL);

        cap.setCapability(CapabilityType.PROXY, hudProxy);

        HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver();

        webDriverArr.add(htmlUnitDriver);

        htmlUnitDriver.setProxySettings(hudProxy);
        htmlUnitDriver.setJavascriptEnabled(true);

        proxySvr.overrideWebDriveForJSCoverProxy(htmlUnitDriver);

        htmlUnitDriver.get(String.format("http://localhost:%d", simpleServerPort));

        pause(3000);

        proxySvr.stopProxyServer(30);

        htmlUnitDriver.quit();

        if (new File("c:\\").exists()) {
            proxySvr.viewReportDo(8080);

            pause(1000);
            try {
                Robot robot = new Robot();

                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_F4);
                robot.delay(10); //set the delay
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_F4);

            } catch (AWTException ex) {
                Logger.getLogger(ServerLoad1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Test(dependsOnMethods = {"launchServer"})
    public void interact5() {
        // launch the proxy using JscovTestutil
        proxySvrPersist = new JscovTestUtil(3129,
                "dd",
                String.format("%s/test/dpends/jscoverInst",
                        System.getProperty("user.dir")));

        proxySvrArr.add(proxySvrPersist);

        String repName = proxySvrPersist.getLastTimeStampReport();
        Assert.assertNotNull(repName);
    }

    @Test(dependsOnMethods = {"interact5"})
    public void interact() {

        proxySvrPersist.startProxyServer();
        try {
            proxySvrPersist.setOnCodeCoverage();
        } catch (RuntimeException rte) {
            Assert.assertTrue(rte.getMessage().contains("ERROR: setOnCodeCoverage() needs to be invoked prior to startProxyServer()."));
        }
        proxySvrPersist.tearDownProxyServer(true);
    }

    @Test(dependsOnMethods = {"interact"})
    public void interact2() {
        proxySvrPersist.setOnCodeCoverage();
        proxySvrPersist.startProxyServer();

        try {
            proxySvrPersist.setOnCodeCoverage();
        } catch (RuntimeException rte) {
            Assert.assertTrue(rte.getMessage().contains("CRITICAL: proxy-server already running."));
        }
        proxySvrPersist.tearDownProxyServer(true);
    }

    @Test(dependsOnMethods = {"interact2"})
    public void interact3() {
        proxySvrPersist.setOnCodeCoverage();
        proxySvrPersist.startProxyServer();

        CcCapture ccCapErr = new CcCapture(System.err);

        ccCapErr.startCapture();
        proxySvrPersist.stopProxyServer(30);
        ccCapErr.stopCapture();

//         proxySvrPersist.tearDownProxyServer(true);
        boolean foundLinesArr = ccCapErr.asOrdered(
                String.format("INFO: JscovTestUtil: Attempting to get a report due to a 'stop' or 'report' action.*"),
                String.format("*HOWEVER, no WebDriver has been allocate or provided.*"))
                .matchEndsWith();

        Assert.assertTrue(foundLinesArr);

    }

    @Test(dependsOnMethods = {"interact3"})
    public void interact6() {
//        proxySvrPersist.removeAllReports();

        String repName = proxySvrPersist.getLastTimeStampReport();
        Assert.assertNotNull(repName);
    }

    @Test(dependsOnMethods = {"interact6"})
    public void interact7() {
        JscovTestUtil otherOne = new JscovTestUtil(3333, "base",
                String.format("%s/dummy", System.getProperty("user.dir")));

        proxySvrArr.add(otherOne);

        otherOne.setOffCodeCoverage();
        try {
            otherOne.startProxyServer();
        } catch (RuntimeException rte) {
            String rteMsg = rte.getMessage();

            Assert.assertTrue(rteMsg.startsWith("Location of JSCover directory not found"));
            Assert.assertTrue(rteMsg.endsWith("/dummy"));
        }

        otherOne.setOnCodeCoverage();
        try {
            otherOne.startProxyServer();
        } catch (RuntimeException rte) {
            String rteMsg = rte.getMessage();

            Assert.assertTrue(rteMsg.startsWith("Location of JSCover directory not found"));
            Assert.assertTrue(rteMsg.endsWith("/dummy"));
        }
    }

    @Test(dependsOnMethods = {"interact7"})
    public void interact8() {
        JscovTestUtil otherOne = new JscovTestUtil(3333, "base",
                String.format("%s/test", System.getProperty("user.dir")));

        proxySvrArr.add(otherOne);

        otherOne.setOnCodeCoverage();
        try {
            otherOne.startProxyServer();
        } catch (RuntimeException rte) {
            String rteMsg = rte.getMessage();

//            "Need to specify explicit path to JSCover-all.jar file.\n"
//                                + "As not fould in: %s\n"
            System.out.println(rteMsg);
            Assert.assertTrue(rteMsg.startsWith("Need to specify explicit path to JSCover-all.jar file."));
            Assert.assertTrue(rteMsg.endsWith("/test"));
        }
    }

    @Test(dependsOnMethods = {"interact8"})
    public void realServer2() {

        int simpleServerPort = 8501;

        HttpServer server = HttpSvr.makeServer(simpleServerPort, new ContextRoot());

        server.start();
        pause(2000);

        // launch the proxy using JscovTestutil
        JscovTestUtil proxySvr = new JscovTestUtil(3131,
                "dd",
                String.format("%s/test/dpends/jscoverInst",
                        System.getProperty("user.dir")));

        proxySvrArr.add(proxySvr);

        proxySvr.setOnCodeCoverage()
                .setOnViewReport()
                .reportIntoTimeStampDir(3)
                .startProxyServer();

        pause(3000);

        WebDriverManager.chromedriver().setup();
        WebDriverManager.firefoxdriver().setup();

        WebDriver ffDriver = proxySvr.webDriverForJSCoverProxy(CHROME);
        webDriverArr.add(ffDriver);

        ffDriver.get(String.format("http://localhost-proxy:%d", simpleServerPort));

        pause(3000);

        proxySvr.viewReportDo(8080);

        proxySvr.setOffViewReport()
                .viewReportDo(8080);

        proxySvr.stopProxyServer(30);

        ffDriver.quit();

        if (new File("c:\\").exists()) {
            proxySvr.setOnViewReport();

            proxySvr.viewReportDo(8080);

            pause(1000);
            try {
                Robot robot = new Robot();

                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_F4);
                robot.delay(10); //set the delay
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_F4);

            } catch (AWTException ex) {
                Logger.getLogger(ServerLoad1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
