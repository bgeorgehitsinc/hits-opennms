/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
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

package org.opennms.features.scv.dominion.grpc;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.opennms.dominion.local.rpc.grpc.DominionGrpc;
import org.opennms.features.minion.dominion.grpc.DominionScvGrpcClient;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

public class DominionSecureCredentialsVault implements SecureCredentialsVault {

    private final DominionScvGrpcClient client;

    public DominionSecureCredentialsVault(DominionScvGrpcClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public Set<String> getAliases() {
        return new HashSet<>(client.getAliases().getAliasesList());
    }

    @Override
    public Credentials getCredentials(String alias) {
        Objects.requireNonNull(alias);
        DominionGrpc.ScvGetCredentialsResponse response = client.getCredentials(alias);

        return new Credentials(response.getUser(), response.getPassword(), response.getAttributesMap());
    }

    @Override
    public void setCredentials(String alias, Credentials credentials) {
        Objects.requireNonNull(alias);
        Objects.requireNonNull(credentials);
        
        client.setCredentials(alias, credentials.getUsername(), credentials.getPassword(),
                credentials.getAttributes());
    }

    @Override
    public void deleteCredentials(final String alias) {
        Objects.requireNonNull(alias);
        throw new IllegalStateException("Not implemented yet");
    }
}
