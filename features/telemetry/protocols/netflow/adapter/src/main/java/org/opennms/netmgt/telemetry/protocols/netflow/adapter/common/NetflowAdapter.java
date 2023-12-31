/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.common;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.processing.Pipeline;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.flows.AbstractFlowAdapter;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

public class NetflowAdapter extends AbstractFlowAdapter<FlowMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(NetflowAdapter.class);

    public NetflowAdapter(final AdapterDefinition adapterConfig,
                          final MetricRegistry metricRegistry,
                          final Pipeline pipeline) {
        super(adapterConfig, metricRegistry, pipeline);
    }

    @Override
    protected FlowMessage parse(TelemetryMessageLogEntry message) {
        try {
            return FlowMessage.parseFrom(message.getByteArray());
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Unable to parse message from proto", e);
        }
        return null;
    }

    @Override
    public List<Flow> convert(final FlowMessage packet, final Instant receivedAt) {
        return Collections.singletonList(new NetflowMessage(packet, receivedAt));
    }
}
