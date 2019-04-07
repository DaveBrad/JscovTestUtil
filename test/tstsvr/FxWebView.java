///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package tstsvr;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.net.Proxy;
//import java.net.ProxySelector;
//import java.net.SocketAddress;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.List;
//import javafx.beans.value.ChangeListener;
//import javafx.application.Application;
//import javafx.beans.value.ObservableValue;
//import javafx.scene.Scene;
//import javafx.scene.layout.VBox;
//import javafx.scene.web.WebView;
//import javafx.stage.Stage;
//import javafx.scene.web.WebEngine;
//import static javafx.concurrent.Worker.State;
//import sun.net.www.http.HttpClient;
//
///**
// *
// * @author dbradley
// */
//public class FxWebView extends Application {
//
//    @Override
//    public void start(final Stage stage) {
//        // Create the WebView
//
////        DefaultHttpClient httpClient = new DefaultHttpClient();
////
////        HttpHost proxy = new HttpHost("X.X.X.X", portNumber); //proxy that i need 
////
////        httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
//        HttpClient hclt;
//
//        ProxySelector selector = new ProxySelector() {
//            @Override
//            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
//                throw new UnsupportedOperationException("Not supported yet.");
////To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public List<Proxy> select(URI uri) {
//                ArrayList<Proxy> proxyList = new ArrayList<Proxy>();
//
//                Proxy proxy = new Proxy(Proxy.Type.HTTP,
//                        new InetSocketAddress("localhost", 3128));
//
//                proxyList.add(proxy);
//                return proxyList;
//            }
//        };
//
//        ProxySelector.setDefault(selector);
//
//        System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", "3128");
//
//        WebView webView = new WebView();
//
//        // Create the WebEngine
//        final WebEngine webEngine = webView.getEngine();
//
//        // LOad the Start-Page
////        webEngine.load("http://www.oracle.com");
//        webEngine.load("http://localhost:8500");
//
//        // Update the stage title when a new web page title is available
//        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
//            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
//                if (newState == State.SUCCEEDED) {
//                    //stage.setTitle(webEngine.getLocation());
//                    stage.setTitle(webEngine.getTitle());
//                }
//            }
//        });
//
//        // Create the VBox
//        VBox root = new VBox();
//        // Add the WebView to the VBox
//        root.getChildren().add(webView);
//
//        // Set the Style-properties of the VBox
//        root.setStyle("-fx-padding: 10;"
//                + "-fx-border-style: solid inside;"
//                + "-fx-border-width: 2;"
//                + "-fx-border-insets: 5;"
//                + "-fx-border-radius: 5;"
//                + "-fx-border-color: blue;");
//
//        // Create the Scene
//        Scene scene = new Scene(root);
//        // Add  the Scene to the Stage
//        stage.setScene(scene);
//        // Display the Stage
//        stage.show();
//    }
//}
