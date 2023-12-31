/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2023 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * <p>RancidAdapterConfigFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class RancidAdapterConfigFactory extends RancidAdapterConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(RancidAdapterConfigFactory.class);

    /**
     * The singleton instance of this factory
     */
    private static RancidAdapterConfigFactory m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Loaded version
     */
    private long m_currentVersion = -1L;


    /**
     * constructor constructor
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @param currentVersion a long.
     * @param reader a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public RancidAdapterConfigFactory(long currentVersion, InputStream reader) throws IOException {
        super(reader);
        m_currentVersion = currentVersion;
    }


    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws IOException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.RANCID_CONFIG_FILE_NAME);

            LOG.debug("init: config file path: {}", cfgFile.getPath());

            try (final InputStream reader = new FileInputStream(cfgFile)) {
                RancidAdapterConfigFactory config = new RancidAdapterConfigFactory(cfgFile.lastModified(), reader);
                setInstance(config);
            }
        } catch (final FileNotFoundException e) {
            LOG.warn("unable to locate {}, falling back to default", ConfigFileConstants.getFileName(ConfigFileConstants.RANCID_CONFIG_FILE_NAME));
            final ClassPathResource resource = new ClassPathResource("/" + ConfigFileConstants.getFileName(ConfigFileConstants.RANCID_CONFIG_FILE_NAME));
            try (final InputStream stream = resource.getInputStream()) {
                RancidAdapterConfigFactory config = new RancidAdapterConfigFactory(0, stream);
                setInstance(config);
            }
        }
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws IOException {
        init();
        getInstance().update();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized RancidAdapterConfigFactory getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }
        return m_singleton;
    }

    private static synchronized void setInstance(final RancidAdapterConfigFactory instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    /**
     * <p>saveXml</p>
     *
     * @param xml a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected void saveXml(final String xml) throws IOException {
        if (xml != null) {
            getWriteLock().lock();
            try {
                long timestamp = System.currentTimeMillis();
                File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.RANCID_CONFIG_FILE_NAME);
                LOG.debug("saveXml: saving config file at {}: {}", timestamp, cfgFile.getPath());
                Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), StandardCharsets.UTF_8);
                fileWriter.write(xml);
                fileWriter.flush();
                fileWriter.close();
                LOG.debug("saveXml: finished saving config file: {}", cfgFile.getPath());
            } finally {
                getWriteLock().unlock();
            }
        }
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    public void update() throws IOException {
        getWriteLock().lock();
        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.RANCID_CONFIG_FILE_NAME);
            if (cfgFile.lastModified() > m_currentVersion) {
                m_currentVersion = cfgFile.lastModified();
                LOG.debug("init: config file path: {}", cfgFile.getPath());
                reloadXML(new FileInputStream(cfgFile));
                LOG.debug("init: finished loading config file: {}", cfgFile.getPath());
            }
        } catch (final FileNotFoundException e) {
            LOG.warn("unable to locate {}, falling back to default", ConfigFileConstants.getFileName(ConfigFileConstants.RANCID_CONFIG_FILE_NAME));
            final ClassPathResource resource = new ClassPathResource("/" + ConfigFileConstants.getFileName(ConfigFileConstants.RANCID_CONFIG_FILE_NAME));
            try (final InputStream stream = resource.getInputStream()) {
                reloadXML(stream);
            }
        } finally {
            getWriteLock().unlock();
        }
    }

}
