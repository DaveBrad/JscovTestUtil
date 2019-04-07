/*
 * Copyright (c) 2019 dbradley. Original author.
* 
*  License under MIT
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*
 */
package org.jtestdb.selenium.jscov;

import java.awt.Desktop;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import static org.jtestdb.selenium.jscov.JscovBrowserKind.CHROME;
import static org.jtestdb.selenium.jscov.JscovBrowserKind.FIREFOX;
import static org.jtestdb.selenium.jscov.JscovBrowserKind.getTypeForWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.jtestdb.selenium.jscov.JscovTestUtil.ProcessingState.*;

/**
 * Class that is designed to interact with JSCover (a Javascript code-coverage
 * tool) while executing test-case scripts within a Selenium v3 environment.
 * JscovTestUtil class&rsquo; purpose is to provided methods that facilitate
 * getting code-coverage of Javascript for a web-page, without manual
 * intervention to instrument JS-files. It is assumed the user knows the
 * JS-files the web-page and web-site will present, so as to allow management
 * and control (include/exclude) code-coverage capture needs.
 * <p>
 * Licensed under MIT.
 * <p>
 * <a href="./doc-files/jtuhow.html">Supplemental documentation</a>: please take
 * the time to read the supplement, it provides some under-the-hood
 * understanding of JscovTestUtil and interaction with JSCover and IDE
 * integration.
 * <p>
 * <a href='#summtop'>Skip to summaries: constructor</a>
 * </p>
 *
 * <h3>Test scripting</h3>
 * <p>
 * Test-case code/scripts are written normally, navigating the
 * webpage-under-test (WUT) to do tests. JscovTestUtil provides methods that
 * 'bracket' around the test-case scripts to control interaction with the
 * JSCover program. This bracketing is most effective in the 'before-suite',
 * 'before-class', 'after-class' and 'after-suite' methods used for test-script
 * setup/configuration arrangements.
 * <p>
 * JscovTestUtil does not support multi-threaded operation, implying
 * test-scripts need to run in sequence, rather than parallel.
 * <div class='indtpp'>
 * Why is this the case?
 * <div class='indtpp'>
 * <p>
 * JSCover is used to perform the code-coverage and is external to the
 * test-scripts: getting reports causes writing to files. These writes do not
 * support concurrent usage, thus not conducive to parallel test-script report
 * calls.
 * <p style="margin-bottom: 3px;">
 * However:</p>
 * <div class='indtpp'>
 * <p>
 * Parallel testing is possible by the test-scripts interacting directly with
 * the Javascript code (once the web-page is loaded)
 * <br><code>&nbsp;&nbsp;&nbsp;
 * ((JavascriptExecutor) someWebDriver.executeScript(&lt;</code>
 * <span style="font-style: italic;">java-script method</span>
 * <code>&gt;);</code>
 * <br>As documented in the JSCover manual about running in a unit-test mode.
 * <p>
 * Rather than driving the Javascript through actions/invocations via the
 * web-page elements: which would typically be in sequence actions in a manner
 * similar to actions through the UI.
 * <br><code>&nbsp;&nbsp;&nbsp;
 * </code> do 'buttonElement.click();' which calls <code>buttonAction()
 * </code>
 * </div></div></div>
 *
 *
 * <h3>How JscovTestUtil works</h3>
 * <i>See supplemental <a href="./doc-files/jtuhow.html">
 * Supplemental documentation</a></i>.
 * <p>
 * The following diagram and its ordered steps is a brief on how JscovTestUtil
 * interacts with hi-level "components" to do Javascript code-coverage, in a
 * test Selenium (Java) and @Test framework environment.
 * </p>
 * <div>
 * <img src="./doc-files/interact.png"
 * onerror="this.oneerror = null; this.src='../doc-files/interact.png';"
 * alt="missing image">
 * </div>
 * <ol>
 * <li><i>Test setup</i>:
 * <ul><li>Start the proxy-server via ProcessBuilder from the JSCover executable
 * directory.</li></ul>
 * </li>
 * <li><i>Testing</i>:
 * <ul><li>Begin executing tests, perform GET-URL action.
 * <ul><li>
 * <i>Proxy-server</i>: passes <b>GET</b> requests directly to web-server.
 * </li></ul>
 * </li></ul>
 * </li>
 * <li><i>Web-server</i>:
 * <ul><li>Respond to <b>GET</b> requests and sends files back to requestor (for
 * files: HTML, CSS, JS,...).</li></ul>
 * <ol type="a">
 * <li><i>Proxy-server</i>: Javascript file(s) are intercepted by JSCover
 * proxy-server and instrumented, if not excluded from instrumentation.</li>
 * <li><i>Proxy-server</i>: Instrumented files are forwarded to browser.</li>
 * <li><i>Proxy-server</i>: Excluded from instrumentation files are
 * passed-through to the browser.</li>
 * <li><i>Proxy-server</i>: Records the original Javascript and retains them in
 * the JSCover reports directory.</li>
 * </ol>
 * </li>
 * <li><i><b>Testing</b></i>:
 * <ul><li>
 * <b>Performs normal test actions via the WebDriver API.</b>
 * </li></ul></li>
 * <li><i>Testing/capture report</i>:
 * <ul><li>invoke a store-report at end of each test, or during testing, or
 * whenever.</li></ul>
 * <ol type="a">
 * <li><i>Instrumented Javascript</i>: Invoke the jscoverage_report(...)
 * function in the instrumented Javascript. This will cause the script to
 * forward captured results to the proxy-server directly.</li>
 * <li><i>Proxy-server</i>: Receives jscoverage JSON and other datum which will
 * be formatted and converted into JSCover report format files.
 * </li>
 * <li><i>Proxy-server</i>: Report-files are stored in the JSCover reports
 * directory. JSCover will also merge the data if existing datum files
 * present.</li>
 * </ol>
 * </li>
 * <li><i>Testing/stop</i>:
 * <ul><li>On testing stop a request to terminate and stop the proxy-server is
 * "called". This will cause a store-report first before stopping the JSCover
 * proxy-server process on the system.
 * </li></ul></li>
 * </ol>
 * <p>
 * JscovTestUtil requires Selenium V3 to be in place as a class-path library for
 * testing purposes, alongside the JSCover program files/structure installed on
 * the local machine. (See
 * <a target="_blank" href="https://tntim96.github.io/JSCover">
 * https://tntim96.github.io/JSCover</a> for information about JSCover.)
 * <h3>Dependencies</h3>
 * <table class='deptab'>
 * <caption style="display: none;">Dependencies table</caption>
 * <tr>
 * <th>class-path</th>
 * <th>installed program or executable</th>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">1) J2SE/JSEE V8</td>
 * <td style="vertical-align: top;">3) JSCover
 * <p style="margin-top: 0px; margin-bottom: 3px; padding-left: 2ch; text-indent: -2ch;">
 * -- Needs the installation on a local machine of the JSCover program/software
 * (see <a target="_blank" href="https://tntim96.github.io/JSCover">
 * https://tntim96.github.io/JSCover</a>).</p></td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">2) org.openqa.selenium classes
 * <p style="margin-top: 0px; margin-bottom: 3px; padding-left: 2ch; text-indent: -2ch;">
 * -- no selenium code has been modified</td>
 * <td style="vertical-align: top;">4) WebDriver executables
 * <p style="margin-top: 0px; margin-bottom: 3px; padding-left: 2ch; text-indent: -2ch;">
 * -- These are the Selenium drivers that need to be put in place for normal
 * Selenium operation.</td>
 * </tr>
 * </table>
 *
 * <p id="summtop">Top of summaries</p>
 *
 * @author dbradley
 */
public class JscovTestUtil {

    /**
     * An additional script provided for the viewReport method so that the
     * jscover.simple.server can be stopped. This script allows the report to be
     * changed to reflect this needed action to stop the server.
     */
    final private static String JSCOV_TEST_UTIL_REPORT_SCRIPT
            = "function stopServer(){\n"
            + "var protocol = window.location.protocol;\n"
            + "\n"
            + " if(!protocol.toUpperCase().startsWith('HTTP')){\n"
            + "  return;\n"
            + " }\n"
            + " var hostName = window.location.hostname;\n"
            + " var portNo = window.location.port;\n"
            + " window.location.replace(protocol + '//' + hostName + ':' + portNo + '/stop');\n"
            + "}\n"
            + "\n"
            + "function onloadjscov(){\n"
            + " var protocol = window.location.protocol;\n"
            + "\n"
            //
            + " var divele = document.createElement('div');\n"
            + " divele.setAttribute('style', 'padding-left: 100px;');\n"
            //
            + " var pele = document.createElement('p');\n"
            + " pele.innerHTML = ' (JscovTestUtil modified) ';\n"
            + " pele.setAttribute('style', 'display: inline;');\n"
            + " divele.appendChild(pele);\n"
            //
            + " if(protocol.toUpperCase().startsWith('HTTP')){\n"
            + " \n"
            //
            + "  var buttonele = document.createElement('button');\n"
            + "  buttonele.setAttribute('onclick', 'stopServer();');\n"
            + "  buttonele.setAttribute('style', 'border-radius: 10px;');\n"
            + "  buttonele.innerHTML = 'Stop the simple-server';\n"
            + "  divele.appendChild(buttonele);\n"
            + " }\n"
            + " var h1JSCoverEle = document.getElementsByTagName('h1')[0];\n"
            + " h1JSCoverEle.parentNode.insertBefore(divele, h1JSCoverEle.nextSibling);\n"
            + "}\n";
    /**
     * When JscovTestUtil detects that jscoverage_report(...) has completed ( by
     * the DOM element with ID STOPELEID_STR being appended) the trigger is
     * removed from DOM automatically within this time period.
     */
    private final static String STOPELEID_MSECOND_REMOVE_TIMER = "3000";

