/**
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2010.                            (c) 2010.
 *  Government of Canada                 Gouvernement du Canada
 *  National Research Council            Conseil national de recherches
 *  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 *  All rights reserved                  Tous droits réservés
 *
 *  NRC disclaims any warranties,        Le CNRC dénie toute garantie
 *  expressed, implied, or               énoncée, implicite ou légale,
 *  statutory, of any kind with          de quelque nature que ce
 *  respect to the software,             soit, concernant le logiciel,
 *  including without limitation         y compris sans restriction
 *  any warranty of merchantability      toute garantie de valeur
 *  or fitness for a particular          marchande ou de pertinence
 *  purpose. NRC shall not be            pour un usage particulier.
 *  liable in any event for any          Le CNRC ne pourra en aucun cas
 *  damages, whether direct or           être tenu responsable de tout
 *  indirect, special or general,        dommage, direct ou indirect,
 *  consequential or incidental,         particulier ou général,
 *  arising from the use of the          accessoire ou fortuit, résultant
 *  software.  Neither the name          de l'utilisation du logiciel. Ni
 *  of the National Research             le nom du Conseil National de
 *  Council of Canada nor the            Recherches du Canada ni les noms
 *  names of its contributors may        de ses  participants ne peuvent
 *  be used to endorse or promote        être utilisés pour approuver ou
 *  products derived from this           promouvoir les produits dérivés
 *  software without specific prior      de ce logiciel sans autorisation
 *  written permission.                  préalable et particulière
 *                                       par écrit.
 *
 *  This file is part of the             Ce fichier fait partie du projet
 *  OpenCADC project.                    OpenCADC.
 *
 *  OpenCADC is free software:           OpenCADC est un logiciel libre ;
 *  you can redistribute it and/or       vous pouvez le redistribuer ou le
 *  modify it under the terms of         modifier suivant les termes de
 *  the GNU Affero General Public        la “GNU Affero General Public
 *  License as published by the          License” telle que publiée
 *  Free Software Foundation,            par la Free Software Foundation
 *  either version 3 of the              : soit la version 3 de cette
 *  License, or (at your option)         licence, soit (à votre gré)
 *  any later version.                   toute version ultérieure.
 *
 *  OpenCADC is distributed in the       OpenCADC est distribué
 *  hope that it will be useful,         dans l’espoir qu’il vous
 *  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
 *  without even the implied             GARANTIE : sans même la garantie
 *  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
 *  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
 *  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
 *  General Public License for           Générale Publique GNU Affero
 *  more details.                        pour plus de détails.
 *
 *  You should have received             Vous devriez avoir reçu une
 *  a copy of the GNU Affero             copie de la Licence Générale
 *  General Public License along         Publique GNU Affero avec
 *  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
 *  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
 *                                       <http://www.gnu.org/licenses/>.
 *
 ************************************************************************
 */
package ca.nrc.cadc.groups.web.integration;


import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import junit.framework.AssertionFailedError;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import ca.nrc.cadc.util.Log4jInit;


/**
 * Subclasses of this should have the necessary tools to create an automated
 * web application test.
 */
public abstract class AbstractWebApplicationIntegrationTestOld
{
    private final static Logger LOGGER =
            Logger.getLogger(AbstractWebApplicationIntegrationTestOld.class);

    // One minute is just too long.
    protected static final int TIMEOUT_IN_MILLISECONDS = 4 * 60 * 1000;

    private String seleniumServerHost;
    private int seleniumServerPort;
    private String internetBrowserCommand;
    private String webURL;
    private String username;
    private String password;
    private int currentWaitTime;
    private boolean failOnTimeout;

    protected WebDriver driver;

    public AbstractWebApplicationIntegrationTestOld()
    {
        Log4jInit.setLevel("ca.nrc.cadc", Level.INFO);

        // Base Host of the web application to be tested.
        final String ssHost = System.getProperty("selenium.server.host");
        if (ssHost == null)
        {
            throw new RuntimeException(
                    "selenium.server.host System property not set");
        }
        else
        {
            setSeleniumServerHost(ssHost);
        }

        // Port of the web application to be tested.
        final String ssPort = System.getProperty("selenium.server.port");
        if (ssPort == null)
        {
            throw new RuntimeException(
                    "selenium.server.port System property not set");
        }
        else
        {
            setSeleniumServerPort(Integer.parseInt(ssPort));
        }

        // Schema of the web application to be tested.
        final String browserCommand = System.getProperty("web.browser.command");
        if (!hasText(browserCommand))
        {
            LOGGER.warn("web.browser.command System property not set.  "
                        + "Defaulting to Mozilla Firefox.");
            setInternetBrowserCommand("");
        }
        else
        {
            setInternetBrowserCommand(browserCommand);
        }

        final String userName = System.getProperty("user.name");
        if (!hasText(userName))
        {
            LOGGER.warn("No username set!  Set the user.name system property "
                        + "if BASIC authentication is required.");
        }
        else
        {
            setUsername(userName);
        }

        if (hasText(getUsername()))
        {
            final String userPassword = System.getProperty("user.password");
            if (!hasText(userPassword))
            {
                LOGGER.warn("No password set!  Set the user.password system "
                            + "property if BASIC authentication is required.");
            }
            else
            {
                setPassword(userPassword);
            }
        }

        // Base Host of the web application to be tested.
        final String webSchema = System.getProperty("web.schema");
        final String webHost = System.getProperty("web.host");
        final String webPort = System.getProperty("web.port");
        if (!hasText(webSchema) || !hasText(webHost))
        {
            throw new RuntimeException("web.schema and web.host System "
                                       + "properties are missing.");
        }
        else
        {
            setWebURL(webSchema + "://" + webHost + (hasText(webPort)
                                                     ? ":" + webPort
                                                     : ""));
        }

        LOGGER.info("Web URL: " + getWebURL());
        LOGGER.info("Browser: " + getInternetBrowserCommand());
        LOGGER.info("Selenium Server: " + getSeleniumServerHost() + ":"
                    + getSeleniumServerPort());
        LOGGER.debug("Done with Abstract Web Test constructor.");
    }

