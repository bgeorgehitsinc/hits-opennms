/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.sampleread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.math.BigDecimal;

public class LateAggregationParamsTest {

    @Test
    public void canCalculateLagParams() {

        // Supply sane values and make sure the same values are returned
        LateAggregationParams lag = LateAggregationParams.builder().step(300 * 1000L).interval(150 * 1000L).heartbeat(450 * 1000L).build();
        assertEquals(300 * 1000L, lag.step);
        assertEquals(150 * 1000L, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);

        // Supply a step that is not a multiple of the interval, make sure this is corrected
        lag = LateAggregationParams.builder().step(310 * 1000L).interval(150 * 1000L).heartbeat(450 * 1000L).build();
        assertEquals(310000L, lag.step);
        assertEquals(155000L, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);

        // Supply an interval that is much larger than the step
        lag = LateAggregationParams.builder().step(300 * 1000L).interval(1500 * 1000L).heartbeat(45000 * 1000L).build();
        assertEquals(300 * 1000L, lag.step);
        // Interval should be reduced
        assertEquals(150 * 1000L, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);

        var stepSize = 300 * 1000L;
        lag = LateAggregationParams.builder().step(stepSize).build();
        assertEquals(stepSize, lag.step);
        // heartbeat should same as default when step is smaller than DEFAULT_HEARTBEAT_MS
        assertEquals(LateAggregationParams.DEFAULT_HEARTBEAT_MS, lag.heartbeat);
        assertEquals(stepSize / LateAggregationParams.INTERVAL_DIVIDER, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);

        // make sure if step is bigger than DEFAULT_HEARTBEAT_MS
        stepSize = LateAggregationParams.DEFAULT_HEARTBEAT_MS * 10;
        lag = LateAggregationParams.builder().step(stepSize).build();
        assertEquals(stepSize, lag.step);
        assertEquals(LateAggregationParams.DEFAULT_HEARTBEAT_MULTIPLIER.multiply(new BigDecimal(stepSize)).longValue(), lag.heartbeat);
        assertEquals(stepSize / LateAggregationParams.INTERVAL_DIVIDER, lag.interval);
        assertTrue(lag.heartbeat >= lag.step);
    }


    @Test
    public void canLimitStepSize() {

        // Request a step size smaller than the lower bound
        LateAggregationParams lag = LateAggregationParams.builder().step(LateAggregationParams.MIN_STEP_MS - 1).build();
        assertEquals(LateAggregationParams.MIN_STEP_MS, lag.step);
    }
}
