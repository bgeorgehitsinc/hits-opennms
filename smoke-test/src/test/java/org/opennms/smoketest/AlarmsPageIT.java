/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.smoketest.utils.RestClient;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AlarmsPageIT extends OpenNMSSeleniumIT {
    @Before
    public void createAlarm() throws Exception {
        final EventBuilder builder = new EventBuilder(EventConstants.IMPORT_FAILED_UEI, "AlarmsPageTest");
        builder.setParam("importResource", "foo");
        final Event ev = builder.getEvent();

        final RestClient restClient = stack.opennms().getRestClient();
        restClient.sendEvent(ev);
    }

    @Before
    public void setUp() throws Exception {
        alarmsPage();
    }

    protected void alarmsPage() {
        driver.get(getBaseUrlInternal() + "opennms/alarm/index.htm");
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(3, countElementsMatchingCss("div.card-header"));
        findElementByXpath("//span[text()='Alarm Queries']");
        findElementByXpath("//span[text()='Alarm Filter Favorites']");
        findElementByXpath("//span[text()='Outstanding and acknowledged alarms']");

        findElementByXpath("//form//input[@name='id']");
        findElementByXpath("//form//button[@type='submit']");
    }

    @Test
    public void testAllLinks() throws InterruptedException{
        findElementByLink("All alarms (summary)").click();
        findElementByXpath("//a[@title='Show acknowledged alarm(s)']");
        assertElementDoesNotExist(By.cssSelector("//table//th//a[text()='First Event Time']"));

        alarmsPage();
        findElementByLink("All alarms (detail)").click();
        findElementByXpath("//a[@title='Show acknowledged alarm(s)']");
        findElementByLink("First Event Time");

        alarmsPage();
        findElementByLink("Advanced Search").click();
        findElementByName("alarmtext");
        findElementByName("iplike");
    }

    @Test
    public void testAlarmLink() throws Exception {
        findElementByLink("All alarms (summary)").click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href,'alarm/detail.htm')]")));

        findElementByXpath("//a[contains(@href,'alarm/detail.htm')]").click();
        findElementByXpath("//tr[@class]//th[text()='Severity']");
    }

    @Test
    public void testAlarmIdNotFoundPage() throws InterruptedException {
        driver.get(getBaseUrlInternal() + "opennms/alarm/detail.htm?id=999999999");
        findElementByXpath("//h1[text()='Alarm Cleared or Not Found']");
    }
}
