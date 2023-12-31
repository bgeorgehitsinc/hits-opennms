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

package org.opennms.features.kafka.consumer.events;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.event.Event;

public class EventsMapperTest {

    @Test
    public void testEventWithoutRequiredFields() {

        List<EventsProto.Event> protobufEvents = new ArrayList<>();
        // Add an event without source and empty uei
        EventsProto.Event.Builder builder = EventsProto.Event.newBuilder();
        builder.setUei("");
        builder.setSource("");
        builder.setDescription("Event without uei/source");
        protobufEvents.add(builder.build());
        builder = EventsProto.Event.newBuilder();
        builder.setUei(EventConstants.NODE_ADDED_EVENT_UEI);
        builder.setSource("kafka-test");
        builder.setIpAddress("192.168.1.2");
        builder.setDescription("Kafka Consumer description");
        protobufEvents.add(builder.build());
        List<Event> events = EventsMapper.mapProtobufToEvents(protobufEvents);
        Assert.assertThat(events.size(), Matchers.is(1));
    }
}
