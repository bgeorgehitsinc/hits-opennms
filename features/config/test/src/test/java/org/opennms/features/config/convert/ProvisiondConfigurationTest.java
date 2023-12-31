/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.config.convert;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;

public class ProvisiondConfigurationTest extends CmConfigTest<ProvisiondConfiguration> {

    public ProvisiondConfigurationTest(ProvisiondConfiguration sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, "provisiond-configuration.xsd", "provisiond-configuration");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<provisiond-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/provisiond-configuration\" importThreads=\"99\">\n" + 
                "   <requisition-def import-url-resource=\"dns://localhost/localhost\" import-name=\"localhost\" rescan-existing=\"true\">\n" + 
                "      <cron-schedule>0 0 0 * * ? *</cron-schedule>\n" + 
                "   </requisition-def>\n" + 
                "</provisiond-configuration>"
            },
            {
                new ProvisiondConfiguration(),
                "<provisiond-configuration/>"
            }
        });
    }

    private static ProvisiondConfiguration getConfig() {
        ProvisiondConfiguration config = new ProvisiondConfiguration();
        config.setImportThreads(99L);
        
        RequisitionDef def = new RequisitionDef();
        def.setImportName("localhost");
        def.setImportUrlResource("dns://localhost/localhost");
        def.setCronSchedule("0 0 0 * * ? *");
        def.setRescanExisting("true");
        config.addRequisitionDef(def);

        return config;
    }
}
