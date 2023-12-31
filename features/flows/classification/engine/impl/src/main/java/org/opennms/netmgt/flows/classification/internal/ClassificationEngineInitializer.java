/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Is required to initialize the classification engine properly, as it requires a transaction to load correctly.
// While starting the bundle, the initialization occurs from within the blueprint container, thus no transaction is available
// This bean wraps the loading in a transaction, ensuring loading can occurr correctly
public class ClassificationEngineInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ClassificationEngineInitializer.class);

    public ClassificationEngineInitializer(ClassificationEngine engine, SessionUtils sessionUtils) {
        sessionUtils.withReadOnlyTransaction(() -> {
            try {
                engine.reload();
            } catch (InterruptedException e) {
                LOG.error("reload was interrupted", e);
            }
            return null;
        });
    }
}
