/* Copyright (c) 2019 dbradley.
* 
*  License under MIT.
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
package org.jtestdb.selenium.tstmod;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Class to set a web-page under test via a test-mode-element being added to
 * some 'normal' element within the page. The presence of a test-mode-element on
 * a page may be used to influence how Javascript or Selenium executes. The
 * design of the executables need to incorporate actions for testing (hence,
 * test-mode-element).
 * <p>
 * The test-mode-element(s) [is a &lt;p&gt; tagged element] will be added with a
 * style-'display:none;' so as to minimize impact on formatting the page and any
 * testing that is coordinate sensitive. The best and appropriate way to avoid
 * coordinate sensitive issues is to add the test-mode-element to the HTML
 * 'body'; the test-mode-element is added/appended to end of the 'body' (or an
 * explicitly provided normal element).
 * <p>
 * For this to work, supporting code may be necessary in Javascript or Selenium
 * to perform stuff differently, if required: implying a web-page needs to be
 * designed for testing. A little extra Javascript to address the testing needs
 * has to consider any performance impacts it could cause.
 *
 * @author dbradley
 * 
 * Licensed under MIT.
 */
public class SetTestMode {

    /**
     * String for the test-mode-element's ID attribute.
     */
    final private String id4TestModeEle;

    /**
     * Information that the test-mode-element will have in its innerHTML to pass
     * to the testing executable.
     * <p>
     * This information may not contain a ':'.
     * <p>
     * WHY? the innerHTML text of the test.mode.element will be formatted as
     * 'info:Test mode element'. Thus splitting the innerHTML with ':' and using
     * the 0th element will provide testing information.
     */
    final private String info;

    /**
     * The Web driver object that the page is loaded from.
     */
    final private WebDriver driver;

    /**
     * A variable used for construction of Javascript that will be used via a
     * JavascriptExecutor object. The script is code to apply/remove the
     * test-mode-element into/out-of the web-page.
     */
    private String execStr = "";

    /**
     * Create a set test-mode processor object, with information/data included
     * in the element's innerHTML text.
     *
     * @param id4TestModeEle string of the test-mode-element's ID to be
     * introduced
     *
     * @param info string that will be information that a testing executable can
     * use to process
     * <p>
     * This information may not contain a ':'.
     * <p>
     * WHY? the innerHTML text of the test.mode.element will be formatted as
     * "info:Test mode element". Thus splitting the innerHTML with ':' and using
     * the 0th element will provide testing information.
     *
     * @param driver the web-page's Selenium driver
     */
    public SetTestMode(String id4TestModeEle, String info, WebDriver driver) {
        this.id4TestModeEle = id4TestModeEle;
        this.info = info;
        this.driver = driver;
    }

    /**
     * Create a set test-mode processor object, as a flag/semaphore.
     *
     * @param id4TestModeEle string of the test-mode-element's ID to be
     * introduced
     * @param driver the web-page's Selenium driver
     */
    public SetTestMode(String id4TestModeEle, WebDriver driver) {
        this.id4TestModeEle = id4TestModeEle;
        this.info = null;
        this.driver = driver;
    }

    /**
     * Apply a test-mode-element to an DOM element as defined by its tag-name
     * and its index within all the elements of the same tag-name.
     *
     * @param tagName the element tag-name to look for a list of elements
     * @param tagN the N element in the list to have the test-mode-element added
     * to
     */
    public void applyOnTag(String tagName, int tagN) {
        // there maybe multiple tag items, thus there is a need
        // for an eplicit tagN index
        //
        // the best tags are 'body' or 'html', however, under HTML 5
        // these tags are optional
        List<WebElement> webEleArr = this.driver.findElements(By.tagName(tagName));

        if (webEleArr.isEmpty()) {
            // this is not allowed so bomb
            throw new RuntimeException(
                    String.format("applyOnTag: no tag '%s' found.", tagName));
        }
        int arrSize = webEleArr.size();
        if (tagN < 0 || tagN >= arrSize) {
            // this is not allowed so bomb
            throw new RuntimeException(
                    String.format("applyOnTag: tagN out of range 0-to-%d '%d' found.",
                            arrSize - 1, tagN));
        }
        // construct on-element variable Javascript script for use by JavascriptExecutor
        this.execStr = "var onEle = document.getElementsByTagName('"
                + tagName
                + "')["
                + tagN
                + "];";

        applyTestModeEleInternal();
    }

