/*
 * Copyright (c) 2019 dbradley. Original author.
* 
*  Licensed under MIT
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

import java.io.File;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Licensed under MIT.
 * <p>
 * <a href="./doc-files/jtuhow.html">Supplemental documentation</a>.
 * <p>
 * The kind of browsers that JscovTestUtil supports running in a Java
 * environment.
 * <p style="margin-bottom: 5px;">
 * Each browser-kind has a:
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'system-property' key
 * &nbsp;&nbsp;&nbsp; and &nbsp;&nbsp;&nbsp;'driver-name-starts-with'
 * pattern</p>
 * <div style='padding-left: 50px;'>
 * <p style="margin-top: 0px;margin-bottom: 0px;">
 * The 'key' is defined by the WebDriver executable and is Selenium V3
 * design.</p>
 * <p style="margin-top: 1px;">
 * The 'pattern' is used to find a driver-file-name that startsWith the pattern
 * from within the driver directory.</p>
 * </div>
 * <p>
 * Each of these can be changed using the 'changeWebDriverProperty' method on
 * the browser-type
 * <br>&nbsp;&nbsp;&nbsp;e.g.
 * <code>CHROME.changeWebDriverProperty("chrome.key", "chromedriverdelta");</code>
 * <br><br>
 * Additionally, the location of the driver for each type can be explicitly set
 * using the 'changeWebDriverFilepath' method
 * <br>&nbsp;&nbsp;&nbsp;e.g.
 * <code>changeWebDriverFilepath("G:\test_driver_dir\chromedriverBeta.exe";</code>
 * <h3>Browser-kind limitations</h3>
 * <p style="margin-bottom:0px;">
 * The browser types supported by JscovTestUtil/JscovBrowserKind are a subset of
 * Selenium's BrowserType due to:</p>
 * <div style="padding-left: 30px;">
 * <p >
 * There are many OSs for browsers, but not all of the OSs support Java. (For
 * example, Android has its own Java implementation that is not compatible with
 * J2SE/J2EE Java, which this class is written for.)
 * <p>
 * Additionally, not all browsers are on all OSs, for example, EDGE is not on
 * Linux.
 * <p>
 * The drivers or browser lack support for proxy settings and/or more than
 * on session.
 * </div>
 */
public enum JscovBrowserKind {
    /**
     * The FIREFOX browser:
     * <br>
     * <br><i>Defaults:</i>&nbsp;&nbsp;&nbsp;System-property-key:
     * "webdriver.gecko.driver" &nbsp;&nbsp;&nbsp;Driver-name-starts-with
     * pattern: "geckodriver"
     */
    FIREFOX(true),
    // - - -
    //  /**
    //   * The EDGE browser:
    //   * <br><br><i>Defaults:</i>&nbsp;&nbsp;&nbsp;System-property-key:
    //   * "webdriver.edge.driver" &nbsp;&nbsp;&nbsp;Driver-name-starts-with
    //   * pattern: "MicrosoftWebdriver"
    //   */
    //
    //  EDGE(true), 
    // - - -
    // /**
    //  * The OPERA browser:
    //  * <br><br><i>Defaults:</i>&nbsp;&nbsp;&nbsp;System-property-key:
    //  * "webdriver.opera.driver" &nbsp;&nbsp;&nbsp;Driver-name-starts-with
    //  * pattern: "operadriver"
    //  * <br><br>
    //  * As of Jan-2019 OPERA selenium driver is unstable and not supporting V3
    //  * (?)&#46;
    //  * <br>-- the application is not part of the "program files" and needs
    //  * special setup for Linux, MacOS and Windows
    //  * <br>-- support of proxy-server is thus considered too unstable
    //  *
    //  */
    // OPERA(false),
    // - - -
    /**
     * The CHROME browser:
     * <br><br><i>Defaults:</i>&nbsp;&nbsp;&nbsp;System-property-key:
     * "webdriver.chrome.driver" &nbsp;&nbsp;&nbsp;Driver-name-starts-with
     * pattern: "chromedriver"
     */
    CHROME(true);

    /**
     * Marks the browser-kind as active or not.
     */
    private boolean isActiveBool;

    /**
     * The executable path for the browser-kind. OPERA requires this to work
     * other kinds do not.
     */
    private String binaryPathForExecutable = null;

    /**
     * Create the kind object to contain specific data about
     * system-property-key, driver-name and active-state. The latter controls
     * whether the kind may be used by JscovTestUtil (false if considered
     * unstable).
     *
     * @param activeState true or false (the latter being considered unstable)
     */
    JscovBrowserKind(boolean activeState) {
        this.isActiveBool = activeState;
    }

    /**
     * Set the executable path for browser-kind&#46; This is provided for OPERA
     * as its Selenium launcher needs to be to an absolute path.
     *
     * @param absolutePath string of path to executable
     *
     * @exception RuntimeException if the path is not a file on the system
     */
    public void setExecutePath(String absolutePath) {
        this.binaryPathForExecutable = absolutePath;
        File testFileExists = new File(absolutePath);
        if (!testFileExists.isFile()) {
            throw new RuntimeException("Path to executable is not a file/does not exist.");
        }
    }

    /**
     * Get the executable path in string format for the browser-kind.
     *
     * @return String for an absolute path
     *
     * @exception RuntimeException if the path has not been setExecutePath
     * previously
     */
    public String getExecutePath() {
        if (this.binaryPathForExecutable == null) {
            throw new RuntimeException(String.format("\nExecute path has not been provided for: %s.", this.toString()));
        }
        return this.binaryPathForExecutable;
    }

    /**
     * Is the browser-kind active as per JscovTestUtil considerations.
     *
     * @return true if active
     */
    public boolean isActiveBrowserKind() {
        return this.isActiveBool;
    }

    /**
     * Get the JscovTestUtil.JscovBrowserKind kind for the WebDriver browser
     * type provided.
     *
     * @param driver WebDriver to get the JscovBrowserKind for
     *
     * @return the JscovTestUtil type or null if not active or supported
     */
    @SuppressWarnings("ConvertToStringSwitch")
    public static JscovBrowserKind getTypeForWebDriver(WebDriver driver) {
        Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
        String browserName = caps.getBrowserName().toUpperCase();

        JscovBrowserKind broserMatch = null;

        // convert to 'if' string so as to support Java 1.6
        if (browserName.equals("FIREFOX")) {
            broserMatch = FIREFOX;

        } else if (browserName.equals("CHROME")) {
            broserMatch = CHROME;

        }
        // else if (browserName.equals("MICROSOFTEDGE")) {
        //     broserMatch = EDGE;
        //
        // } else if (browserName.equals("OPERA")) {
        //      // OPERA is considered unstable for Selenium as of Jan-2019
        //    broserMatch = OPERA;
        // }
        if (broserMatch != null) {
            if (broserMatch.isActiveBrowserKind()) {
                return broserMatch;
            }
        }
        return null;
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
