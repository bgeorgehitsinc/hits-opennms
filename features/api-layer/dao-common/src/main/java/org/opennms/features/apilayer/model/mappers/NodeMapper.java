/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.model.mappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.opennms.core.utils.LocationUtils;
import org.opennms.integration.api.v1.model.immutables.ImmutableNode;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

@Mapper(uses = {GeolocationMapper.class, NodeAssetRecordMapper.class, IpInterfaceMapper.class,
        SnmpInterfaceMapper.class, MetaDataMapper.class, MonitoredServiceMapper.class})
public interface NodeMapper {
    ImmutableNode map(OnmsNode onmsNode);

    default String mapLocation(OnmsMonitoringLocation onmsMonitoringLocation) {
        return onmsMonitoringLocation == null ? LocationUtils.DEFAULT_LOCATION_NAME :
                LocationUtils.getEffectiveLocationName(onmsMonitoringLocation.getLocationName());
    }

    default List<String> mapCategories(Set<OnmsCategory> categories) {
        return categories == null ? Collections.emptyList() :
                categories.stream().map(OnmsCategory::getName).collect(Collectors.toList());
    }
}
