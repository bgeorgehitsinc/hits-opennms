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

package org.opennms.netmgt.alarmd;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmEntityListener;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsReductionKeyMemo;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

public class AlarmLifecycleListenerManager implements AlarmEntityListener, InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmLifecycleListenerManager.class);

    public static final String ALARM_SNAPSHOT_INTERVAL_MS_SYS_PROP = "org.opennms.alarms.snapshot.sync.ms";
    public static final long ALARM_SNAPSHOT_INTERVAL_MS = SystemProperties.getLong(ALARM_SNAPSHOT_INTERVAL_MS_SYS_PROP, TimeUnit.MINUTES.toMillis(2));

    private final Set<AlarmLifecycleListener> listeners = Sets.newConcurrentHashSet();
    private Timer timer;

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private SessionUtils sessionUtils;

    private void start() {
        timer = new Timer("AlarmLifecycleListenerManager");
        // Use a fixed delay instead of a fixed interval so that snapshots are not constantly in progress
        // if they take a long time
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    doSnapshot();
                } catch (Exception e) {
                    LOG.error("Error while performing snapshot update.", e);
                }
            }
        }, 0, ALARM_SNAPSHOT_INTERVAL_MS);
    }

    private void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    protected void doSnapshot() {
        if (listeners.size() < 1) {
            return;
        }

        final AtomicLong numAlarms = new AtomicLong(-1);
        final long systemMillisBeforeSnapshot = System.currentTimeMillis();
        final AtomicLong systemMillisAfterLoad = new AtomicLong(-1);
        try {
            forEachListener(AlarmLifecycleListener::preHandleAlarmSnapshot);
            sessionUtils.withTransaction(() -> {
               // Load all of the alarms
               final List<OnmsAlarm> allAlarms = alarmDao.findAll();
               numAlarms.set(allAlarms.size());
               // Save the timestamp after the load, so we can differentiate between how long it took
               // to load the alarms and how long it took to invoke the callbacks
               systemMillisAfterLoad.set(System.currentTimeMillis());
               forEachListener(l -> {
                   LOG.debug("Calling handleAlarmSnapshot on listener: {}", l);
                   l.handleAlarmSnapshot(allAlarms);
                   LOG.debug("Done calling listener.");
               });
               return null;
            });
        } finally {
            if (LOG.isDebugEnabled()) {
                final long now = System.currentTimeMillis();
                LOG.debug("Alarm snapshot for {} alarms completed. Spent {}ms loading the alarms. " +
                                "Snapshot processing took a total of of {}ms.",
                        numAlarms.get(),
                        systemMillisAfterLoad.get() - systemMillisBeforeSnapshot,
                        now - systemMillisBeforeSnapshot);
            }
            forEachListener(AlarmLifecycleListener::postHandleAlarmSnapshot);
        }
    }

    public void onNewOrUpdatedAlarm(OnmsAlarm alarm) {
        forEachListener(l -> l.handleNewOrUpdatedAlarm(alarm));
    }

    @Override
    public void onAlarmArchived(OnmsAlarm alarm, String previousReductionKey) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onAlarmDeleted(OnmsAlarm alarm) {
        forEachListener(l -> l.handleDeletedAlarm(alarm.getId(), alarm.getReductionKey()));
    }

    @Override
    public void onAlarmCreated(OnmsAlarm alarm) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onAlarmUpdatedWithReducedEvent(OnmsAlarm alarm) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onAlarmAcknowledged(OnmsAlarm alarm, String previousAckUser, Date previousAckTime) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onAlarmUnacknowledged(OnmsAlarm alarm, String previousAckUser, Date previousAckTime) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onAlarmSeverityUpdated(OnmsAlarm alarm, OnmsSeverity previousSeverity) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onStickyMemoUpdated(OnmsAlarm alarm, String previousBody, String previousAuthor, Date previousUpdated) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onReductionKeyMemoUpdated(OnmsAlarm alarm, String previousBody, String previousAuthor, Date previousUpdated) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onStickyMemoDeleted(OnmsAlarm alarm, OnmsMemo memo) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onReductionKeyMemoDeleted(OnmsAlarm alarm, OnmsReductionKeyMemo memo) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onLastAutomationTimeUpdated(OnmsAlarm alarm, Date previousLastAutomationTime) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onRelatedAlarmsUpdated(OnmsAlarm alarm, Set<OnmsAlarm> previousRelatedAlarms) {
        onNewOrUpdatedAlarm(alarm);
    }

    @Override
    public void onTicketStateChanged(OnmsAlarm alarm, TroubleTicketState previousState) {
        onNewOrUpdatedAlarm(alarm);
    }

    private void forEachListener(Consumer<AlarmLifecycleListener> callback) {
        for (AlarmLifecycleListener listener : listeners) {
            try {
                callback.accept(listener);
            } catch (Exception e) {
                LOG.error("Error occurred while invoking listener: {}. Skipping.", listener, e);
            }
        }
    }

    public void onListenerRegistered(final AlarmLifecycleListener listener, final Map<String,String> properties) {
        LOG.debug("onListenerRegistered: {} with properties: {}", listener, properties);
        listeners.add(listener);
    }

    public void onListenerUnregistered(final AlarmLifecycleListener listener, final Map<String,String> properties) {
        LOG.debug("onListenerUnregistered: {} with properties: {}", listener, properties);
        listeners.remove(listener);
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    public void setSessionUtils(SessionUtils sessionUtils) {
        this.sessionUtils = sessionUtils;
    }

    @Override
    public void afterPropertiesSet() {
        start();
    }

    @Override
    public void destroy() {
        stop();
    }

}