    /**
     * The prefix string used in the timestamp report name.
     * <pre>
     * '12345
     * '.....1234567890123456789
     * 'jscovYYYYMMdd_mmhhss_mmm
     * </pre>
     */
    private final static String REP_TIMESTAMP_PREFIX = "jscov";

    /**
     * The length of the prefix and the time-stamp portion for optimized
     * directory processing for clean up
     */
    private final static int REP_TIMERSTAMP_LENGTH = 19 + REP_TIMESTAMP_PREFIX.length();

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

    /**
     * The WebDriver executable location is provided within the project (IDE).
     */
    private final static String USER_DIR = System.getProperty("user.dir");

    /**
     * The directory location to the WebDriver executable files organized into a
     * structure by OS-platform with browser specific formats.
     */
    private static String webDriverLocation = String.format("%s/webdrvrexe",
            USER_DIR);

    /**
     * The location to the JSCover jscover-all.jar file that is used to launch
     * the proxy-server and/or the simple-server.
     */
    private File jscoverAllJarFile = null;

    /**
     * The driver that is allocated for interaction with JSCover and its
     * proxy-server and on-the-fly instrumentation.
     * <p>
     * This driver will have a proxy profile added to it.
     */
    private WebDriver jscoverDriverWithProxySet = null;

    /**
     * The port number for JSCover proxy-server that will be launched by
     * JscovTestUtil and associated with 'this.jscoverDriverOfProxyServer'.
     */
    private int jscoverLocalPortNo;

    /**
     * The local proxy-server and port required for processing via browser
     * interfaces and settings in string format.
     * <p>
     * e.g. 'localhost-proxy:ppppp' (where ppppp is port number of proxy-server
     */
    private String jscoverLocalProxyAndPortString = "";

    /**
     * Location of the JSCover directory, assumed to be associate within an IDE,
     * but may be changed using the JscobTestUtil constructor with the
     * 'externalJscoverDir' parameter.
     */
    private String locationOfJscoverDir
            = String.format("%s/jscoverInst", System.getProperty("user.dir"));

    /**
     * The path to the JSCover structures which contain the JSCover-all.jar
     * which will be used to launch the JSCover proxy-server.
     * <p>
     * This maybe internal in an IDE or external stored in a system directory.
     */
    private String pathToJSCoverAllJar = null;

    /**
     * The processing state of the JscovTestUtil object.
     */
    private ProcessingState processingState = NOT_STARTED;

    /**
     * ??
     */
    private boolean offCodeCoverage = true;

    /**
     * ??
     */
    private boolean startProxyServerInvoked = false;

    /**
     * The directory within 'locationOfJscoverDir' to place the report into.
     */
    private String reportDir;

    /**
     * The sub-directory to further report into, this is controlled by
     * reportIntoTimestamp and reportIntoSubDir.
     */
    private String reportFurtherSubDirOrTimeStamp = "";

    /**
     * The number of time-stamp reports to retain in the "reports' directory.
     */
    private int retainNTimestampReports = 0;
    /**
     * The state of launching the view-report capability once the testing is
     * stopped. Default is off.
     */
    private boolean viewReportLaunchOff = true;

    /**
     * Storage for any JSCover options the user my wish to add.
     */
    private final ArrayList<String> additionalOptionsArr = new ArrayList<>();

    /**
     * Create a JscovTestUtil object where the JSCover-directory is contained
     * within the "user-dir" of the current running environment (an IDE may
     * be)&#46;
     * <br>
     * <br>
     * The JSCover-directory for this mode is
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ' &lt;user-dir&gt;/jscoverInst '
     * <br>
     * and as such the JSCover zip file needs to be extracted into the
     * 'jscoverinst' directory directly.
     * <p>
     * The 'JSCover-all.jar' used for running the proxy-server is located in the
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; '
     * &lt;user-dir&gt;/jscoverInst/target/dist '
     * <br>directory, which JscovTestUtil will assume to be the case.
     *
     * @param portNo the port number of the JScover proxy-server
     *
     * @param reportDirectory string that is a sub-directory to use for storing
     * reports into (as per JSCover 'local-storage' option) [this directory will
     * be created if missing in '&lt;user-dir&gt;/jscoverInst']
     * <br><br>
     * Its is recommended that the user sets this reportDirectory to be
     * "ignored" by GIT/HG/SVN/... source code management on their IDE project.
     */
    public JscovTestUtil(int portNo, String reportDirectory) {
        this.jscoverLocalPortNo = portNo;
        this.reportDir = reportDirectory;

        this.jscoverLocalProxyAndPortString
                = String.format("localhost-proxy:%d", jscoverLocalPortNo);
    }

    /**
     * Create a JscovTestUtil object where the JSCover-directory is an external
     * directory and separate from the test environment&#46;
     * <br><br>
     * This will require the user to have some experience in using and setting
     * up JSCover on their system&#46; The external directory will allow the
     * user to manage multiple test environments using the same
     * JSCover-structures.
     *
     * @param portNo the port number of the JSCover proxy-server
     *
     * @param reportDirectory string that is a sub-directory to use for storing
     * reports into (as per JSCover 'local-storage' option) [this directory will
     * be created if missing in the external directory location
     * (locationOfJscoverDir)].
     *
     * @param externalJscoverDir the location of the JSCover-directory where the
     * JScover files are, as an absolute file path on the local system
     */
    public JscovTestUtil(int portNo, String reportDirectory, String externalJscoverDir) {
        this(portNo, reportDirectory);

        this.locationOfJscoverDir = externalJscoverDir;
    }

    /**
     * Get the proxy-server as a string ('http://localhost-proxy:nnnn').
     *
     * @return string of the proxy-server URL
     */
    public String getProxyServerString() {
        return this.jscoverLocalProxyAndPortString;
    }

    /**
     * Allocate a WebDriver for the JscovTestUtil kind of browser that is not a
     * JscovTestUtil JSCover configured browser.
     *
     * @param browserKind type of browser to allocate driver for that
     * JscovTestUtil supports
     *
     * @return WebDriver object for a browser kind
     */
    static public WebDriver webDriverForBrowser(JscovBrowserKind browserKind) {
        WebDriver driverL = null;

        if (!browserKind.isActiveBrowserKind()) {
            throw new RuntimeException(
                    String.format("Browser kind %s is denied/not active\n"
                            + "this may be due to browser stability issues.",
                            browserKind.toString()));
        }
        // allocare a WebDriver for the browser kind
        switch (browserKind) {
            case CHROME:
                driverL = new ChromeDriver();
                break;

            // case EDGE:
            //     // 'WebDriverException: Unable to parse remote response: Unknown'
            //     // means trying to create another session. EDGE does not support
            //     // more than one driver session. MS says to use a GRID arrangement,
            //     // but these tests are limited to a single processing engine.
            //     driverL = new EdgeDriver();
            //     break;
            case FIREFOX:
                driverL = new FirefoxDriver();
                break;

            // case OPERA:
            //    OperaOptions opOptions = new OperaOptions();
            //    opOptions.setBinary(OPERA.getExecutePath());
            //    driverL = new OperaDriver(opOptions);
            //    break;
            default:
                throw new RuntimeException("Using a browser kind not coded.");
        }
        Dimension winDim = new Dimension(750, 400);
        driverL.manage().window().setSize(winDim);

        return driverL;
    }

    /**
     * Allocate a same-as WebDriver type based on another driver object
     * provided.
     * <p>
     * NOTE: EDGE does not support more than 1 session, so a substitute will be
     * provided: CHROME.
     *
     * @param otherDriver type of browser to allocate driver for
     *
     * @return WebDriver object
     */
    static public WebDriver webDriverForTypeSameAs(WebDriver otherDriver) {
        WebDriver driverL = null;
        try {
            driverL = webDriverForBrowser(getTypeForWebDriver(otherDriver));

        } catch (WebDriverException e) {
            if (e.getMessage().startsWith("Unable to parse remote response")) {
                // attempting to open another EDGE  session, but this
                // is only allowed in GRID mode
                //
                // SO, use a CHROME browser as the 2nd/Nth subsitute

                System.out.printf("*******: Substituting chrome WebDriver as  ");
                driverL = new ChromeDriver();
            }
        }
        return driverL;
    }

