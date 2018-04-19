/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2016.                            (c) 2016.
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
 *
 ************************************************************************
 */

package ca.nrc.cadc.groups.web.integration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;


/**
 * Not actually a new page, but logistically it looks like it.
 */
public class EditGroupMembersPage extends GMUIPage
{
    static final By EDIT_MEMBERS_POPUP_SELECTOR =
            By.cssSelector("div#edit_members_container-popup.ui-popup-active");
    static final String AUTOCOMPLETE_LOADING_CLASSNAME =
            "ui-autocomplete-loading";
    static final By AUTOCOMPLETE_SELECTOR =
            By.cssSelector("#add-members-form > div.form-container > div.span-4 > span.ui-autocomplete-message");
    private static final By FIRST_AUTOCOMPLETE_SUGGESTION =
            By.xpath("//ul[@id='ui-id-1']/li[contains(.,'cadcauthtest1')]");
    static final String MEMBER_ENTRY_SELECTOR_STRING =
            "//div[@id='members_grid']//span[contains(@class, 'cellValue')][@title='%s']";


    @FindBy(id = "members-search")
    private WebElement memberSearchField;

    @FindBy(css = "#members_grid-container > div.grid-header.width-100 > span.grid-header-label")
    private WebElement gridHeaderLabel;

    @FindBy(id = "add_button_members")
    private WebElement addMemberButton;

    @FindBy(id = "done_form_members")
    private WebElement doneButton;


    public EditGroupMembersPage(final WebDriver _driver) throws Exception
    {
        super(_driver);

        PageFactory.initElements(driver, this);
    }


    public String getGridHeaderLabelText() throws Exception
    {
        return gridHeaderLabel.getText();
    }

    public void enterMemberName(final String memberName) throws Exception
    {
        sendKeys(memberSearchField, "");
        memberSearchField.clear();

        sendKeys(memberSearchField, memberName);
    }

    public void selectFirstAutocompleteSuggestion() throws Exception
    {
        click(FIRST_AUTOCOMPLETE_SUGGESTION);
    }

    public void addMember() throws Exception
    {
        click(addMemberButton);
    }

    public WebElement findEntry(final String entryTitle) throws Exception
    {
        final By entry = By.xpath(String.format(MEMBER_ENTRY_SELECTOR_STRING,
                                                entryTitle));
        waitForElementPresent(entry);

        return find(entry);
    }

    public void done() throws Exception
    {
        click(doneButton);
    }
}
