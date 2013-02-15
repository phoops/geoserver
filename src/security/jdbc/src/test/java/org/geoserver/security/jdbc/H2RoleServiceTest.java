/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

import org.geoserver.security.GeoServerRoleService;

public class H2RoleServiceTest extends JDBCRoleServiceTest {

    
    @Override
    protected String getFixtureId() {
        return "h2";
    }
        
    @Override
    public GeoServerRoleService createRoleService(String serviceName) throws Exception {
        return JDBCTestSupport.createH2RoleService(getFixtureId(), getSecurityManager());
    }

}
