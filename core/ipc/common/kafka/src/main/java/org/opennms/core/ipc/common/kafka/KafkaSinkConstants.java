/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.common.kafka;

public interface KafkaSinkConstants {

    String KAFKA_TOPIC_PREFIX = "Sink";

    String KAFKA_CONFIG_PID = "org.opennms.core.ipc.sink.kafka";

    String KAFKA_COMMON_CONFIG_PID = "org.opennms.core.ipc.kafka";

    String KAFKA_CONFIG_CONSUMER_PID = KAFKA_CONFIG_PID + ".consumer";

    String KAFKA_CONFIG_SYS_PROP_PREFIX = KAFKA_CONFIG_PID + ".";

    String KAFKA_COMMON_CONFIG_SYS_PROP_PREFIX = KAFKA_COMMON_CONFIG_PID + ".";

    // Configurable max buffer size for kafka that should be less than 900KB.
    String MAX_BUFFER_SIZE_PROPERTY = "max.buffer.size";
    //By default, kafka allows 1MB buffer sizes, here message (content in sink-message.proto) is limited to 900KB.
    int DEFAULT_MAX_BUFFER_SIZE = 921600;

    // Configurable messageId cache config to specify number of messages and time to expire.
    String MESSAGEID_CACHE_CONFIG = "messageId.cache.config";
    // Default to 1000 messages (large) in 10 minute interval.
    String DEFAULT_MESSAGEID_CONFIG = "maximumSize=1000,expireAfterWrite=10m";
}