    /**
     * Apply a test-mode-element to the DOM WebElement.
     *
     * @param onElement WebElement to apply child-element test-mode-element to
     */
    public void applyOnWebelement(WebElement onElement) {
        String idOfOnElement = onElement.getAttribute("id");
        if (idOfOnElement == null) {
            // this is not possible to so bomb
            throw new RuntimeException("applyOnWebelement: WebElement needs 'id' attribute.");
        }
        applyOnIdElement(idOfOnElement);
    }

    /**
     * Apply a test-mode-element to the DOM element with on-element ID.
     *
     * @param idOnElement string of element-id to apply a child
     * test-mode-element to
     */
    public void applyOnIdElement(String idOnElement) {
        WebElement onElement = driver.findElement(By.id(idOnElement));
        if (onElement == null) {
            // this is not possible to so bomb
            throw new RuntimeException(
                    String.format("applyOnIdElement: '%s' element does not exist.",
                            idOnElement));
        }
        // construct on-element variable Javascript script for use by JavascriptExecutor
        execStr = "var onEle = document.getElementById('"
                + idOnElement
                + "');";

        applyTestModeEleInternal();
    }

    /**
     * Apply the test-mode-element onto the current page via a javascript that
     * has been pre-prepared in the execStr variable as the 'onEle' JS variable.
     * <p>
     * This is for internal use only.
     */
    private void applyTestModeEleInternal() {

        // script for adding/appending a test-mode-element to an on-element
        // varibale previouly prepared.
        this.execStr += ""
                // test-mode-element is a p-tag element
                + "var testModeEle = document.createElement('p');"
                // id attribute of the test-mode-element, with display style
                // of none
                + "testModeEle.setAttribute('id', '"
                + this.id4TestModeEle
                + "');"
                + "testModeEle.setAttribute('style', 'display: none;');"
                + "testModeEle.appendChild(document.createTextNode('";

        // add any information to the test-mode-element, along with standard label
        if (info != null) {
            execStr += info;
        }
        execStr += ":Under test mode'));"
                + "onEle.appendChild(testModeEle);"
                + "";
        // Selenium execute the script to add/append the test-mode-element
        doJS();
    }

    /**
     * Remove the test mode element from current page, if it exists. If it does
     * not exist then provide an error message.
     */
    public void removeTestModeEle() {
        WebElement onElement = driver.findElement(By.id(id4TestModeEle));
        if (onElement == null) {
            // provide a message but do not bomb as this method is
            // attempting to remove the said element
            System.err.printf("removeTestModeEle: '%s' element does not exist.",
                    id4TestModeEle);
            return;
        }
        // construct the remove element javascript
        this.execStr = ""
                + "var tstModEle = document.getElementById('"
                + id4TestModeEle
                + "');"
                + "tstModEle.remove();";
        // Selenium execute the script to add/append the test-mode-element
        doJS();
    }

    /**
     * Selenium execute the Javascript stored in execStr .
     */
    private void doJS() {
        JavascriptExecutor js = (JavascriptExecutor) this.driver;
        js.executeScript(execStr);
        execStr = "";
        pauseMy(500);
    }

    /**
     * Pause capability with TRY's hidden from other code.
     *
     * @param msecs time to pause in milliseconds
     */
    private static void pauseMy(int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException ex) {
            // Logger.getLogger(ctrl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
