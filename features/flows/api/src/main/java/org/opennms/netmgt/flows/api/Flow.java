/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.api;

import static org.opennms.integration.api.v1.flows.Flow.Direction;
import static org.opennms.integration.api.v1.flows.Flow.NetflowVersion;
import static org.opennms.integration.api.v1.flows.Flow.SamplingAlgorithm;

import java.time.Instant;
import java.util.Optional;

public interface Flow {

    /**
     * Time at which the flow was received by listener in milliseconds since epoch UTC.
     */
    Instant getReceivedAt();

    /**
     * Flow timestamp in milliseconds.
     */
    Instant getTimestamp();

    /**
     * Number of bytes transferred in the flow.
     */
    Long getBytes();

    /**
     * Direction of the flow (egress vs ingress)
     */
    Direction getDirection();

    /**
     * Destination address.
     */
    String getDstAddr();

    /**
     * Destination address hostname.
     */
    Optional<String> getDstAddrHostname();

    /**
     * Destination autonomous system (AS).
     */
    Long getDstAs();

    /**
     * The number of contiguous bits in the source address subnet mask.
     */
    Integer getDstMaskLen();

    /**
     * Destination port.
     */
    Integer getDstPort();

    /**
     * Slot number of the flow-switching engine.
     */
    Integer getEngineId();

    /**
     * Type of flow-switching engine.
     */
    Integer getEngineType();

    /**
     * Unix timestamp in ms at which the previous exported packet
     * associated with this flow was switched.
     */
    Instant getDeltaSwitched();

    /**
     * Unix timestamp in ms at which the first packet
     * associated with this flow was switched.
     */
    Instant getFirstSwitched();

    /**
     * Number of flow records in the associated packet.
     */
    int getFlowRecords();

    /**
     * Flow packet sequence number.
     */
    long getFlowSeqNum();

    /**
     * SNMP ifIndex
     */
    Integer getInputSnmp();

    /**
     * IPv4 vs IPv6
     */
    Integer getIpProtocolVersion();

    /**
     * Unix timestamp in ms at which the last packet
     * associated with this flow was switched.
     */
    Instant getLastSwitched();

    /**
     * Next hop
     */
    String getNextHop();

    /**
     * Next hop hostname
     */
    Optional<String> getNextHopHostname();

    /**
     * SNMP ifIndex
     */
    Integer getOutputSnmp();

    /**
     * Number of packets in the flow
     */
    Long getPackets();

    /**
     * IP protocol number i.e 6 for TCP, 17 for UDP
     */
    Integer getProtocol();

    /**
     * Sampling algorithm ID
     */
    SamplingAlgorithm getSamplingAlgorithm();

    /**
     * Sampling interval
     */
    Double getSamplingInterval();

    /**
     * Source address.
     */
    String getSrcAddr();

    /**
     * Source address hostname.
     */
    Optional<String> getSrcAddrHostname();

    /**
     * Source autonomous system (AS).
     */
    Long getSrcAs();

    /**
     * The number of contiguous bits in the destination address subnet mask.
     */
    Integer getSrcMaskLen();

    /**
     * Source port.
     */
    Integer getSrcPort();

    /**
     * TCP Flags.
     */
    Integer getTcpFlags();

    /**
     * TOS.
     */
    Integer getTos();

    default Integer getDscp() {
        return getTos() != null ? getTos() >>> 2 : null;
    }

    default Integer getEcn() {
        return getTos() != null ? getTos() & 0x03 : null;
    }

    /**
     * Netfow version
     */
    NetflowVersion getNetflowVersion();

    /**
     * VLAN ID.
     */
    Integer getVlan();

    /**
     * Method to get node lookup identifier.
     *
     * This field can be used as an alternate means to identify the
     * exporter node when the source address of the packets are altered
     * due to address translation.
     *
     * * @return the identifier
     */
    String getNodeIdentifier();
}