    /**
     * Delete the files within a directory, its sub-directories and finally the
     * top level directory.
     *
     * @param fileOrDir FIle object for directory or file to delete
     */
    private static void deleteDir(File fileOrDir) {
        File[] listOfFileDir = fileOrDir.listFiles();
        if (listOfFileDir != null) {
            // have items in the list
            for (File childFile : listOfFileDir) {
                if (childFile.isDirectory()) {
                    deleteDir(childFile);
                } else {
                    childFile.delete();
                }
            }
        }
        fileOrDir.delete();
    }

    /**
     * Clean the reports directory of all sub-directories effectivily deletes
     * all the held reports&#46; <br><br>
     * <span style="color: red;">WARNING: there is no recovery from this action
     * :WARNING</span>
     */
    public void removeAllReports() {
        deleteDir(new File(this.locationOfJscoverDir, getreportDirBase()));
    }

    /**
     * Get the last time-stamped report name from the reports directory, null if
     * not found.
     *
     * @return String of the last time-stamp report within the reports
     * directory, or null if on is not found
     */
    public String getLastTimeStampReport() {
        List<String> arrListDirs = getTimestampDirList();

        int siz = arrListDirs.size();
        if (siz == 0) {
            return null;
        }
        // 
        return arrListDirs.get(siz - 1);
    }

    /**
     * Reporting is to reuse the time-stamp directory that is provided&#46; This
     * allows merging of results/reports into a single time-stamped
     * directory&#46; Or a none time-stamp name may be used to override as a
     * standalone sub-directory to report into.
     * <p>
     * Use the {@link #getLastTimeStampReport()} to get the name of the last
     * time-stamp report name.
     *
     * @param retainNTimestampReports the number of report directories with
     * time-stamps to be retained in the reporting directory (0 means retain
     * all)
     *
     * @param reuseTimeStampDirectory the directory-name of an existing
     * time-stamped directory. (No check is made for existence or if its a
     * time-stamped name. Additional, if its a none time-stamped name it will
     * override the existing report sub-directory setting).
     *
     * @return this object so as to cascade settings
     *
     * @exception RuntimeException if retainNTimestampReports is negative
     * @exception RuntimeException if reuseTimeStampDirectory is null or an
     * empty string
     */
    public JscovTestUtil reportIntoReuseDir(int retainNTimestampReports, String reuseTimeStampDirectory) {
        checkRetainNMakesSense(retainNTimestampReports);

        // if the capture is started, this will have no effect so things
        // will not be changed mid-way
        if (this.processingState == NOT_STARTED
                || this.processingState == STARTED_AND_STOPPED) {

            if (reuseTimeStampDirectory == null) {
                throw new RuntimeException("'reuseTimeStampDirectory' is null, not allowed.");
            }
            if (reuseTimeStampDirectory.trim().isEmpty()) {
                throw new RuntimeException("'reuseTimeStampDirectory' is empty string, not allowed.");
            }

            this.retainNTimestampReports = retainNTimestampReports;
            this.reportFurtherSubDirOrTimeStamp = reuseTimeStampDirectory;
        }
        return this;
    }

    /**
     * Set report into a sub-directory of the
     * "reports/&lt;<i>reportDirectory</i>&gt;" directory with furtherSubDir
     * value (&lt;<i>reportDirectory</i>&gt; is set by the constructor call).
     * This will cause the retain value for time-stamps to be reset to 0.
     * <p>
     * The path for the reports to be placed is:
     * <pre>
     * &lt;<i>JSCover-directory</i>&gt;/reports/&lt;<i>reportDirectory</i>&gt;/&lt;<i>furtherSubDir</i>&gt;/
     * </pre>
     *
     * @return this object so as to cascade settings
     *
     * @param furtherSubDir name of the sub-directory to use
     */
    public JscovTestUtil reportIntoSubDir(String furtherSubDir) {
        if (this.processingState == NOT_STARTED
                || this.processingState == STARTED_AND_STOPPED) {
            this.retainNTimestampReports = 0;
            this.reportFurtherSubDirOrTimeStamp = furtherSubDir;
        }
        return this;
    }

    /**
     * Set report into the "reports/&lt;<i>reportDirectory</i>&gt;" directory
     * and into a time-stamped sub-directory (jscovYYYYMMDD_hhmmdd_mmm)
     * (&lt;<i>reportDirectory</i>&gt; is set by the constructor call).
     * <p>
     * Only if the JscovTestUtil is running and started, otherwise this call is
     * ignored. That is, reporting will remain into the same directory during
     * active running.
     * <p>
     * The path for the reports to be placed is:
     * <pre>
     * &lt;<i>JSCover-directory</i>&gt;/reports/&lt;<i>reportDirectory</i>&gt;/&lt;<i>jscovYYYYMMDD_hhmmdd_mmm</i>&gt;/
     * </pre>
     *
     * @param retainNTimestampReports the number of report directories with
     * time-stamps to be retained in the reporting directory (0 means retain
     * all) after code-coverage is stopped
     *
     * @return this object so as to cascade settings
     *
     * @exception RuntimeException if retainNTimestampReports is negative
     */
    public JscovTestUtil reportIntoTimeStampDir(int retainNTimestampReports) {
        checkRetainNMakesSense(retainNTimestampReports);

        // if the capture is started, this will have no effect so things
        // will not be changed mid-way
        if (this.processingState == NOT_STARTED
                || this.processingState == STARTED_AND_STOPPED) {

            this.retainNTimestampReports = retainNTimestampReports;

            LocalDateTime nowDateTime = LocalDateTime.now();
            DateTimeFormatter dateFmtr = DateTimeFormatter.ofPattern("YYYYMMdd_HHmmss_SSS");

            this.reportFurtherSubDirOrTimeStamp
                    = String.format("%s%s",
                            REP_TIMESTAMP_PREFIX, nowDateTime.format(dateFmtr));
        }
        return this;
    }

    /**
     * Set the JscovTestUtil object to a not-active mode so that actual
     * code-coverage does not happen&#46; HOWEVER, the test case script do not
     * need to be changed in their use of start, store-report and stop method
     * calls.
     * <p>
     * Test-case code may be written with the JscovTestUtil start, stop and
     * store-report calls remaining in place. The
     * 'setOnCodeCoverage/setOffCodeCoverage' methods are used to cause the on
     * or off state of code-coverage-capture. Implying, in the test environment
     * the user only needs to explicitly code the setOnCodeCoverage or
     * setOffCodeCoverage methods.
     *
     * @return this object so as to cascade settings
     *
     * @exception RuntimeException if the object is in a started proxy-server
     * state already.
     */
    public JscovTestUtil setOffCodeCoverage() {
        setNotActiveStateInternal(true);
        return this;
    }

    /**
     * Set the view-report state to OFF when the <code>viewReportDo</code>
     * method is invoked within the test-scripts&#46; The view-report will not
     * popup.
     *
     * @return this object so as to cascade settings
     */
    public JscovTestUtil setOffViewReport() {
        this.viewReportLaunchOff = true;
        return this;
    }

    /**
     * Set the JscovTestUtil object to an active mode so that code-coverage
     * capture will happen as the start, store-report and stop methods are
     * invoked.
     * <p>
     * Test-case code may be written with the JscovTestUtil start, stop and
     * store-report calls remaining in place. The
     * 'setOnCodeCoverage/setOffCodeCoverage' methods are used to cause the on
     * or off state of code-coverage-capture. Implying, in the test environment
     * the user only needs to explicitly code the setOnCodeCoverage or
     * setOffCodeCoverage methods.
     *
     * @return this object so as to cascade settings
     *
     * @exception RuntimeException if the object is in a started proxy-server
     * state already.
     */
    public JscovTestUtil setOnCodeCoverage() {

        if (this.offCodeCoverage) {
            if (this.startProxyServerInvoked) {
                throw new RuntimeException("\nERROR: setOnCodeCoverage() needs to be invoked prior to startProxyServer().");
            }
        }

        if (this.processingState == NOT_STARTED
                || this.processingState == STARTED_AND_STOPPED) {
            setNotActiveStateInternal(false);
        } else {
            throw new RuntimeException("\nCRITICAL: proxy-server already running.");
        }
        return this;
    }

    /**
     * Set the view-report state to ON when the <code>viewReportDo</code> method
     * is invoked within the test-scripts&#46; This will result in an
     * independent process being launched and popup a browser to the "reports".
     *
     * @return this object so as to cascade settings
     */
    public JscovTestUtil setOnViewReport() {
        this.viewReportLaunchOff = false;
        return this;
    }

