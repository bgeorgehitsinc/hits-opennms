/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.test;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

public abstract class OnmsAssert {

    public static void assertArrayEqualsIgnoreOrder(final Object[] a, final Object[] b) {
        final List<?> aList = Arrays.asList(a);
        final List<?> bList = Arrays.asList(b);
        Assert.assertTrue(aList.containsAll(bList) && bList.containsAll(aList));
    }

    public static void assertThrowsException(Class<? extends Throwable> expectedException, RunnableWithException function) {
        try {
            function.run();
        } catch(Exception e) {
            if(!expectedException.isAssignableFrom(e.getClass())) {
                fail(String.format("Expected exception: %s but was %s", expectedException.getName(), e.getClass().getName()));
            }
            return;
        }
        fail(String.format("Expected exception: %s but none was thrown.", expectedException.getName()));
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

}
