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

package org.opennms.features.apilayer.common.requisition.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.opennms.integration.api.v1.config.requisition.immutables.ImmutableRequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;

@Mapper(uses={RequisitionMetaDataMapper.class})
public interface RequisitionMonitoredServiceMapper {

    @Mappings({
            @Mapping(source = "serviceName", target = "name")
    })
    ImmutableRequisitionMonitoredService map(RequisitionMonitoredService service);

    @Mappings({
            @Mapping(source = "name", target = "serviceName")
    })
    RequisitionMonitoredService map(org.opennms.integration.api.v1.config.requisition.RequisitionMonitoredService service);

}