    /**
     * Start the proxy-server, checking that resources are in place&#46; Start
     * may be called many times, only the first one will do the actual start.
     * <p>
     * The start will pause for 2 seconds to allow processes to actually start.
     *
     * @exception RuntimeException 1) location issues for the 'jscover'
     * directory, or 2) 'target/dist/JSCover-all.jar' is not found as expected.
     * 3) Failed to start the proxy-server.
     */
    public void startProxyServer() {
        this.startProxyServerInvoked = true;

        File testJscoverDir = new File(locationOfJscoverDir);

        if (!testJscoverDir.isDirectory()) {
            this.startProxyServerInvoked = false;

            throw new RuntimeException(String.format("Location of JSCover directory not found:\n%s",
                    locationOfJscoverDir));
        }

        if (this.offCodeCoverage
                || this.processingState == STARTED
                || this.processingState == BROWSER_TYPE_ERROR) {
            return;
        }
        // by default this should contain a target/dist/JScover-All.jar
        //
        //?? if not try to find it
        if (jscoverAllJarFile == null) {
            pathToJSCoverAllJar = "target/dist/JSCover-all.jar";

            File testJscoverAllFile = new File(
                    String.format("%s/%s", locationOfJscoverDir, pathToJSCoverAllJar));

            if (testJscoverAllFile.isFile()) {
                jscoverAllJarFile = testJscoverAllFile;
            } else {
                //?? find the file as an alternative, so as to deal with 
                //?? future changes
                throw new RuntimeException(
                        String.format("Need to specify explicit path to JSCover-all.jar file.\n"
                                + "As not fould in: %s", locationOfJscoverDir));
            }
        }
        // have the location of the JScover-all.jar file
        //
        // a start of the proxy-server may still fail due to port-no being in use
        //
        // however, begin the proxy-server and in the JSCover directory
        actualStart();

        // allow the proxy-server to start-up and begin processing
        pause(2000);
    }

    /**
     * Stop the proxy-server running but first perform a 'store-report' action
     * to create/append code-coverage to the reports directory. Wait for a
     * time-out period to get the report.
     * <p>
     * Attempting to get a report from a 'stop' or 'report' action without prior
     * use of 'webDriverForJSCoverProxy()'/'overrideWebDriveForJSCoverProxy'
     * will not cause a report to generate and an ERR INFO message provided.
     * <p>
     * Any storage management is performed (applies to time-stamped capability
     * report capture).
     * <p>
     * The stop will pause for 2 seconds to allow processes to actually stop.
     * <p>
     * Processing to get reports is done in the browser/WebDriver and is
     * external to this class. The user needs to determine a good time-out value
     * for their environment.
     *
     * @param timerForJscoverReport2Complete time (seconds) allowed to process
     * the code-coverage captured data into a report (small Javascript low
     * time-out lots-of/large files more time needed [also, CPU speed or file
     * access arrangement considerations].
     *
     * @exception TimeoutException if there is a time-out, consider lengthening
     * the time-out for the web-page report generation
     *
     * @exception JavascriptException cannot find jscoverage_report in the
     * web-page being processed (likely web-page not instrumented or page has
     * closed before calling this method)
     */
    public void stopProxyServer(int timerForJscoverReport2Complete) {
        this.startProxyServerInvoked = false;

        if (this.offCodeCoverage
                || this.processingState == BROWSER_TYPE_ERROR
                || this.processingState == STARTED_AND_STOPPED) {
            // JSCover is not active so do no action. This allows invokes
            // of JscovTestUtil methods to remain within test-cases, but
            // not actively be running.
            return;
        }
        if (this.processingState == NOT_STARTED) {
            throw new RuntimeException("Proxy-server has not been started.");
        }
        if (timerForJscoverReport2Complete < 12) {
            timerForJscoverReport2Complete = 12; // seconds, small project
        }
        actualStop(timerForJscoverReport2Complete);

        cleanTimestampDirs();
    }

    /**
     * Teardown the proxy-server without any reports&#46; In the event of
     * test-case failure with the proxy-server in started mode, the user will
     * need to teardown the server.
     * <p>
     * <code>tearDownProxyServer</code> may be used at the beginning of a
     * test-case with the force condition set. Invoking between 'start' and
     * 'stop' states will impact processing in an undetermined manner.
     * <p>
     * The ON or OFF states will remain as previously set.
     *
     * @param force true to send a stop message to the proxy-server
     */
    public void tearDownProxyServer(boolean force) {
        if (this.processingState == STARTED || force) {
            stopAJscoverServer(jscoverLocalProxyAndPortString, true);
        }
        //
        this.startProxyServerInvoked = false;
        this.processingState = NOT_STARTED;
    }

    /**
     * Store the report into the JSCover-structure-directory as provided in the
     * report-directory provided via the constructor methods&#46; JSCover
     * support the automatic merging of reports while reporting into the same
     * directories, this allows multiple store-report calls allowed.
     * <p>
     * In the event that a test-case fails at least some report information may
     * be collected.
     * <p>
     * Attempting to get a report from a 'stop' or 'report' action without prior
     * use of 'webDriverForJSCoverProxy()'/'overrideWebDriveForJSCoverProxy'
     * will not cause a report to generate and an ERR INFO message provided.
     * <p>
     * Concurrent test-cases doing store-reports is not supported. It may work,
     * but?
     *
     * @param timerForJscoverReport2Complete time (seconds) allowed to process
     * the code-coverage captured data into a report (small Javascript low
     * time-out lots-of/large files more time needed [also, CPU speed or file
     * access arrangement considerations].
     *
     * @exception TimeoutException if there is a time-out, consider lengthening
     * the time-out for the web-page report generation
     *
     * @exception JavascriptException cannot find jscoverage_report in the
     * web-page being processed (likely web-page not instrumented or page has
     * closed before calling this method)
     */
    public void storeJscoverReport(int timerForJscoverReport2Complete) {
        storeJscoverReportToDirInternal(timerForJscoverReport2Complete);
    }

