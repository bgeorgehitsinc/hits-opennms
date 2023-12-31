/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.jmxconfig;

import org.junit.Ignore;

/**
 *
 * @author Markus Neumann <markus@opennms.com>
 */
@Ignore("this is used by other tests, but is not a test itself")
public class JmxTestDummy implements JmxTestDummyMBean {

    private int writable = 0;

    @Override
    public String getName() {
        return "JmxTest";
    }

    @Override
    public int getX() {
        return 42;
    }

    @Override
    public Integer getInteger() {
        return getX();
    }

    @Override
    public Long getLong() {
        return Long.valueOf(getX());
    }

    @Override
    public void setWritableY(int writable) {
        this.writable = writable;
    }

    @Override
    public int getWritableY() {
        return writable;
    }
}
