/*
 ************************************************************************
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 *
 * (c) 2014.                         (c) 2014.
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
 * 10/17/14 - 2:54 PM
 *
 *
 *
 ****  C A N A D I A N   A S T R O N O M Y   D A T A   C E N T R E  *****
 ************************************************************************
 */

package ca.nrc.cadc.groups.web.integration;

import ca.nrc.cadc.web.selenium.AbstractWebApplicationIntegrationTest;
import ca.nrc.cadc.web.selenium.AnonymousPage;
import ca.nrc.cadc.web.selenium.LoginPage;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.*;


public class LDAPWalkthrough extends AbstractWebApplicationIntegrationTest {
    public LDAPWalkthrough() throws Exception {
        super();
        setFailOnTimeout(true);
    }

    @Test
    public void walkThrough() throws Exception {
        final AnonymousPage anonymousPage = goTo("/canfar/", "",
                                                 AnonymousPage.class);
        final LoginPage loginPage = anonymousPage.goToLoginPage();
        final AuthenticatedCANFARDashboardPage dashboardPage =
            loginPage.doLogin(getUsername(), getPassword(),
                              AuthenticatedCANFARDashboardPage.class);

        GMUIPage groupManagementPage = dashboardPage.clickGMUI();
        final String newGroupName = generateGroupName();

        final CreateGroupPage createGroupPage =
            groupManagementPage.createGroup();

        createGroupPage.enterGroupName(newGroupName);
        groupManagementPage = createGroupPage.submit();

        while (groupManagementPage.getGroupLink(newGroupName) == null) {
            groupManagementPage.scrollDown();
        }

        EditGroupMembersPage editGroupMembersPage =
            groupManagementPage.editGroupMembers(newGroupName);

        assertTrue("Wrong header.",
                   editGroupMembersPage.getGridHeaderLabelText()
                                       .contains("Showing 1 rows"));

        editGroupMembersPage.enterMemberName("cadc");
        editGroupMembersPage.waitForTextPresent(
            EditGroupMembersPage.AUTOCOMPLETE_SELECTOR,
            "more not shown here.");

        editGroupMembersPage.enterMemberName("cadcauth");
        editGroupMembersPage.selectFirstAutocompleteSuggestion();
        editGroupMembersPage.addMember();

        final WebElement memberEntry =
            editGroupMembersPage.findEntry("CADC Authtest1");

        assertNotNull("No new entry for CADC Authtest1", memberEntry);

        editGroupMembersPage.done();

        // Add administrator
        final EditGroupAdministratorsPage editGroupAdministratorsPage =
            groupManagementPage.editGroupAdministrators(newGroupName);

        editGroupAdministratorsPage.enterAdminName("cadc");

        editGroupAdministratorsPage.waitForTextPresent(
            EditGroupAdministratorsPage.AUTOCOMPLETE_SELECTOR,
            "more not shown here.");

        editGroupAdministratorsPage.enterAdminName("cadcauth");
        editGroupAdministratorsPage.selectFirstAutocompleteSuggestion();
        editGroupAdministratorsPage.addAdmin();

        final WebElement adminEntry =
            editGroupAdministratorsPage.findEntry("CADC Authtest2");

        assertNotNull("No new entry for CADC Authtest1", adminEntry);

        editGroupAdministratorsPage.done();
    }

    private String generateGroupName() {
        return new RandomStringGenerator.Builder().withinRange('A', 'Z').build().generate(16);
    }
}
