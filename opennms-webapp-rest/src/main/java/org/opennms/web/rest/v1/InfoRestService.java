/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.text.ParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.resource.Vault;
import org.opennms.core.time.CentralizedDateTimeFormat;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.timeformat.api.TimeformatService;
import org.opennms.netmgt.vmmgr.Controller;
import org.opennms.netmgt.vmmgr.StatusGetter;
import org.opennms.web.rest.v1.config.DatetimeformatConfig;
import org.opennms.web.rest.v1.config.TicketerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("infoRestService")
@Path("info")
@Tag(name = "Info", description = "Info API")
@Transactional
public class InfoRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(InfoRestService.class);

    private static StatusGetter s_statusGetter;

    @Autowired
    TimeformatService timeformatService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfo(@Context HttpServletRequest httpServletRequest) throws ParseException {
        final SystemInfoUtils sysInfoUtils = new SystemInfoUtils();

        final InfoDTO info = new InfoDTO();
        info.setDisplayVersion(sysInfoUtils.getDisplayVersion());
        info.setVersion(sysInfoUtils.getVersion());
        info.setPackageName(sysInfoUtils.getPackageName());
        info.setPackageDescription(sysInfoUtils.getPackageDescription());
        info.setTicketerConfig(getTicketerConfig());
        info.setDatetimeformatConfig(getDateformatConfig(httpServletRequest.getSession(false)));
        info.setServices(this.getServices());
        return Response.ok().entity(info).build();
    }

    private DatetimeformatConfig getDateformatConfig(HttpSession session) {
        DatetimeformatConfig config = new DatetimeformatConfig();
        config.setZoneId(extractUserTimeZoneId(session));
        config.setDatetimeformat(timeformatService.getFormatPattern());
        return config;
    }

    private ZoneId extractUserTimeZoneId(HttpSession session){
        ZoneId zoneId = null;
        if(session != null){
            zoneId = (ZoneId) session.getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID);
        }
        if(zoneId == null){
            zoneId = ZoneId.systemDefault();
        }
        return zoneId;
    }

    private TicketerConfig getTicketerConfig() {
        final TicketerConfig ticketerConfig = new TicketerConfig();
        ticketerConfig.setEnabled("true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled")));
        if (ticketerConfig.isEnabled()) {
            ticketerConfig.setPlugin(System.getProperty("opennms.ticketer.plugin"));
        }
        return ticketerConfig;
    }

    private Map<String,String> getServices() {
        if (InfoRestService.s_statusGetter == null) {
            InfoRestService.s_statusGetter = new StatusGetter(new Controller());
        }

        try {
            return s_statusGetter.retrieveStatus();
        } catch (final IllegalStateException e) {
            LOG.warn("Failed to retrieve statuses.  Info will be incomplete.");
        }

        return Collections.emptyMap();
    }

    static void setStatusGetter(final StatusGetter getter) {
        s_statusGetter = getter;
    }
}
