/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.operations;

import org.opennms.netmgt.provision.service.ProvisionService;

public class UpdateOperation extends SaveOrUpdateOperation {
    
    /**
     * <p>Constructor for UpdateOperation.</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param location a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @param rescanExisting a {@link java.lang.String} object
     * @param monitorKey a {@link java.lang.String} object (optional)
     */
    public UpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String location, String building, String city, ProvisionService provisionService, String rescanExisting, String monitorKey) {
        super(nodeId, foreignSource, foreignId, nodeLabel, location, building, city, provisionService, rescanExisting, monitorKey);
    }

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Override
	public String toString() {
       return "UPDATE: Node: "+(getNode().getId() == null ? "[no ID]" : getNode().getId())+": "+getNode().getLabel();
    }

	/** {@inheritDoc} */
	@Override
    protected void doPersist() {
        getProvisionService().updateNode(getNode(), getRescanExisting(), getMonitorKey());
    }
}
