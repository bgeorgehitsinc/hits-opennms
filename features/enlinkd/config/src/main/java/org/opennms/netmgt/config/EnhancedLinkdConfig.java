/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;


/**
 * <p>EnhancedLinkdConfig interface.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 *
 * @version $Id: $
 */
public interface EnhancedLinkdConfig {

    /**
     * <p>getThreads</p>
     *
     * @return a int.
     */
    int getThreads();

    /**
     * <p>getExecutorThreads</p>
     *
     * @return a int.
     */
    int getExecutorThreads();

    /**
     * <p>getExecutorQueueSize</p>
     *
     * @return a int.
     */
    int getExecutorQueueSize();

    /**
     * <p>getMaxBft</p>
     *
     * @return a int.
     */
    int getMaxBft();

    /**
     * <p>getDiscoveryBridgeThreads</p>
     *
     * @return a int.
     */
    int getDiscoveryBridgeThreads();

    /**
     * <p>getInitialSleepTime</p>
     *
     * @return a long.
     */
    long getInitialSleepTime();

    /**
     * <p>getCdpRescanInterval</p>
     *
     * @return a long.
     */
    long getCdpRescanInterval();

    /**
     * <p>getCdpPriority</p>
     *
     * @return a int.
     */
    int getCdpPriority();

    /**
     * <p>getLldpRescanInterval</p>
     *
     * @return a long.
     */
    long getLldpRescanInterval();

    /**
     * <p>getLldpPriority</p>
     *
     * @return a int.
     */
    int getLldpPriority();

    /**
     * <p>getBridgeRescanInterval</p>
     *
     * @return a long.
     */
    long getBridgeRescanInterval();

    /**
     * <p>getBridgePriority</p>
     *
     * @return a int.
     */
    int getBridgePriority();

    /**
     * <p>getOspfRescanInterval</p>
     *
     * @return a long.
     */
    long getOspfRescanInterval();

    /**
     * <p>getOspfPriority</p>
     *
     * @return a int.
     */
    int getOspfPriority();

    /**
     * <p>getIsisRescanInterval</p>
     *
     * @return a long.
     */
    long getIsisRescanInterval();

    /**
     * <p>getIsisPriority</p>
     *
     * @return a int.
     */
    int getIsisPriority();

    /**
     * <p>getBridgeTopologyInterval</p>
     *
     * @return a long.
     */

    long getBridgeTopologyInterval();

    /**
     * <p>getTopologyUpdaterInterval</p>
     *
     * @return a long.
     */
    long getTopologyInterval();

    /**
     * <p>useCdpDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useCdpDiscovery();

    /**
     * <p>useBridgeDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useBridgeDiscovery();

    /**
     * <p>useLldpDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useLldpDiscovery();

    /**
     * <p>useOspfDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useOspfDiscovery();

    /**
     * <p>useIsisDiscovery</p>
     *
     * @return a boolean.
     */
    boolean useIsisDiscovery();

    boolean disableBridgeVlanDiscovery();

    /**
     * <p>reload</p>
     * <p>Reload the configuration file<p>
     *
     * @throws java.io.IOException if any.
     */
    void reload() throws IOException;
    
    /**
     * <p>save</p>
     *
     * @throws java.io.IOException if any.
     */
    void save() throws IOException;

    /**
     * <p>getConfiguration</p>
     *
     * @return a {@link org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration} object.
     */
     EnlinkdConfiguration getConfiguration();    
        
    Lock getReadLock();

    Lock getWriteLock();

}
