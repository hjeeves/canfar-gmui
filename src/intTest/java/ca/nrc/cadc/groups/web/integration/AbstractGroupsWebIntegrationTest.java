/*
 ************************************************************************
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 *
 * (c) 2010.                         (c) 2010.
 * National Research Council            Conseil national de recherches
 * Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 * All rights reserved                  Tous droits reserves
 *
 * NRC disclaims any warranties         Le CNRC denie toute garantie
 * expressed, implied, or statu-        enoncee, implicite ou legale,
 * tory, of any kind with respect       de quelque nature que se soit,
 * to the software, including           concernant le logiciel, y com-
 * without limitation any war-          pris sans restriction toute
 * ranty of merchantability or          garantie de valeur marchande
 * fitness for a particular pur-        ou de pertinence pour un usage
 * pose.  NRC shall not be liable       particulier.  Le CNRC ne
 * in any event for any damages,        pourra en aucun cas etre tenu
 * whether direct or indirect,          responsable de tout dommage,
 * special or general, consequen-       direct ou indirect, particul-
 * tial or incidental, arising          ier ou general, accessoire ou
 * from the use of the software.        fortuit, resultant de l'utili-
 *                                      sation du logiciel.
 *
 *
 * @author jenkinsd
 * Oct 5, 2010 - 3:47:38 PM
 *
 *
 *
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 ************************************************************************
 */
package ca.nrc.cadc.groups.web.integration;



import ca.nrc.cadc.web.selenium.AbstractWebApplicationIntegrationTest;
import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import ca.nrc.cadc.util.StringUtil;


public abstract class AbstractGroupsWebIntegrationTest
        extends AbstractWebApplicationIntegrationTest
{
    protected void login(String path) throws Exception
    {
        // Click login
        driver.findElement(By.linkText(
                    path.equals("/fr/groupes/") ? "Connexion" : "Login")).
                    click();

        doLogin(path);
    }

    protected void doLogin(final String path) throws Exception
    {
        waitOneSecond();
        inputTextValue(By.id("username"), getUsername());
        inputTextValue(By.id("password"), getPassword());
        driver.findElement(By.id("login_button")).click();

        waitForTextPresent(
                path.equals("/fr/groupes/") ? "Déconnexion" : "Logout");
        waitOneSecond();
    }

    protected void logout(String path) throws Exception
    {
        driver.findElement(By.linkText("Sharon Goliath")).click();
        driver.findElement(By.linkText(
                     path.equals("/fr/groupes/") ? "Déconnexion" : "Logout"))
                .click();

        waitForElementPresent(By.linkText(
                    path.equals("/fr/groupes/") ? "Connexion" : "Login"));
    }

    protected String getAlertText() throws Exception
    {
        final WebElement alertElement =
                driver.findElement(By.id("gms_alert"));
        final String alertText;

        if (alertElement == null)
        {
            alertText = null;
        }
        else
        {
            alertText = alertElement.findElement(
                    By.className("gms_alert_reason")).getText();
        }

        return alertText;
    }

    protected void click(final By by) throws Exception
    {
        driver.findElement(by).click();
    }

    /**
     * Open the home page.
     *
     * @throws Exception
     */
    protected void goToHomePage(final String path) throws Exception
    {
        String alertTxt = "Login required";
        if (path.equals("/fr/groupes/"))
        {
            alertTxt = "Connexion requise";
        }

        final String alertText = getAlertText();

        goToHomePage(path, StringUtil.hasText(alertText)
                           && alertText.endsWith(alertTxt));
    }

    /**
     * Open the home page.
     *
     * @throws Exception
     */
    protected void goToHomePage(final String path,
                                final boolean expectLoginPage) throws Exception
    {
        driver.get(getWebURL() + path);

        // Should already be on login page, then.
        if (expectLoginPage)
        {
            doLogin(path);
        }

        waitForElementPresent(By.className("slick-viewport"));
        waitOneSecond();
    }

    protected void hover(final By by) throws Exception
    {
        // Wicked hack.  I hate this.
        // jenkinsd 2014.04.10
        //
        if (getInternetBrowserCommand().endsWith("afari"))
        {
            final String byString = by.toString();
            final String value =
                    byString.substring(byString.indexOf(":") + 1).trim();
            final String locatorPrefix;
            final String locatorSuffix;

            if (by instanceof By.ById)
            {
                locatorPrefix = "\"#";
                locatorSuffix = "\"";
            }
            else if (by instanceof By.ByClassName)
            {
                locatorPrefix = "\".";
                locatorSuffix = "\"";
            }
            else if (by instanceof By.ByLinkText)
            {
                locatorPrefix = "\"a:contains('";
                locatorSuffix = "')\"";
            }
            else if (by instanceof By.ByName)
            {
                locatorPrefix = "\"[name='";
                locatorSuffix = "']\"";
            }
            else
            {
                locatorPrefix = "\"";
                locatorSuffix = "\"";
            }

            ((JavascriptExecutor) driver).
                    executeScript("$(" + locatorPrefix + value
                                  + locatorSuffix + ").hover();");
        }
        else
        {
            final Actions action = new Actions(driver);
            final WebElement tooltip = driver.findElement(by);
            action.moveToElement(tooltip).perform();
        }
    }

    /**
     * Generate an ASCII string, replacing the '\' and '+' characters with
     * underscores to keep them URL friendly.
     *
     * @param length        The desired length of the generated string.
     * @return              An ASCII string of the given length.
     */
    protected String generateAlphaNumeric(final int length)
    {
        return RandomStringUtils.randomAlphanumeric(length);
    }
}