    @Before
    public void setUp() throws Exception
    {
        final DesiredCapabilities capabilities;

        if (getInternetBrowserCommand().equals("*firefox"))
        {
            capabilities = DesiredCapabilities.firefox();
        }
        else if (getInternetBrowserCommand().equals("*safari"))
        {
            capabilities = DesiredCapabilities.safari();
        }
        else if (getInternetBrowserCommand().equals("*googlechrome"))
        {
            capabilities = DesiredCapabilities.chrome();
        }
        else
        {
            throw new IllegalArgumentException("No such browser.");
        }

        capabilities.setJavascriptEnabled(true);

        driver = new RemoteWebDriver(new URL("http://"
                                             + getSeleniumServerHost()
                                             + ":"
                                             + getSeleniumServerPort()
                                             + "/wd/hub"),
                                     capabilities);
        driver.manage().window().maximize();

        // Safari does not support setTimeout.
        if (!getInternetBrowserCommand().equals("*safari"))
        {
            // Set the timeout to four minutes.
            driver.manage().timeouts()
                    .pageLoadTimeout(TIMEOUT_IN_MILLISECONDS,
                                     TimeUnit.MILLISECONDS);
        }

        driver.manage().timeouts().implicitlyWait(5000,
                                                  TimeUnit.MILLISECONDS);
    }

    @After
    public void tearDown() throws Exception
    {
        driver.quit();
    }

    @Test
    public void runTests() throws Exception
    {
        try
        {
            runAllTests();
        }
        catch (Throwable t)
        {
            String filename = getName() + ".png";
            try
            {
                final WebDriver augmentedDriver =
                        new Augmenter().augment(driver);
                final File sourceFile =
                        ((TakesScreenshot) augmentedDriver).getScreenshotAs(
                                OutputType.FILE);

                FileUtils.copyFile(sourceFile, new File("./" + filename));

                System.err.println("Saved screenshot " + filename);
            }
            catch (Exception e)
            {
                System.err.println("Couldn't save screenshot " + filename
                                   + ": " + e.getMessage());
                e.printStackTrace();
            }

            throw new Exception(t);
        }
        finally
        {
            tearDown();
        }
    }

    /**
     * Run the tests for this web application.
     *
     * @throws Exception If anything goes wrong.
     */
    protected abstract void runAllTests() throws Exception;

    /**
     * Like assertTrue, but fails at the end of the test (during tearDown)
     *
     * @param b The boolean flag to check for truthiness.
     */
    public void verifyTrue(final boolean b)
    {
        if (!b)
        {
            throw new IllegalArgumentException("Verification failed.");
        }
    }

    protected void verifyEquals(final Object o1, final Object o2)
    {
        verifyTrue(o1.equals(o2));
    }

