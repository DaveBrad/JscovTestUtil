///* Copyright (c) 2019 dbradley.
// */
//package testcases;
//
//import com.machinepublishers.jbrowserdriver.JBrowserDriver;
//import com.machinepublishers.jbrowserdriver.ProxyConfig;
//import com.machinepublishers.jbrowserdriver.Settings;
//import com.machinepublishers.jbrowserdriver.Timezone;
//import com.machinepublishers.jbrowserdriver.UserAgent;
//import com.sun.net.httpserver.HttpServer;
//import org.openqa.selenium.JavascriptExecutor;
//import org.openqa.selenium.Proxy;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.htmlunit.HtmlUnitDriver;
//import org.openqa.selenium.remote.CapabilityType;
//import org.openqa.selenium.remote.DesiredCapabilities;
//import org.testng.annotations.Test;
//import sun.net.www.http.HttpClient;
//import tstsvr.ContextRoot;
//import tstsvr.HttpSvr;
//
///**
// *
// * @author dbradley
// */
//public class ServerLoad_recordofinvestigation {
//    
//     private final static String STOPELEID_MSECOND_REMOVE_TIMER = "3000";
//
//    /**
//     * ID of a DOM element that will cause JscovTestUtil to know that the
//     * jscoverage_report(...) has completed.
//     */
//    private final static String STOPELEID_STR = "stopEleId";
//    /**
//     * Javascript request for report create with on complete callback function
//     * which will append a 'stopEleId' to the DOM. The presence of the
//     * stop-element will cause the stopping of the JSCover proxy server.
//     */
//    final private static String REPORT_COMPLETE_AND_CALLBACK_SCRIPT
//            = "return jscoverage_report('%s', function(){"
//            // callback actions
//            // 1) once report is complete, append to DOM stopEleId element
//            // 2) remove the element from DOM after N milliseconds
//            // - - - by doing this ensures the DOM has no lagging element
//            // - - - in the event JscovTestUtil is interrupted.
//            //
//            + "var bodyEle = document.getElementsByTagName('body')[0];"
//            + "var doStopEle = document.createElement('p');"
//            + "doStopEle.setAttribute('id', '" + STOPELEID_STR + "');"
//            + "bodyEle.appendChild(doStopEle);"
//            + ""
//            // 2) remove element action
//            //
//            // setTimeout( 
//            //  function(){document.getElementById('stopEleId').remove(); },
//            //  1000);
//            //
//            + "setTimeout("
//            + " function(){document.getElementById('" + STOPELEID_STR + "').remove(); },"
//            + STOPELEID_MSECOND_REMOVE_TIMER
//            + ");"
//            + "});";
//
//    HttpClient hc;
//
//    @Test
//    public void launchServer() {
//
//        HttpServer server = HttpSvr.makeServer(8500,
//                new ContextRoot());
//
//        server.start();
//        pause(2000);
//
////        Application.launch(FxWebView.class);
//        pause(3000);
//
////        DesiredCapabilities cap = new DesiredCapabilities();
////Proxy proxy = new Proxy();
////proxy
////        .setHttpProxy("127.0.0.1:3128")
////        .setProxyType(Proxy.ProxyType.MANUAL);
////        cap.setCapability(CapabilityType.PROXY, proxy);
////        
////        JBrowserDriver dddd = new JBrowserDriver(cap);
//        JBrowserDriver dddd;
//        if (true) {
//            Settings.Builder settings = Settings.builder();
//            ProxyConfig pCfg = new ProxyConfig(ProxyConfig.Type.HTTP, "localhost", 3128);
//
//            settings.proxy(pCfg);
//            settings.userAgent(UserAgent.CHROME);
//            settings.timezone(Timezone.EUROPE_LONDON);
//            settings.headless(true);
//            settings.javascript(true);
//            settings.javascript(false); // ??
//            settings.quickRender(true);
//
////            settings.javaOptions("-Dquantum.verbose=true", 
////                    "-Dprism.verbose=true", 
////                    "-verbose", 
////                    "-verbose:class", 
////                    "-Dprism.useFontConfig=false");
//            dddd = new JBrowserDriver(settings.build());
//        } else {
//            dddd = new JBrowserDriver();
//        }
//
//////        dddd.get("http://localhost:8500/testpage.js");
////        dddd.get("http://localhost:8500/");
////
////        String src = dddd.getPageSource();
////        System.err.println(src);
//        int a = 1;
//
//        DesiredCapabilities cap = new DesiredCapabilities();
//
//        Proxy hudProxy = new Proxy();
//        hudProxy.setHttpProxy("localhost:3128")
//                .setProxyType(Proxy.ProxyType.MANUAL);
//
//        cap.setCapability(CapabilityType.PROXY, hudProxy);
//
//        HtmlUnitDriver dddddddd = new HtmlUnitDriver(); // (cap);
//        dddddddd.setProxySettings(hudProxy);
//        dddddddd.setJavascriptEnabled(true);
//
////        dddddddd.setProxy("http://localhost", 3128);
//        dddddddd.get("http://localhost:8500/");
//
//       
//                pause(3000);
//
//        JavascriptExecutor js = (JavascriptExecutor) dddddddd;
//        Object result = js.executeScript("return a");
//
//        int b = 1;
//        
//        String generateReportScript = String.format(REPORT_COMPLETE_AND_CALLBACK_SCRIPT,
//                "ddddd");
//        
//        
//        ((JavascriptExecutor) dddddddd).executeScript(generateReportScript);
//        
////        JFXPanel jfxPanel = new JFXPanel();
////        
////        WebView browser = new WebView();
////        
////        jfxPanel.add(browser);
////        
////        WebEngine webEngine = browser.getEngine();
////        try {
////            webEngine.load(new URL("http://localhost:8500").toString());
////        } catch (MalformedURLException ex) {
////            System.err.println("mal formed 2");
////            Logger.getLogger(ServerLoad.class.getName()).log(Level.SEVERE, null, ex);
////        }
////        
////        WebView dddd;
////        
////        
////        dddd.
////        
////
////        URL yahoo = null;
////        try {
////            yahoo = new URL("http://localhost:8500/");
////        } catch (MalformedURLException ex) {
////            Logger.getLogger(ServerLoad.class.getName()).log(Level.SEVERE, null, ex);
////        }
////        URLConnection yc = null;
////        try {
////
////            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 3128));
//////conn = new URL(urlString).openConnection(proxy);
////
////            yc = yahoo.openConnection(proxy);
////
////            BufferedReader in;
////
////            in = new BufferedReader(
////                    new InputStreamReader(
////                            yc.getInputStream()));
////
////            String inputLine;
////
////            while ((inputLine = in.readLine()) != null) {
////                System.out.println(inputLine);
////            }
////
////            in.close();
////
////        } catch (IOException ex) {
////            Logger.getLogger(ServerLoad.class.getName()).log(Level.SEVERE, null, ex);
////        }
////        HttpClient client = HttpClient.newHttpClient();
////        HttpRequest request = HttpRequest.newBuilder()
////                .uri(URI.create("http://openjdk.java.net/"))
////                .build();
////        client.sendAsync(request, BodyHandlers.ofString())
////                .thenApply(HttpResponse::body)
////                .thenAccept(System.out::println)
////                .join();
////        try {
////            this.hc = HttpClient.New(new URL("http://localhost:8501"), "localhost", 3128, true);
//////            this.hc = HttpClient.New(new URL("http://localhost:8500"),
//////                    Proxy.NO_PROXY.toString(), 3128, true);
////
//////        xxxx = new HttpClient
////        } catch (MalformedURLException ex) {
////            System.err.println("mal formed");
////            Logger.getLogger(ServerLoad.class.getName()).log(Level.SEVERE, null, ex);
////        } catch (IOException ex) {
////            System.err.println("io except 1");
////            Logger.getLogger(ServerLoad.class.getName()).log(Level.SEVERE, null, ex);
////        }
////
////        try {
////            hc.getURLFile();
////        } catch (IOException ex) {
////            System.err.println("io except 21");
////            Logger.getLogger(ServerLoad.class.getName()).log(Level.SEVERE, null, ex);
////        }
////        int a = 1;
////        hc.getURLFile("http://localhost:8500");
////        pause(3000000);
//    }
//
//    // - - - - - - - - - - - - - - - - - - - - - 
//    private void pause(int msecs) {
//        try {
//            Thread.sleep(msecs);
//
//        } catch (InterruptedException ex) {
//            //            Logger.getLogger(ctrl.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//}
//
////package org.kodejava.example.commons.httpclient;
////
////import org.apache.commons.httpclient.Credentials;
////import org.apache.commons.httpclient.HostConfiguration;
////import org.apache.commons.httpclient.HttpClient;
////import org.apache.commons.httpclient.HttpMethod;
////import org.apache.commons.httpclient.HttpStatus;
////import org.apache.commons.httpclient.UsernamePasswordCredentials;
////import org.apache.commons.httpclient.auth.AuthScope;
////import org.apache.commons.httpclient.methods.GetMethod;
////
////import java.io.IOException;
////
////public class HttpGetProxy {
////    private static final String PROXY_HOST = "proxy.host.com";
////    private static final int PROXY_PORT = 8080;
////
////    public static void main(String[] args) {
////        HttpClient client = new HttpClient();
////        HttpMethod method = new GetMethod("https://kodejava.org");
////
////        HostConfiguration config = client.getHostConfiguration();
////        config.setProxy(PROXY_HOST, PROXY_PORT);
////
////        String username = "guest";
////        String password = "s3cr3t";
////        Credentials credentials = new UsernamePasswordCredentials(username, password);
////        AuthScope authScope = new AuthScope(PROXY_HOST, PROXY_PORT);
////
////        client.getState().setProxyCredentials(authScope, credentials);
////
////        try {
////            client.executeMethod(method);
////
////            if (method.getStatusCode() == HttpStatus.SC_OK) {
////                String response = method.getResponseBodyAsString();
////                System.out.println("Response = " + response);
////            }
////        } catch (IOException e) {
////            e.printStackTrace();
////        } finally {
////            method.releaseConnection();
////        }
////    }
////}
/////**
//// *  Create an HTTPS client URL.  Traffic will be tunneled through
//// * the specified proxy server, with a connect timeout
//// */
////HttpsClient(SSLSocketFactory sf, URL url, String proxyHost, int proxyPort,
////            int connectTimeout)
////    throws IOException {
////    this(sf, url,
////         (proxyHost == null? null:
////            HttpClient.newHttpProxy(proxyHost, proxyPort, "https")),
////            connectTimeout);
////}
////protected void proxiedConnect(URL url,
////                                       String proxyHost, int proxyPort,
////                                       boolean useCache)
////    throws IOException {
////    http = HttpClient.New (url, proxyHost, proxyPort, useCache,
////        connectTimeout, this);
////    http.setReadTimeout(readTimeout);
////}
////class MyApp extends Application {
////    public void start(Stage stage) {
////        Circle circ = new Circle(40, 40, 30);
//////        Group root = new Group(circ);
//////        
//////        
//////        
//////        Scene scene = new Scene(root, 400, 300);
////
////        stage.setTitle("My JavaFX Application");
//////        stage.setScene(scene);
////        stage.show();
////    }
////}
