/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.script.groovy;

import org.geoserver.script.wfs.WfsTxHookTest;

public class GroovyTxHookTest extends WfsTxHookTest {

    @Override
    public String getExtension() {
        return "groovy";
    }

}
