/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;

import org.opennms.netmgt.poller.support.TimeoutTracker;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerParameter;
import org.opennms.netmgt.poller.monitors.support.ParameterSubstitutingMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * This class is designed to be used by the service poller framework to test the availability
 * of the existence of files or directories on remote interfaces via CIFS. The class implements
 * the ServiceMonitor interface that allows it to be used along with other plug-ins by the service
 * poller framework.
 *
 * @author <a mailto:christian.pape@informatik.hs-fulda.de>Christian Pape</a>
 * @version 1.10.9
 */
public class JCifsMonitor extends ParameterSubstitutingMonitor {

    /*
    * default retries
    */
    private static final int DEFAULT_RETRY = 0;

    /*
     * default timeout
     */
    private static final int DEFAULT_TIMEOUT = 3000;

    private static String modeCandidates;

    static {
        modeCandidates = "";
        for (Mode m : Mode.values()) {
            if (!"".equals(modeCandidates)) {
                modeCandidates += ", ";
            }
            modeCandidates += m;
        }
    }

    /**
     * logging for JCifs monitor
     */
    private final Logger logger = LoggerFactory.getLogger(JCifsMonitor.class);

    /**
     * This method queries the CIFS share.
     *
     * @param svc        the monitored service
     * @param parameters the parameter map
     * @return the poll status for this system
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, PollerParameter> parameters) {

        final String domain = resolveKeyedString(parameters, "domain", "");
        final String username = resolveKeyedString(parameters, "username", "");
        final String password = resolveKeyedString(parameters, "password", "");
        String mode = getKeyedString(parameters, "mode", "PATH_EXIST").toUpperCase();
        String path = getKeyedString(parameters, "path", "");
        String smbHost = getKeyedString(parameters, "smbHost", "");
        final String folderIgnoreFiles = getKeyedString(parameters, "folderIgnoreFiles", "");

        // changing to Ip address of MonitoredService if no smbHost is given
        if ("".equals(smbHost)) {
            smbHost = svc.getIpAddr();
        }

        // Filename filter to give user the possibility to ignore specific files in folder for the folder check.
        SmbFilenameFilter smbFilenameFilter = new SmbFilenameFilter() {
            @Override
            public boolean accept(SmbFile smbFile, String s) throws SmbException {
                return !s.matches(folderIgnoreFiles);
            }
        };

        // Initialize mode with default as PATH_EXIST
        Mode enumMode = Mode.PATH_EXIST;

        try {
            enumMode = Mode.valueOf(mode);
        } catch (IllegalArgumentException exception) {
            logger.error("Mode '{}‘ does not exists. Valid candidates are {}", mode, modeCandidates);
            return PollStatus.unknown("Mode " + mode + " does not exists. Valid candidates are " + modeCandidates);
        }

        // Checking path parameter
        if (!path.startsWith("/")) {
            path = "/" + path;
            logger.debug("Added leading / to path.");
        }

        // Build authentication string for NtlmPasswordAuthentication: syntax: domain;username:password
        String authString = "";

        // Setting up authenticationString...
        if (domain != null && !"".equals(domain)) {
            authString += domain + ";";
        }

        authString += username + ":" + password;

        // ... and path
        String fullUrl = "smb://" + smbHost + path;

        logger.debug("Domain: [{}], Username: [{}], Password: [{}], Mode: [{}], Path: [{}], Authentication: [{}], Full Url: [{}]", new Object[]{domain, username, password, mode, path, authString, fullUrl});

        // Initializing TimeoutTracker with default values
        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        // Setting default PollStatus
        PollStatus serviceStatus = PollStatus.unknown();

        for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {

            NtlmPasswordAuthentication ntlmPasswordAuthentication = new NtlmPasswordAuthentication(authString);

            try {
                // Creating SmbFile object
                SmbFile smbFile = new SmbFile(fullUrl, ntlmPasswordAuthentication);
                // Setting the defined timeout
                smbFile.setConnectTimeout(tracker.getConnectionTimeout());
                // Does the file exists?
                boolean smbFileExists = smbFile.exists();

                switch (enumMode) {
                    case PATH_EXIST:
                        if (smbFileExists) {
                            serviceStatus = PollStatus.up();
                        } else {
                            serviceStatus = PollStatus.down("File " + fullUrl + " should exists but doesn't!");
                        }
                        break;
                    case PATH_NOT_EXIST:
                        if (!smbFileExists) {
                            serviceStatus = PollStatus.up();
                        } else {
                            serviceStatus = PollStatus.down("File " + fullUrl + " should not exists but does!");
                        }
                        break;
                    case FOLDER_EMPTY:
                        if (smbFileExists) {
                            if (smbFile.list(smbFilenameFilter).length == 0) {
                                serviceStatus = PollStatus.up();
                            } else {
                                serviceStatus = PollStatus.down("Directory " + fullUrl + " should be empty but isn't!");
                            }
                        } else {
                            serviceStatus = PollStatus.down("Directory " + fullUrl + " should exists but doesn't!");
                        }
                        break;
                    case FOLDER_NOT_EMPTY:
                        if (smbFileExists) {
                            if (smbFile.list(smbFilenameFilter).length > 0) {
                                serviceStatus = PollStatus.up();
                            } else {
                                serviceStatus = PollStatus.down("Directory " + fullUrl + " should not be empty but is!");
                            }
                        } else {
                            serviceStatus = PollStatus.down("Directory " + fullUrl + " should exists but doesn't!");
                        }
                        break;
                    default:
                        logger.warn("There is no implementation for the specified mode '{}'", mode);
                        break;
                }

            } catch (MalformedURLException exception) {
                logger.error("Malformed URL on '{}' with error: '{}'", smbHost, exception.getMessage());
                serviceStatus = PollStatus.down(exception.getMessage());
            } catch (SmbException exception) {
                logger.error("SMB error on '{}' with error: '{}'", smbHost, exception.getMessage());
                serviceStatus = PollStatus.down(exception.getMessage());
            }
        }

        return serviceStatus;
    }

    /**
     * Supported modes for CIFS monitor
     */
    private enum Mode {
        PATH_EXIST,
        PATH_NOT_EXIST,
        FOLDER_EMPTY,
        FOLDER_NOT_EMPTY
    }
}
