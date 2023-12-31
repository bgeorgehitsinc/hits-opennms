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

package org.opennms.netmgt.passive.jmx;

import org.apache.commons.lang.NotImplementedException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.passive.PassiveStatusKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>PassiveStatusd class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class PassiveStatusd extends AbstractServiceDaemon implements PassiveStatusdMBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(PassiveStatusd.class);
    /**
     * <p>Constructor for PassiveStatusd.</p>
     */
    public PassiveStatusd() {
        super(NAME);
    }

    public final static String NAME = "passive";

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        EventIpcManagerFactory.init();
        EventIpcManager mgr = EventIpcManagerFactory.getIpcManager();

        PassiveStatusKeeper keeper = getPassiveStatusKeeper();
        keeper.setEventManager(mgr);
        keeper.setDataSource(DataSourceFactory.getInstance());
        keeper.init();
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        getPassiveStatusKeeper().start();
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        getPassiveStatusKeeper().stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return getPassiveStatusKeeper().getStatus();
    }

    @Override
    public long getStartTimeMilliseconds() {
        throw new NotImplementedException();
    }

    private PassiveStatusKeeper getPassiveStatusKeeper() {
        return PassiveStatusKeeper.getInstance();
    }
}
