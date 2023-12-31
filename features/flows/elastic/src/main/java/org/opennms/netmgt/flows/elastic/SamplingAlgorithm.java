/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import org.opennms.integration.api.v1.flows.Flow;

import com.google.gson.annotations.SerializedName;

public enum SamplingAlgorithm {
    @SerializedName("Unassigned")
    Unassigned,
    @SerializedName("SystematicCountBasedSampling")
    SystematicCountBasedSampling,
    @SerializedName("SystematicTimeBasedSampling")
    SystematicTimeBasedSampling,
    @SerializedName("RandomNoutOfNSampling")
    RandomNoutOfNSampling,
    @SerializedName("UniformProbabilisticSampling")
    UniformProbabilisticSampling,
    @SerializedName("PropertyMatchFiltering")
    PropertyMatchFiltering,
    @SerializedName("HashBasedFiltering")
    HashBasedFiltering,
    @SerializedName("FlowStateDependentIntermediateFlowSelectionProcess")
    FlowStateDependentIntermediateFlowSelectionProcess;

    public static SamplingAlgorithm from(final Flow.SamplingAlgorithm samplingAlgorithm) {
        if (samplingAlgorithm == null) {
            return Unassigned;
        }

        switch (samplingAlgorithm) {
            case Unassigned:
                return Unassigned;
            case SystematicCountBasedSampling:
                return SystematicCountBasedSampling;
            case SystematicTimeBasedSampling:
                return SystematicTimeBasedSampling;
            case RandomNOutOfNSampling:
                return RandomNoutOfNSampling;
            case UniformProbabilisticSampling:
                return UniformProbabilisticSampling;
            case PropertyMatchFiltering:
                return PropertyMatchFiltering;
            case HashBasedFiltering:
                return HashBasedFiltering;
            case FlowStateDependentIntermediateFlowSelectionProcess:
                return FlowStateDependentIntermediateFlowSelectionProcess;
            default:
                throw new IllegalArgumentException("Unknown sampling algorithm: " + samplingAlgorithm.name());
        }
    }
}
