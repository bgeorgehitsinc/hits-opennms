/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.topology;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.IconRepository;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;

import com.google.common.collect.Sets;

public class LegacyIconRepositoryAdapter implements IconRepository {

    private final GraphProvider delegate;

    public LegacyIconRepositoryAdapter(final GraphProvider delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public boolean contains(String iconKey) {
        return getUsedIconIds().contains(iconKey);
    }

    @Override
    public String getSVGIconId(String iconKey) {
        return iconKey;
    }

    private Set<String> getUsedIconIds() {
        if (delegate.getCurrentGraph() != null) {
            return delegate.getCurrentGraph().getVertices().stream()
                    .map(Vertex::getIconKey)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return Sets.newHashSet();
    }
}
