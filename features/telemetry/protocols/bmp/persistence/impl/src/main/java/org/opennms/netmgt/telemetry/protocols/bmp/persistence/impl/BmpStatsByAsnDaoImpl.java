/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.persistence.impl;

import java.util.Date;
import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByAsn;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpStatsByAsnDao;

public class BmpStatsByAsnDaoImpl extends AbstractDaoHibernate<BmpStatsByAsn, Long> implements BmpStatsByAsnDao {
    public BmpStatsByAsnDaoImpl() {
        super(BmpStatsByAsn.class);
    }

    @Override
    public BmpStatsByAsn findByAsnAndIntervalTime(String peerHashId, Long originAs, Date intervalTime) {
        
        Criteria criteria = new Criteria(BmpStatsByAsn.class);
        criteria.addRestriction(new EqRestriction("peerHashId", peerHashId));
        criteria.addRestriction(new EqRestriction("originAsn", originAs));
        criteria.addRestriction(new EqRestriction("timestamp", intervalTime));
        List<BmpStatsByAsn> bmpStatsByAsnList = findMatching(criteria);
        if (bmpStatsByAsnList != null && bmpStatsByAsnList.size() > 0) {
            return bmpStatsByAsnList.get(0);
        }
        return null;
    }
}
