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

package org.opennms.web.svclayer.support;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SurveillanceViewConfigDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

public class DefaultSurveillanceServiceTest {
    
    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private SurveillanceViewConfigDao m_surveillanceViewConfigDao;
    
    @Before
    public void setUp() throws Exception {
        m_nodeDao = mock(NodeDao.class);
        m_categoryDao = mock(CategoryDao.class);
        m_surveillanceViewConfigDao = mock(SurveillanceViewConfigDao.class);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_nodeDao);
        verifyNoMoreInteractions(m_categoryDao);
        verifyNoMoreInteractions(m_surveillanceViewConfigDao);
    }

    @Test
    public void testCreateSurveillanceTable() {
        
        View view = new View();
        
        when(m_surveillanceViewConfigDao.getView(eq("default"))).thenReturn(view);

        DefaultSurveillanceService surveillanceSvc = new DefaultSurveillanceService();
        surveillanceSvc.setNodeDao(m_nodeDao);
        surveillanceSvc.setCategoryDao(m_categoryDao);
        surveillanceSvc.setSurveillanceConfigDao(m_surveillanceViewConfigDao);

        surveillanceSvc.createSurveillanceTable();

        verify(m_surveillanceViewConfigDao, atLeastOnce()).getView("default");
    }

    public Collection<OnmsCategory> createCategories(List<String> catNames) {
        Collection<OnmsCategory> categories = createCategoryNameCollection(catNames);
        return categories;
    }

    private Collection<OnmsCategory> createCategoryNameCollection(List<String> categoryNames) {
        
        Collection<OnmsCategory> categories = new ArrayList<>();
        for (String catName : categoryNames) {
            categories.add(m_categoryDao.findByName(catName));
        }
        return categories;
    }
    
    @Test
    public void testUrlMaker() {
        System.err.println(createNodePageUrl("1 of 10"));
        
    }
    
    private String createNodePageUrl(String label) {
        OnmsNode m_foundDownNode = new OnmsNode();
        m_foundDownNode.setId(1);
        if (m_foundDownNode != null) {
            label = "<a href=\"element/node.jsp?node="+m_foundDownNode.getId()+"\">"+label+"</a>";
        }
        return label;
    }


}
