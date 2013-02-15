/* 
 * Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.test;

/**
 * Mock data for testing 3D BBOXes in app-schema
 *
 * 
 * @author Niels Charlier
 */
public class BBox3DMockData extends AbstractAppSchemaMockData {

    /**
     * @see org.geoserver.test.AbstractAppSchemaMockData#addContent()
     */
    @Override
    public void addContent() {
        add3DFeatureType(GSML_PREFIX, "MappedFeature", "MappedFeature3D.xml",
                "MappedFeature3D.properties", "ObservationMethod.xml", "ObservationMethod.properties");
    }
}
