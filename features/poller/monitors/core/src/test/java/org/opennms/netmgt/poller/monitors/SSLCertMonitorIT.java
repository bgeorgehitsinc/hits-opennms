/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.netmgt.utils.DnsUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitConfigurationEnvironment
public class SSLCertMonitorIT {

    // The certificate JUnitHttpServer uses is valid:
    //   from  Tue Nov 08 14:30:45 CET 2016
    //   until Fri Nov 03 14:30:45 CET 2017
    private static final long EXPIRE_DATE = 1509715800000L;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    @JUnitHttpServer(port=10342, https=true)
    public void testValidDateForCertificate() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor() {
            @Override
            protected Calendar getCalendarInstance() {
                final Calendar cal = GregorianCalendar.getInstance();
                cal.setTimeInMillis(EXPIRE_DATE - 86400000 * 5);
                return cal;
            }
        };

        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "10342");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");

        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "SSLCert");
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());
    }

    @Test
    @JUnitHttpServer(port=10342, https=true)
    public void testExpiringDateForCertificate() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor() {
            @Override
            protected Calendar getCalendarInstance() {
                final Calendar cal = GregorianCalendar.getInstance();
                cal.setTimeInMillis(EXPIRE_DATE - 86400000 * 4);
                return cal;
            }
        };

        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "10342");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");

        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "SSLCert");
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isUnavailable());
    }

    @Test
    @JUnitHttpServer(port=10342, https=true)
    public void testExpiredDateForCertificate() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor() {
            @Override
            protected Calendar getCalendarInstance() {
                final Calendar cal = GregorianCalendar.getInstance();
                cal.setTimeInMillis(EXPIRE_DATE - 86400000 * -1);
                return cal;
            }
        };

        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "10342");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");

        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "SSLCert");
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isUnavailable());
    }

    @Test
    @JUnitHttpServer(port=10342, https=true, vhosts = "test.example.com")
    public void testHostNameVerificationSucceeds() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor() {
            @Override
            protected Calendar getCalendarInstance() {
                final Calendar cal = GregorianCalendar.getInstance();
                cal.setTimeInMillis(EXPIRE_DATE - 86400000 * 5);
                return cal;
            }
        };

        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "10342");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");
        parameters.put("server-name", "${nodelabel}.example.com");

        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "test", DnsUtils.resolveHostname("localhost", false), "SSLCert");
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());
    }

    @Test
    @JUnitHttpServer(port=10342, https=true)
    public void testHostNameVerificationFails() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor() {
            @Override
            protected Calendar getCalendarInstance() {
                final Calendar cal = GregorianCalendar.getInstance();
                cal.setTimeInMillis(EXPIRE_DATE - 86400000 * 5);
                return cal;
            }
        };

        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "10342");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");
        parameters.put("server-name", "klatschmohnwiese");

        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "SSLCert");
        PollStatus status = monitor.poll(svc, parameters);

        assertTrue(status.isUnavailable());
    }

    @Test
    @JUnitHttpServer(port=10342, https=true)
    public void testStartTLSFails() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor() {
            @Override
            protected Calendar getCalendarInstance() {
                final Calendar cal = GregorianCalendar.getInstance();
                cal.setTimeInMillis(EXPIRE_DATE - 86400000 * 5);
                return cal;
            }
        };

        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "10342");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");
        parameters.put("starttls-preamble", "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' xmlns:tls='http://www.ietf.org/rfc/rfc2595.txt' to='jabber.org'>");
        parameters.put("starttls-preamble-response", "^.*rfc2595.*$");
        parameters.put("starttls-start", "<tls:starttls/>");
        parameters.put("starttls-start-response", "^.*stream:stream.*$");

        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "SSLCert");
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isUnavailable());

    }

    @Test
    @Ignore("point at an XMPP server that supports STARTTLS to use this test")
    public void testXMPPStartTLS() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "5222");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");
        parameters.put("starttls-preamble", "<stream:stream xmlns:stream='http://etherx.jabber.org/streams' xmlns='jabber:client' to='{ipAddr}' version='1.0'>");
        parameters.put("starttls-preamble-response", "^.*starttls.*$");
        parameters.put("starttls-start", "<starttls xmlns='urn:ietf:params:xml:ns:xmpp-tls'/>");
        parameters.put("starttls-start-response", "^.*proceed.*$");

        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", InetAddress.getByName("127.0.0.1"), "SSLCert");
        // this would normally happen in the poller request builder implementation
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
        subbedParams.forEach((k, v) -> {
            parameters.put(k, v);
        });
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());
    }

}
