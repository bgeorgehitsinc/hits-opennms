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

package org.opennms.netmgt.enlinkd.service.api;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;

public class MacPort implements Topology {

    private Integer m_nodeId;
    private Integer m_macPortIfIndex;
    private String  m_macPortName;
    private final Map<String, Set<InetAddress>> m_macPortMap = new HashMap<>();
    
    public MacPort() {
    }
        
    public String getPortMacInfo() {
        
        final StringBuffer strbfr = new StringBuffer();
        m_macPortMap.keySet().forEach(mac -> {
            strbfr.append("ip:["); 
            m_macPortMap.get(mac).forEach(ip -> {
                strbfr.append(InetAddressUtils.str(ip)); 
                strbfr.append(" ");
            });
            strbfr.append("], mac:[");
            strbfr.append(mac);
            strbfr.append("]");
            
        });

        return strbfr.toString();
    }
    
    public String printTopology() {

        return "nodeid:[" +
                m_nodeId +
                "], port name:[" +
                m_macPortName +
                "], ifindex:[" +
                m_macPortIfIndex +
                "], macPortMap:[" +
                m_macPortMap +
                "]";
    }

    public Integer getIfIndex() {
        return m_macPortIfIndex;
    }
    public void setIfIndex(Integer macPortIfIndex) {
        m_macPortIfIndex = macPortIfIndex;
    }
    public String getMacPortName() {
        return m_macPortName;
    }
    public void setMacPortName(String macPortName) {
        m_macPortName = macPortName;
    }
    public Map<String, Set<InetAddress>> getMacPortMap() {
        return m_macPortMap;
    }
    public Integer getNodeId() {
        return m_nodeId;
    }
    public void setNodeId(Integer nodeId) {
        m_nodeId = nodeId;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_macPortIfIndex == null) ? 0
                                              : m_macPortIfIndex.hashCode());
        result = prime * result
                + m_macPortMap.hashCode();
        result = prime * result
                + ((m_macPortName == null) ? 0 : m_macPortName.hashCode());
        result = prime * result
                + ((m_nodeId == null) ? 0 : m_nodeId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MacPort other = (MacPort) obj;
        if (m_macPortIfIndex == null) {
            if (other.m_macPortIfIndex != null)
                return false;
        } else if (!m_macPortIfIndex.equals(other.m_macPortIfIndex))
            return false;
        if (!m_macPortMap.equals(other.m_macPortMap))
            return false;
        if (m_macPortName == null) {
            if (other.m_macPortName != null)
                return false;
        } else if (!m_macPortName.equals(other.m_macPortName))
            return false;
        if (m_nodeId == null) {
            return other.m_nodeId == null;
        } else return m_nodeId.equals(other.m_nodeId);
    }
}
