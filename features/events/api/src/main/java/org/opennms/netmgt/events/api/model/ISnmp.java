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

package org.opennms.netmgt.events.api.model;

/**
 * A definition corresponding to POJO '{@link org.opennms.netmgt.xml.event.Snmp}'.
 *
 * The 'has...()' methods exist since the corresponding 'get...()' methods will return a default value if null.
 * Using the 'has...()' method is the only means to determine if the backing value is null.
 */
public interface ISnmp {
    String getCommunity();
    Integer getGeneric();
    String getId();
    String getTrapOID();
    String getIdtext();
    Integer getSpecific();
    Long getTimeStamp();
    String getVersion();
    boolean hasGeneric();
    boolean hasSpecific();
    boolean hasTimeStamp();
}
