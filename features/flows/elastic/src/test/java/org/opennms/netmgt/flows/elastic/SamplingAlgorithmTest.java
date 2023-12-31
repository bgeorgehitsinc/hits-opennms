/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.opennms.integration.api.v1.flows.Flow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
public class SamplingAlgorithmTest {

    @Test
    public void canMapAllValues() {
        for (Flow.SamplingAlgorithm samplingAlgorithm : Flow.SamplingAlgorithm.values()) {
            assertThat(SamplingAlgorithm.from(samplingAlgorithm), notNullValue());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    @PrepareForTest(Flow.SamplingAlgorithm.class)
    public void willThrowExceptionOnIllegalValue() {
        Flow.SamplingAlgorithm illegalValue = PowerMockito.mock(Flow.SamplingAlgorithm.class);

        Whitebox.setInternalState(illegalValue, "name", "illegalValue");
        Whitebox.setInternalState(illegalValue, "ordinal", 8);

        PowerMockito.mockStatic(Flow.SamplingAlgorithm.class);
        PowerMockito.when(Flow.SamplingAlgorithm.values()).thenReturn(new Flow.SamplingAlgorithm[]{
                Flow.SamplingAlgorithm.Unassigned,
                Flow.SamplingAlgorithm.SystematicCountBasedSampling,
                Flow.SamplingAlgorithm.SystematicTimeBasedSampling,
                Flow.SamplingAlgorithm.RandomNOutOfNSampling,
                Flow.SamplingAlgorithm.UniformProbabilisticSampling,
                Flow.SamplingAlgorithm.PropertyMatchFiltering,
                Flow.SamplingAlgorithm.HashBasedFiltering,
                Flow.SamplingAlgorithm.FlowStateDependentIntermediateFlowSelectionProcess,
                illegalValue});

        SamplingAlgorithm.from(illegalValue);
    }
}