    protected WebElement find(final By by)
    {
        try
        {
            return driver.findElement(by);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    protected void verifyElementPresent(By by)
    {
        final WebElement webElement = driver.findElement(by);
        verifyFalse(webElement == null);
    }

    void verifyDisabledInput(final String idSelector)
    {
        final Object obj =
                ((JavascriptExecutor) driver).executeScript(
                        "return document.getElementById('" + idSelector
                        + "').disabled;");

        verifyTrue((obj != null) && ((Boolean) obj));
    }

    protected void verifyElementNotPresent(final By by) throws Exception
    {
        final WebElement webElement = driver.findElement(by);
        verifyTrue(webElement == null);
    }

    /**
     * Scroll the Grid.  This is for cadcVOTV grids.
     *
     * @param elementIDToScroll     The ID of the container.
     * @throws Exception
     */
    void scrollGrid(final String elementIDToScroll) throws Exception
    {
        final String findByClassNameLoop =
                "for (i in elems) {"
                + "if((' ' + elems[i].className + ' ').indexOf(' slick-viewport ') > -1) {"
                + "targetDiv = elems[i];break;"
                + "}}";
        final String script =
                "var objDiv = document.getElementById('" + elementIDToScroll
                + "'), targetDiv; var elems = objDiv.getElementsByTagName('div'), i;"
                + findByClassNameLoop
                + " targetDiv.scrollTop = targetDiv.scrollTop + 15;";

        ((JavascriptExecutor) driver).executeScript(script);
    }

    protected void verifyText(final By by, final String value)
    {
        verifyEquals(value, getText(by));
    }

    String getText(final By by)
    {
        return driver.findElement(by).getText();
    }

    protected void verifyTextPresent(final String text) throws Exception
    {
        verifyTrue(driver.getPageSource().contains(text));
    }

    public void verifyFalse(final boolean b)
    {
        if (b)
        {
            throw new IllegalArgumentException("Verification failed.");
        }
    }

    protected String getName()
    {
        return this.getClass().getName();
    }

    protected void waitForTextPresent(final String text) throws Exception
    {
        while (!driver.getPageSource().contains(text))
        {
            waitOneSecond();
        }

        waitOneSecond();
        setCurrentWaitTime(0);
    }

    void waitForTextPresent(final By by, final String text) throws Exception
    {
        waitForElementPresent(by);
        while (!find(by).getText().contains(text))
        {
            waitFor(500l);
        }
    }

    protected void waitForElementPresent(final By by)
    {
        WebDriverWait webDriverWait = new WebDriverWait(driver,
                                                        getCurrentWaitTime());
        WebElement element =
                (webDriverWait).until(
                        ExpectedConditions.presenceOfElementLocated(by));
        if (element == null)
        {
            fail("Could not find " + by.toString());
        }
    }

    protected void waitFor(final int seconds) throws Exception
    {
        int count = 0;
        while (count <= seconds)
        {
            waitOneSecond();
            count++;
        }

        setCurrentWaitTime(0);
    }

    /**
     * Check if a String has length.
     * <p><pre>
     * StringUtil.hasLength(null) = false
     * StringUtil.hasLength("") = false
     * StringUtil.hasLength(" ") = true
     * StringUtil.hasLength("Hello") = true
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is not null and has length
     */
    public boolean hasLength(String str)
    {
        return ((str != null) && (str.length() > 0));
    }

    /**
     * Check if a String has text. More specifically, returns <code>true</code>
     * if the string not <code>null<code>, it's <code>length is > 0</code>, and
     * it has at least one non-whitespace character.
     * <p><pre>
     * StringUtil.hasText(null) = false
     * StringUtil.hasText("") = false
     * StringUtil.hasText(" ") = false
     * StringUtil.hasText("12345") = true
     * StringUtil.hasText(" 12345 ") = true
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is not null, length > 0,
     * and not whitespace only
     * @see Character#isWhitespace
     */
    public boolean hasText(String str)
    {
        if (!hasLength(str))
        {
            return false;
        }

        for (int i = 0; i < str.length(); i++)
        {
            if (!Character.isWhitespace(str.charAt(i)))
            {
                return true;
            }
        }

        return false;
    }

    public String getSeleniumServerHost()
    {
        return seleniumServerHost;
    }

    public void setSeleniumServerHost(final String seleniumServerHost)
    {
        this.seleniumServerHost = seleniumServerHost;
    }

    public int getSeleniumServerPort()
    {
        return seleniumServerPort;
    }

    public void setSeleniumServerPort(int seleniumServerPort)
    {
        this.seleniumServerPort = seleniumServerPort;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getWebURL()
    {
        return webURL;
    }

    public void setWebURL(String webURL)
    {
        this.webURL = webURL;
    }

    public String getInternetBrowserCommand()
    {
        return internetBrowserCommand;
    }

    public void setInternetBrowserCommand(String internetBrowserCommand)
    {
        this.internetBrowserCommand = internetBrowserCommand;
    }

    protected int getCurrentWaitTime()
    {
        return currentWaitTime;
    }

    public void setCurrentWaitTime(final int currentWaitTime)
    {
        this.currentWaitTime = currentWaitTime;
    }

    /**
     * Fails a test with the given message.
     *
     * @param message Message to display explaining the failure.
     */
    public void fail(final String message)
    {
        throw new AssertionFailedError(message);
    }

    protected boolean isFailOnTimeout()
    {
        return failOnTimeout;
    }

    protected void setFailOnTimeout(boolean failOnTimeout)
    {
        this.failOnTimeout = failOnTimeout;
    }

    /**
     * Wait one second.
     *
     * @throws Exception If anything went wrong.
     */
    protected void waitOneSecond() throws Exception
    {
        if (isFailOnTimeout()
            && (getCurrentWaitTime() >= TIMEOUT_IN_MILLISECONDS))
        {
            fail("Timed out.");
        }
        else
        {
            setCurrentWaitTime(getCurrentWaitTime() + 1000);
            waitFor(1000l);
        }
    }

    /**
     * Allow waiting for less than a second.
     *
     * @param milliseconds Time in milliseconds to wait.
     * @throws Exception
     */
    void waitFor(final long milliseconds) throws Exception
    {
        Thread.sleep(milliseconds);
    }
}