    /**
     * View report in the default-browser when the testing has completed. This
     * is an involved process of:
     * <ol>
     * <li>Determine if view-report is ON and the system is in a
     * started-&amp;-stopped state.</li>
     * <li>Stop an existing JSCover SimpleWebServer using the same port number.
     * <p>
     * This is done to deal with any changes of the JSCover installed directory
     * between test runs.</li>
     * <li>Start a NEW independent JSCover SimpleWebServer process with the port
     * number provided. </li>
     * <li>Launch the default browser and load the last report generate HTML
     * file for viewing.</li>
     * </ol>
     * <p style="padding-left: 5ch; text-indent: -5ch;">
     * Note: JSCover produces 'jscoverage.html' as the viewer-able report file,
     * but JscovTestUtil modifies the file to 'jscovtestutil.html'. An
     * additional added button to "stop the simple-server" is incorporated into
     * 'jscovtestutil.html'.</p>
     * <p style="padding-left: 5ch; text-indent: -5ch;">
     * Only HTTP or HTTPS protocols support the stop button.</p>
     *
     * @param viewingServerPortNo port number for the simpler-server
     */
    public void viewReportDo(int viewingServerPortNo) {

        if (this.processingState != STARTED_AND_STOPPED
                || this.viewReportLaunchOff) {
            return;
        }
        // stop the current simple-server in the event the process is running
        // and was associated with a different working-directory for the
        // ProcessBuilder
        //
        // Why: users may change their arrangement to use a different
        // JSCover structure. With the previous setup the same port will
        // be pointing/using the previous JSCover structures if the user
        // doesn't release/stop the server.
        //
        // the server may have been stopped by the user, so ignore connection
        // exception 
        stopAJscoverServer(String.format("localhost:%d", viewingServerPortNo), true);
        pause(3000); // give time for the simple-server to shutdown

        String portNoStr = String.format("%d", viewingServerPortNo);

        // start the processing JSCover simple-server so as to create
        // a local server for seeing reports with ( the simple-server requires
        // the working directory to be in position
        ProcessBuilder procJscovSimpleWebServer = new ProcessBuilder("java",
                "-cp",
                pathToJSCoverAllJar,
                "jscover.server.SimpleWebServer",
                ".",
                portNoStr);

        try {
            procJscovSimpleWebServer.directory(new File(locationOfJscoverDir));
            procJscovSimpleWebServer.start();

        } catch (IOException ex) {
            System.err.printf("\n*******Unable to launch JSCover simple web server.\n",
                    ex.getMessage());
            return;
        }
        String subDirectoryForReport = this.getreportDirBase();

        if (!this.reportFurtherSubDirOrTimeStamp.isEmpty()) {
            subDirectoryForReport += "/" + this.reportFurtherSubDirOrTimeStamp;
        }
        // do a modification to the jscovage.html file to provide a stop
        // link
        String reportPathAbs = String.format("%s/%s/",
                this.locationOfJscoverDir, subDirectoryForReport);

        String content = "";
        try {
            String jscoverageHtmlFilePath
                    = String.format("%s/jscoverage.html", reportPathAbs);

            content = new String(Files.readAllBytes(
                    Paths.get(jscoverageHtmlFilePath)), "UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException("\n******: critical IOException reading jscoverage.html file.");
        }
        // add in the jscovtestutil.js file for the report to use for
        // processing the stop simple-server button to the reports
        //
        String replaceWith = String.format(
                "<script type='text/javascript' "
                + "src='jscovtestutil.js'></script>\n</head>");

        content = content.replace("</head>", replaceWith);

        // update the onload to do the onloadjscov button for simple-server stop
        // action function
        //
        content = content.replace("<body onload=\"jscoverage_body_load();\"",
                "<body onload=\"jscoverage_body_load(); onloadjscov();\"");

        // introduce the two newjscovtestutil.html/.js files for the reports
        //
        String jscovTstUtlReportName = "jscovtestutil.html";
        String jscoverage2HtmlFilePath
                = String.format("%s/%s", reportPathAbs, jscovTstUtlReportName);
        try {
            Files.write(Paths.get(jscoverage2HtmlFilePath), content.getBytes());
        } catch (IOException e) {
            // e.printStackTrace();
            System.err.printf("'viewReportDo' failed to update report file\n"
                    + "   %s\n"
                    + "   reason: %s\n",
                    jscoverage2HtmlFilePath,
                    e.getMessage());
            return;
        }
        String jscovFilePath
                = String.format("%s/jscovtestutil.js", reportPathAbs);
        try {
            Files.write(Paths.get(jscovFilePath), JSCOV_TEST_UTIL_REPORT_SCRIPT.getBytes());
        } catch (IOException e) {
            System.err.printf("'viewReportDo' failed to write file\n"
                    + "   %s\n"
                    + "   reason: %s\n",
                    jscovFilePath,
                    e.getMessage());
            return;
        }
        //
        //
        // launch the simple server for viewing the report that is a modification
        // of jscoverage.html
        //
        String urlSimpleSevr = String.format("http://localhost:%s/%s/%s",
                portNoStr, subDirectoryForReport, jscovTstUtlReportName);

        try {
            Desktop.getDesktop().browse(new URL(urlSimpleSevr).toURI());

        } catch (MalformedURLException ex) {
            System.err.printf("\n******* MalformedURLException: trying to launch: %s\n",
                    ex.getMessage());
        } catch (URISyntaxException ex) {
            System.err.printf("\n******* URISyntaxException: trying to launch: %s\n",
                    ex.getMessage());
        } catch (IOException ex) {
            System.err.printf("\n******* IOException: trying to launch: %s\n",
                    ex.getMessage());
        }
    }

    /**
     * WARNING: This method will override the proxy-set Webdriver with the class
     * object&#46; It is intended for testing WebDriver classes (such as
     * HtmlUnitDriver, which would require special class-path arrangements
     * outside of normal Selenium V3 main stream browsers. ??
     *
     *
     * @param aProxyConfiguresWebDriver the driver with proxy settings set.
     *
     * @return the driver object
     */
    public WebDriver overrideWebDriveForJSCoverProxy(WebDriver aProxyConfiguresWebDriver) {
        this.jscoverDriverWithProxySet = aProxyConfiguresWebDriver;
        return aProxyConfiguresWebDriver;
    }

    /**
     * Allocate a WebDriver for JSCover proxy-server interaction that is
     * controlled through a JscovTestUtil object and is of the JscovTestUtil
     * kind of browser..
     *
     * @param jscoverBrowserKind kind of browser to allocate driver for
     *
     * @return WebDriver object
     *
     * @exception RuntimeException browser kind is de-active
     */
    public WebDriver webDriverForJSCoverProxy(JscovBrowserKind jscoverBrowserKind) {
        WebDriver driverL;

        if (this.processingState == BROWSER_TYPE_ERROR) {
            return null;
        }
        if (!jscoverBrowserKind.isActiveBrowserKind()) {
            throw new RuntimeException(
                    String.format("%s is de-active and not allowed to be used.",
                            jscoverBrowserKind.toString()));
        }
        if (this.offCodeCoverage) {
            driverL = webDriverForBrowser(jscoverBrowserKind);
        } else {
            //Below will set browser proxy settings using DesiredCapabilities.
            Proxy proxy = new Proxy();

            proxy.setAutodetect(false);
            proxy.setProxyType(Proxy.ProxyType.MANUAL);
            proxy.setHttpProxy(jscoverLocalProxyAndPortString);

            // set the proxy (on the local browser session)
            switch (jscoverBrowserKind) {
                //
                // EDGE does not suppoort Selenium proxy (from
                // seaches done Jan-2019)
                //
                // case EDGE:
                //    //Use Capabilities when launch browser driver Instance.
                //    EdgeOptions edgeOptions = new EdgeOptions();
                //    edgeOptions.setProxy(proxy);
                //
                //    driverL = new EdgeDriver(edgeOptions);
                //    break;

                case FIREFOX:
                    //Use Capabilities when launch browser driver Instance.
                    FirefoxOptions ffOptions = new FirefoxOptions();
                    ffOptions.setProxy(proxy);

                    driverL = new FirefoxDriver(ffOptions);
                    break;

                // case OPERA:
                //     // OPERA is considered unstable for Selenium as of Jan-2019
                //     //
                //     // NOT TESTED
                //     // OPERA is using chromium engine, so assume chrome type
                //     //       proxy settings
                //     //
                //     OperaOptions operaOptions = new OperaOptions();
                //     operaOptions.setProxy(proxy);
                // 
                //     driverL = new OperaDriver(operaOptions);
                //     break;
                case CHROME:
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.setProxy(proxy);

                    driverL = new ChromeDriver(chromeOptions);
                    break;

                default:
                    // this a critical error, deny all other processing
                    this.processingState = BROWSER_TYPE_ERROR;
                    throw new RuntimeException(
                            String.format("The JscovTestUtil browser kind '%s' is not supported/coded.\n"
                                    + "Or the browser kind does not support local-profile proxy-server.",
                                    jscoverBrowserKind.toString()));
            }
        }
        // size the window to a more manage size
        Dimension winDim = new Dimension(750, 400);
        driverL.manage().window().setSize(winDim);

        jscoverDriverWithProxySet = driverL;
        return driverL;
    }

    /**
     * Perform the actual start of the proxy-server.
     * <p>
     * May throw exception which means the proxy-server is not running<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;proxySvrProcess.start();
     * <br>
     * but process may be running but had an input error, there are complex ways
     * to manage this, but JscovTestUtil will fail during the testing will
     * better explanation/exception.
     *
     * @exception RuntimeException 1) location issues for the 'jscover'
     * directory, or 2) 'target/dist/JSCover-all.jar' is not found as expected.
     * 3) Failed to start the proxy-server.
     */
    private void actualStart() {
        // will always provide a default report directory as a base point
        // otherwise the jscover-directory-structure will be used and
        // cause issues
        String reportDirBase = getreportDirBase();

        // re-build the commands each time some as to treat as a separate
        // item/process each time its used
        ArrayList<String> commandAndParms = new ArrayList<>();
        commandAndParms.add("java");
        commandAndParms.add("-jar");
        commandAndParms.add(pathToJSCoverAllJar);
        commandAndParms.add("-ws");
        commandAndParms.add("--local-storage");
        commandAndParms.add("--proxy");
        commandAndParms.add(String.format("--port=%d", this.jscoverLocalPortNo));
        commandAndParms.add(String.format("--report-dir=%s", reportDirBase));

        this.additionalOptionsArr.forEach((addOpt) -> {
            commandAndParms.add(addOpt);
        });
        // start the proxy-server as an external process
        ProcessBuilder proxySvrProcess = new ProcessBuilder(commandAndParms);
        proxySvrProcess.directory(new File(locationOfJscoverDir));

        try {
            // may throw exception which means the proxy-server is not running
            proxySvrProcess.start();

            // but process may be running but had an input error, there are
            // complex ways to manage this, but JscovTestUtil will fail
            // during the testing wil better explanation/exception
        } catch (IOException ex) {
            throw new RuntimeException(
                    String.format("Failed to start proxy-server process.\n"
                            + "Root-Cause is: %s\n"
                            + "Root-Message is: %s\n",
                            ex.getCause(),
                            ex.getMessage()));
        }
        this.processingState = STARTED;
    }

    /**
     * Stop the proxy-server running, but first get the code-coverage report.
     * Wait for a time-out period to get the report.
     * <p>
     * Processing to get reports is done in the browser/WebDriver and is
     * external to this class. The user needs to determine a good time-out value
     * for their environment.
     * <p>
     * Attempting to get a report from a 'stop' or 'report' action without prior
     * use of 'webDriverForJSCoverProxy()'/'overrideWebDriveForJSCoverProxy'
     * will not cause a report to generate and an ERR INFO message provided.
     *
     * @param timerForJscoverReport2Complete time (seconds) allowed to process
     * the captured code-coverage data into a report (Note: small Javascripts
     * and/or small number of files low time-out value. Lots-of or large files
     * more time needed.)
     *
     * @exception TimeoutException if there is a time-out, consider lengthening
     * the time-out for report generation execution
     */
    private void actualStop(int timerForJscoverReport2Complete) {
        TimeoutException teTrace = null;
        try {
            storeJscoverReportToDirInternal(timerForJscoverReport2Complete);

        } catch (TimeoutException teInner) {
            teTrace = teInner;
        }
        // no matter we wish to clean-up/stop the proxy-server
        //
        stopAJscoverServer(jscoverLocalProxyAndPortString, false);
        this.processingState = STARTED_AND_STOPPED;

        // if we timeed-out waiting for the report to complete, need to
        // throw an Exception to show as such within the testing environment
        // so the caller can change the timer value
        if (teTrace != null) {
            throw teTrace;
        }
        // report stored successfully
        pause(2000);
    }

    /**
     * Check retain N makes sense, negative does not.
     *
     * @param retainN the number to retain
     *
     * @Exception RuntimeException if negative
     */
    private void checkRetainNMakesSense(int retainN) {
        if (retainN < 0) {
            throw new RuntimeException("The retain N report value is negative (?).");
        }
    }

    /**
     * Clean up the timestamp sub-directories that exist in the "reports"
     * directory so as to manage storage.
     */
    private void cleanTimestampDirs() {
        // 0 means no clean up
        if (this.retainNTimestampReports <= 0) {
            return;
        }
        File reportsDirFile = new File(this.locationOfJscoverDir, getreportDirBase());

        if (reportsDirFile.isDirectory()) {
            // need to delete some directories, but need to ID the
            // latest one to retain
            List<String> arrListDirs = getTimestampDirList();
            if (arrListDirs.size() < retainNTimestampReports) {
                return;
            }
            int retainNMinus1 = arrListDirs.size() - retainNTimestampReports;
            //
            for (int i = 0; i < retainNMinus1; i++) {
                File dirToDelete = new File(reportsDirFile, arrListDirs.get(i));

                if (dirToDelete.isDirectory()) {
                    deleteDir(dirToDelete);
                }
            }
        }
    }

    private List<String> getTimestampDirList() {
        File reportsDirFile = new File(this.locationOfJscoverDir, getreportDirBase());

        // list all 'jscovYYYY....' directories in the reports directory
        String[] listOfRepDir = reportsDirFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                //............1111111111222222
                //..01234567890123456789012345 index
                //
                //..12345
                //.......1234567890123456789    time-stamp length + REP_TIMESTAMP_PREFIX.length
                // 'jscovYYYYMMdd_mmhhss_mmm
                //...............109876543210   minus index
                //
                // below code is optimized for speed
                if (name.length() == REP_TIMERSTAMP_LENGTH) {
                    if (name.startsWith(REP_TIMESTAMP_PREFIX)) {
                        // just confirm the underscores
                        if (name.charAt(name.length() - 4) == '_'
                                && name.charAt(name.length() - 11) == '_') {
                            if (new File(dir, name).isDirectory()) {
                                return true;

                            }
                        }
                    }
                }
                return false;
            }
        }
        );
        List<String> arrListDirs;
        if (listOfRepDir
                == null) {
            arrListDirs = new ArrayList<>();
        } else {
            arrListDirs = Arrays.asList(listOfRepDir);
            Collections.sort(arrListDirs);
        }
        return arrListDirs;
    }

    /**
     * Get the report directory base, where the 'reports' is defined as the
     * lowest base and appended with any user report-directory settings
     * (explicitly set or using time-stamp);
     *
     * @return string of the report directory as it pertains to the location of
     * JSCover's installation/location.
     */
    private String getreportDirBase() {
        String reportDirBase = "reports";

        if (this.reportDir != null) {
            if (!this.reportDir.trim().isEmpty()) {
                reportDirBase = String.format("%s/%s", reportDirBase, this.reportDir);
            }
        }
        return reportDirBase;
    }

    // active or not-active control of the JscovTestUtil
    /**
     * Set the active or not-active processing state, with filtering to accept
     * or deny new state depending on the current running processing state.
     *
     * @param state the new state to set
     */
    private void setNotActiveStateInternal(boolean state) {

        if (this.processingState == STARTED) {
            throw new RuntimeException("The JscovTestUtil object has a started proxy-server running\n"
                    + "setting active/not-active is denied.\n"
                    + "Change the invoking code to ensure the appropritate protocol is followed.");
        }
        if (this.processingState == BROWSER_TYPE_ERROR) {
            return;
        }
        this.offCodeCoverage = state;
    }

    /**
     * Send the stop message to the proxy-server so it closes down internally;
     *
     * @param jscoverLocalServer
     * @param ignoreConnectException
     */
    private void stopSendToProxyServer(String jscoverLocalServer, boolean ignoreConnectException) {
        String errMsg = "";
        String stopJSCoverServerHttpGet
                = String.format("http://%s/stop", jscoverLocalServer);

        try {
            URL myURL = new URL(stopJSCoverServerHttpGet);
            URLConnection myURLConnection = myURL.openConnection();
            myURLConnection.connect();

            // by doing this, it will actually "launch" the page and thus stop the
            // process
            Object ooo = myURLConnection.getContent();

        } catch (MalformedURLException me) {
            // new URL() failed, should not happen but is a coding error
            // on the part of the callee
            errMsg = me.getMessage();

        } catch (ConnectException ce) {
            if (!ignoreConnectException) {
                errMsg = ce.getMessage();
            }

        } catch (IOException e) {
            // openConnection() failed
            errMsg = e.getMessage();
        }
        // only if error is detected need to say so
        if (!errMsg.isEmpty()) {
            // nothing we can do about this
            System.out.println(String.format(
                    "INFO: Stop JSCover-server '%s' fail in JscovTestUtil\n"
                    + "INFO: '%s' : fail not critical\n"
                    + "INFO: potential portNo collision between test code JscovTestUtil server vs system.",
                    stopJSCoverServerHttpGet, errMsg));
        }
    }

    /**
     * Stop the JSCover server whether its the proxy-server or simple-server as
     * provided by the jscoverLocalServer URL name and port
     *
     * @param jscoverLocalServer String of the JSCover serverJscovTestUtil
     * driver does not support RemoteWebDriver
     *
     * @param ignoreConnectException ignore ConnectException as it is possible
     * that the connection is already closed to the server
     */
    @SuppressWarnings("CallToThreadYield")
    private void stopAJscoverServer(String jscoverLocalServer, boolean ignoreConnectException) {
        this.startProxyServerInvoked = false;

        stopSendToProxyServer(jscoverLocalServer, ignoreConnectException);

        // need to yeild the JVM doing this work so other JVM programs can
        // do some work (the actual stop on the proxy-server is a Java
        // program)
        Thread.yield();
    }

    /**
     * Store the report into the JSCover-structure-directory as provided in the
     * report-directory provided via the constructor methods.
     * <p>
     * Attempting to get a report from a 'stop' or 'report' action without prior
     * use of 'webDriverForJSCoverProxy()'/'overrideWebDriveForJSCoverProxy'
     * will not cause a report to generate and an ERR INFO message provided.
     *
     * @param timerForJscoverReport2Complete time (seconds) allowed to process
     * the code-coverage captured data into a report (small Javascript low
     * time-out lots-of/large files more time needed [also, CPU speed or file
     * access arrangement considerations].
     *
     * @exception TimeoutException if there is a time-out, consider lengthening
     * the time-out for the web-page report generation
     *
     * @exception JavascriptException cannot find jscoverage_report in the
     * web-page being processed (likely web-page not instrumented or page has
     * closed before calling this method)
     */
    @SuppressWarnings("CallToThreadYield")
    private void storeJscoverReportToDirInternal(int timerForJscoverReport2Complete) {
        if (this.processingState != STARTED) {
            // JSCover is not active so do no action. This allows invokes
            // of JscovTestUtil methods to remain within test-cases, but
            // not actively be running.
            return;
        }
        if (this.jscoverDriverWithProxySet == null) {
            // there is no driver to allocated for this object
            System.err.printf(
                    String.format("\nINFO: JscovTestUtil: Attempting to get a report due to a 'stop' or 'report' action.\n"
                            + "      HOWEVER, no WebDriver has been allocate or provided.\n")
            );
            return;
        }
        // the driver may have been closed prior to asking for a report, as
        // this would be impossible (no session so nothing to communicate with)
        // we'll ignore any action
        try {
            if (((RemoteWebDriver) jscoverDriverWithProxySet).getSessionId() == null) {
                return;
            }
        } catch (ClassCastException cce) {
            System.err.println("CAUTION: JscovTestUtil overridden driver does not support RemoteWebDriver");
        }

        // prepare to get the JSCover results of code coverage
        String subDirectoryForReport = "";

        if (!this.reportFurtherSubDirOrTimeStamp.isEmpty()) {
            subDirectoryForReport = this.reportFurtherSubDirOrTimeStamp;
        }
        // cause the jscover-report to be created.

        // once complete place a element in the DOM (id='stopEleId') to indicate
        // the report creation has finished
        //
        // try {
        String generateReportScript = String.format(REPORT_COMPLETE_AND_CALLBACK_SCRIPT,
                subDirectoryForReport);

        ((JavascriptExecutor) jscoverDriverWithProxySet).executeScript(generateReportScript);

        // need to provide time for the JSCover (Java to run, otherwise 
        // this thread will tend to lock up the JVM)
        Thread.yield();
        //
        // if there is no session the javascript executor will exception, however
        // for any other issues we wish to ensure reports are provided. So the SessionId
        // is checked at the begining of this method
        //
        // } catch (NoSuchSessionException ee) {
        //     return;
        // }
        // wait on the DOM being updated with the 'stopEleId' element for
        // the specified time
        //
        (new WebDriverWait(jscoverDriverWithProxySet, timerForJscoverReport2Complete))
                .until(ExpectedConditions
                        .presenceOfElementLocated(By.id(STOPELEID_STR)));
    }

    /**
     * Restart the proxy-server which causes the proxy-server to
     * <b>stopProxyServer</b> and then <b>startProxyServer</b> again&#46;
     * <br><br>
     * This method allows the user to change JS-file filters b=prior to
     * continuing tests&#46; Any changes to the "instrumentation" filters should
     * be applied before calling this method&#46;
     * <br><br>
     * CAUTION: Has the potential to fail to provide full reports (some JS-files
     * missing in report) if the system is slow due to under heavy load. The
     * heavy load may cause the actual <b>stop</b> of JSCover proxy-server to be
     * slow to shutdown (over 2 seconds), and/or the re-<b>start</b> of JSCover
     * proxy-server to be ready to accept connections (over another 2 seconds).
     *
     * <p>
     * <B>Why this method</b></p>
     * <p class='whyidn'>
     * Why? change the JS-file filters such that proxy will instrument when a
     * newer GET or a link to another page occurs.</p>
     * <pre>
     * &#64;Test(dependsOnMethods = {"selectConfigButt"})
     * public void changeToDeviceConfig() {
     *   // will be changing the page but will change to newer
     *   // exclude JS-file from code-coverage
     *   jscovObject.optionClearAnyAdded()
     *              .optionNoInstrument("aes.js")
     *              .optionNoInstrument("subdir/wmospwd.js")
     *              .restartProxyServer(30);
     *   // prepare to goto different page
     *   //
     *   WebElement devAccPwdInputEle = driver.findElement(By.id("pwdcfg"));
     *   WebElement devAccCfgButtEle = driver.findElement(By.id("pwdbutt"));
     *
     *   // change to a different page
     *   devAccPwdInputEle.sendKeys("somepassword");
     *   devAccCfgButtEle.click();
     *   pause(2000); // allow new page to load
     *
     *   jscovObject.storeJscoverReport(30);
     * }
     *
     *
     * </pre>
     *
     * @param timerForJscoverReport2Complete time (seconds) allowed to process
     * the code-coverage captured data into a report (small Javascript low
     * time-out lots-of/large files more time needed [also, CPU speed or file
     * access arrangement considerations].
     *
     * @exception TimeoutException if there is a time-out, consider lengthening
     * the time-out for the web-page report generation
     *
     * @exception JavascriptException cannot find jscoverage_report in the
     * web-page being processed (likely web-page not instrumented or page has
     * closed before calling this method)
     */
    @SuppressWarnings("CallToThreadYield")
    public void restartProxyServer(int timerForJscoverReport2Complete) {
        this.stopProxyServer(timerForJscoverReport2Complete);
        this.startProxyServer();
    }

    /**
     * Add JSCover options to the proxy-server process executable ("--port" and
     * "--report-dir" are denied as they are set by the constructor)&#46; All
     * other options are accepted but not checked (implying options that are not
     * JSCover will cause the proxy-server not to launch, with unknown
     * consequences)&#46;
     * <br><br>
     * <span class='warnopts'><span>WARNING:</span> Too many options will cause
     * command-line buffer overrun and the <code>startProxyServer</code> may
     * fail.</span>
     * <p>
     * Duplicate options will be ignored.
     * <p>
     * Removal of single options is not supported instead use
     * <code>clearAdditionalJSCoverOptions</code> and re-add all other options.
     *
     * @param listOfOptionStrings list of the JSCover options as string
     *
     * @return this object
     */
    public JscovTestUtil optionAddGeneric(String... listOfOptionStrings) {
        for (String optionStr : listOfOptionStrings) {
            for (String denyOption : new String[]{"--port", "--report-dir"}) {
                if (optionStr.startsWith(denyOption)) {
                    throw new RuntimeException(
                            String.format("Attempting to change '%s': denied. ", denyOption));
                }
            }
            // accept options, but no checking other than duplicate
            if (!this.additionalOptionsArr.contains(optionStr)) {
                this.additionalOptionsArr.add(optionStr);
            }
        }
        return this;
    }

    /**
     * Clear any additional JSCover options that were added.
     *
     * @return this object
     */
    public JscovTestUtil optionClearAnyAdded() {
        this.additionalOptionsArr.clear();
        return this;
    }

    /**
     * Add a JS-file to the "--no-instrument" option, implying all other files
     * will be instrumented&#46; The parameters are a URL as it pertains to the
     * web-page being tested (see full Javadoc&#46;)
     * <br>
     * <span class='warnopts'><span>WARNING:</span> Too many options will cause
     * command-line buffer overrun and the <code>startProxyServer</code> may
     * fail.</span>
     * <p>
     * Using this option allows a user to exclude certain JS-files from being
     * instrumented by the JSCover proxy-server. Some of the reasons are:
     * <ul>
     * <li>A JS-file has been minified and as such many statements will appear
     * on a single line of the JS-file. JSCover instruments a statement per line
     * of the JS-file (so likely to fail and not make sense).</li>
     * <li>Wish to exclude a JS-file from the code-coverage collection.</li>
     * </ul>
     * <p>
     * Of note, it is assumed that there are few JS-files involved in the
     * web-page (less than 20).
     * <p  style="margin-bottom: 0px;">
     * The URL-JS-File parameter is an exact match to the files to be loaded. In
     * simple terms: Javascript files are HTTP GET requested due to the
     * &lt;script src... &gt; tag:</p>
     * <div class='indtpp'>
     * <pre>
     * &lt;script type="text/javascript" src="./subdir/file1.js"&gt;&lt;/script&gt;
     * &lt;script type="text/javascript" src="./file2.js"&gt;&lt;/script&gt;
     * &lt;script type="text/javascript" src="/file3.js"&gt;&lt;/script&gt;</pre>
     * </div>
     * <p style="margin-bottom: 0px;">
     * UrlJsFile would contain the URL of the file (as it pertains to the above
     * paths):</p>
     * <div class='indtpp'>
     * <pre>
     * /subdir/file1.js   <i>or</i>    subdir/file1.js
     * /file2.js          <i>or</i>    file2.js
     * /file3.js          <i>or</i>    file3.js</pre>
     * </div>
     *
     * @param urlJsFiles list of URLs for each JS-file (too many of these could
     * use up the OSs command-line buffer size)
     *
     * @return this object
     *
     * @exception RuntimeException provided a null or empty string
     */
    public JscovTestUtil optionNoInstrument(String... urlJsFiles) {
        return optionInstrument(false, "--no-instrument", "urlJsFiles", urlJsFiles);
    }

    /**
     * Add a '--no-instrument-reg=URL' option in its regex format for the
     * proxy-server&#46; The regex is a JVM regular expression which can be
     * involved to achieve desired effect&#46; See <a href='#regexexample'>
     * regex patterns examples</a> for <code>optionNoInstrumentReg</code> and
     * <code>optionOnlyInstrumentReg</code>&#46;
     * <br><br>
     * <span class='warnopts'><span>WARNING:</span> Too many options will cause
     * command-line buffer overrun and the <code>startProxyServer</code> may
     * fail.</span>
     * <p>
     * Using this option allows a user to exclude certain JS-files from being
     * instrumented by the JSCover proxy-server. Some of the reasons are:
     * <ul>
     * <li>A JS-file has been minified and as such many statements will appear
     * on a single line of the JS-file. JSCover instruments a statement per line
     * of the JS-file (so likely to fail and not make sense).</li>
     * <li>Wish to exclude a JS-file from the code-coverage collection.</li>
     * </ul>
     * <p>
     * Of note, it is assumed that there are few JS-files involved in the
     * web-page (less than 20).
     *
     *
     * <h3 id='regexexample'>regex patterns examples</h3>
     * <p style="margin-bottom: 0px;">
     * Javascript files are HTTP GET requested due to the &lt;script src... &gt;
     * tag:</p>
     * <div class='indtpp'>
     * <pre>
     * &lt;script type="text/javascript" src="./subdir/file1.js"&gt;&lt;/script&gt;
     * &lt;script type="text/javascript" src="./subdir/fileA1.js"&gt;&lt;/script&gt;
     * &lt;script type="text/javascript" src="./file2.js"&gt;&lt;/script&gt;
     * &lt;script type="text/javascript" src="/file3.js"&gt;&lt;/script&gt;</pre>
     * </div>
     * <p style="margin-bottom: 0px;">
     * regexOfJsFile regex-pattern that match URL of the file (as it pertains to
     * the above paths). Regex can be involved and deal with complex abilities
     * to match patterns. For JscovTestUtil very simple patterns are
     * recommended, for more involved patterns consult external Java regex
     * expertise.
     * </p>
     * <p>
     * Following are simple positive matches (note: '\' needs to be coded as
     * '\\' in Java)
     * </p>
     * <div class='indtpp'>
     * <table class='regextab'>
     * <caption style='display: none;'>regex to string</caption>
     * <tr>
     * <th><i>regex-pattern</i></th>
     * <th>./subdir/file1.js</th>
     * <th>./subdir/fileA1.js</th>
     * <th>./file2.js</th>
     * <th>/file3.js</th>
     * <th><i>simple wildcard</i></th>
     * <th><i>coded as</i></th>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*dir.file.*$</td>
     * <td>X</td> <td>X</td> <td>-</td> <td>-</td> <td>*dir*file*</td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file.*$</td>
     * <td>X</td> <td>X</td> <td>X</td> <td>X</td> <td>*file*</td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file.*1.*$</td>
     * <td>X</td> <td>X</td> <td>-</td> <td>-</td> <td>*file*1*</td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file3.*$</td>
     * <td>-</td> <td>-</td> <td>-</td> <td>X</td> <td>*file3*</td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file2.*$</td>
     * <td>-</td> <td>-</td> <td>X</td> <td>-</td> <td>*file2*</td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*2.*$</td>
     * <td>-</td> <td>-</td> <td>X</td> <td>-</td> <td>*2*</td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file3\.js$</td>
     * <td>-</td> <td>-</td> <td>-</td> <td>X</td> <td>*file3.js</td>
     * <td>^.*file3\\.js$</td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*subdir.*$</td>
     * <td>X</td> <td>X</td> <td>-</td> <td>-</td> <td>*subdir*</td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file[32].*$</td>
     * <td>-</td> <td>-</td> <td>X</td> <td>X</td> <td></td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file[321].*$</td>
     * <td>X</td> <td>-</td> <td>X</td> <td>X</td> <td></td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file.*[321].*$</td>
     * <td>X</td> <td>X</td> <td>X</td> <td>X</td> <td></td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file.*[13].*$</td>
     * <td>X</td> <td>X</td> <td>-</td> <td>X</td> <td></td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file.*[3].*$|^.*file.*[1].*$</td>
     * <td>X</td> <td>X</td> <td>-</td> <td>X</td> <td>
     * <span class='smtxt'>regex or condition</span></td> <td></td>
     * </tr>
     *
     * <tr>
     * <td><p>
     * ^.*file.*[A].*$|^.*file.*[3].*$
     * <p>
     * ^.*file.*[A].*$|^.*file[3].*$
     * <p>
     * ^.*file.*[A].*$|^.*file3.*$</td>
     * <td>-</td> <td>X</td> <td>-</td> <td>X</td> <td>
     * <span class='smtxt'>regex or condition</span></td> <td></td>
     * </tr>
     *
     * </table>
     * </div>
     *
     * @param regexOfJsFile the regex-pattern to match with JSCover when doing
     * instrumentation (too many of these could use up the OSs command-line
     * buffer size)
     *
     * @return this object
     *
     * @exception RuntimeException provided a null or empty string
     * @throws PatternSyntaxException If the expression's syntax is invalid ( do
     * not ignore this)
     */
    public JscovTestUtil optionNoInstrumentReg(String... regexOfJsFile) {
        return optionInstrument(true, "--no-instrument-reg", "regexOfJsFile", regexOfJsFile);
    }

    /**
     * Add a '--only-instrument-reg=URL' option in its regex format for the
     * proxy-server&#46; The regex is a JVM regular expression which can be
     * involved to achieve desired effect&#46; See <a href='#regexexample'>regex
     * patterns examples</a> for <code>optionNoInstrumentReg</code> and
     * <code>optionOnlyInstrumentReg</code>&#46;
     * <br><br>
     * <span class='warnopts'><span>WARNING:</span> Too many options will cause
     * command-line buffer overrun and the <code>startProxyServer</code> may
     * fail.</span>
     * <p>
     * Using this option allows a user to restrict code-coverage to certain
     * JS-files to being instrumented by the JSCover proxy-server.
     * <p>
     * Of note, it is assumed that there are few JS-files involved in the
     * web-page (less than 20).
     *
     * @param regexOfJsFile the regex-pattern to match with JSCover when doing
     * instrumentation (too many of these could use up the OSs command-line
     * buffer size)
     *
     * @return this object
     *
     * @exception RuntimeException provided a null or empty string
     * @throws PatternSyntaxException If the expression's syntax is invalid ( do
     * not ignore this)
     */
    public JscovTestUtil optionOnlyInstrumentReg(String... regexOfJsFile) {
        return optionInstrument(true, "--only-instrument-reg", "regexOfJsFile", regexOfJsFile);
    }

    /**
     * Add the option for instrumentation include or exclude patterns.
     * <br><br>
     * <span class='warnopts'><span>WARNING:</span> Too many options will cause
     * command-line buffer overrun and the <code>startProxyServer</code> may
     * fail.</span>
     *
     * @param option string for instrument
     * @param paramName name of calling methods list of URLs
     * @param strToAppend string list of URLs or regex
     *
     * @return this object
     *
     * @exception RuntimeException provided a null or empty string
     * @throws PatternSyntaxException If the expression's syntax is invalid ( do
     * not ignore this)
     */
    private JscovTestUtil optionInstrument(boolean checkRegex, String option, String paramName, String... strToAppend) {
        for (String s : strToAppend) {
            if (s == null) {
                throw new RuntimeException(String.format("\n %s contains a 'null' string.", paramName));
            }
            if (s.trim().isEmpty()) {
                throw new RuntimeException(String.format("\n %s contains an empty-string.", paramName));
            }
            if (checkRegex) {
                Pattern.compile(s);
            }
            // add as a generic option will check and avoid duplicates
            optionAddGeneric(String.format("%s=%s", option, s));
        }
        return this;
    }

    // - - - - - - - - - - - - - - - - - - - - - 
    protected void pause(int msecs) {
        try {
            Thread.sleep(msecs);

        } catch (InterruptedException ex) {
            //            Logger.getLogger(ctrl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * The internal states for processing test utilities needs.
     */
    enum ProcessingState {
        /**
         * The user has selected a browser kind that does not support JSCover
         * via a proxy-server. As of Jan-2019 EDGE is one such, for some reason
         * proxy seems to have issues. Plus it cannot be set.
         *
         * Basically in a critical state.
         */
        BROWSER_TYPE_ERROR,
        //        /**
        //         * JscovTestUtil code coverage processing is not active, and is
        //         * explicitly controlled by a users calls of setOnCodeCoverage and
        //         * setOffCodeCoverage.
        //         * <p>
        //         * Ignore any start, store-report or stop requests.
        //         */
        //        OFF_CODE_COVERAGE,
        /**
         * JscovTestUtil code coverage processing has not been started yet.
         * <p>
         * Accept: start requests<br>
         * Ignore: store-report or stop requests
         */
        NOT_STARTED,
        /**
         * JscovTestUtil code coverage processing has been started.
         * <p>
         * Accept: start requests<br>
         * Ignore: store-report or stop requests
         */
        STARTED,
        /**
         * JscovTestUtil code coverage processing has been stopped after it was
         * started.
         * <p>
         * Accept: start requests<br>
         * Ignore: store-report or stop requests
         */
        STARTED_AND_STOPPED
    }
}

/*
License:
- Any use of JscovTestUtil & JscovBrowserKind in source or object 
  form must retain this copyright and license notice.
- Use of the "software" implies your agreement with this license as a "licensee".

Terms:
- "licensee" is a person or entity, non-commercial or commercial.
- "software" pertains to JscovTestUtil & JscovBrowserKind source-code, 
  object-form, and/or documentation file(s).

Warranty:
-  This "software" is provided "as is", with no warranty expressed or 
   implied, and no guarantee for accuracy or applicability to any purpose. 

Copies:
- Multiple copies of the "software" are permitted per licensee.

Usage:
- "software" may be used by a person or entity, non-commercial or commercial
  royalty free.
- "software" may be incorporated into commissioned other software as a 
  as a class-path dependency royalty free and free of charge for distribution.

Modification:
- "software" source code may be modified for personal use only, but must
  retain this license and original copyright notice.
- any modification to the "software" must be clearly marked as such by 
  by adding modifying-author's own copyright (next to the origin authors copyright).
  -- i.e. "Copyright (c) <year> <modifying-author>. Modifying author."
- Documentation may not be modified, the exception being Javadoc comments.

Distribution:
- The "software" is permitted to be re-distributed as an integrated component
  for a clients project/package.
- Copies may be provided to 'clients' "as is" for no fee.
- "software" may not be re-licensed or sub-licensed.

 */
