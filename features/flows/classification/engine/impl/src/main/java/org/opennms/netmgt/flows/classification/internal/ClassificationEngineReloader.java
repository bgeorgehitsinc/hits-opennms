/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassificationEngineReloader {

    private static final Logger LOG = LoggerFactory.getLogger(ClassificationEngineReloader.class);

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public ClassificationEngineReloader(Identity identity, ClassificationEngine engine, String reloadIntervalString){
        if (identity != null) {
            final int reloadInterval = Integer.parseInt(reloadIntervalString);
            LOG.debug("Scheduling reload of classification engine every {} seconds", reloadInterval);
            executorService.scheduleWithFixedDelay(() -> {
                LOG.debug("Performing reload of Classification Engine...");
                try {
                    engine.reload();
                } catch (InterruptedException e) {
                    LOG.error("reload was interrupted", e);
                }
                LOG.debug("Reload of Classification Engine performed. Next reload will be in {} seconds", reloadInterval);
            }, reloadInterval, reloadInterval, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        LOG.debug("Shutting down {}", getClass().getSimpleName());
        executorService.shutdown();
    }
}
