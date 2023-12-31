/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.ThrowableAnticipator;

/**
 * Test case for BroadcastEventProcessor.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class BroadcastEventProcessorTest {
    private EventConfDao m_eventConfDao = mock(EventConfDao.class);

    @Test
    public void testInstantiateWithNullEventIpcManager() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("argument eventIpcManager must not be null"));
        
        try {
            new BroadcastEventProcessor(null, m_eventConfDao);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }

    @Test
    public void testInstantiateWithNullEventConfDao() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("argument eventConfDao must not be null"));
        
        try {
            new BroadcastEventProcessor(new MockEventIpcManager(), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }

    @Test
    public void testInstantiateAndClose() {
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        BroadcastEventProcessor processor = new BroadcastEventProcessor(eventIpcManager, m_eventConfDao);
        processor.close();
    }

    @Test
    public void testReload() {
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        BroadcastEventProcessor processor = new BroadcastEventProcessor(eventIpcManager, m_eventConfDao);
        
        EventBuilder eventBuilder = new EventBuilder(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI, "dunno");
        
        // Expect a call to reload the EventConfDao
        m_eventConfDao.reload();

        processor.onEvent(ImmutableMapper.fromMutableEvent(eventBuilder.getEvent()));
    }

    @Test
    public void testReloadDaemonConfig() {
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        BroadcastEventProcessor processor = new BroadcastEventProcessor(eventIpcManager, m_eventConfDao);

        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "dunno");
        eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, "Eventd");

        // Expect a call to reload the EventConfDao
        m_eventConfDao.reload();

        processor.onEvent(ImmutableMapper.fromMutableEvent(eventBuilder.getEvent()));
    }

}
