/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.web.layer;

import org.geoserver.web.GeoServerWicketTestSupport;
import org.junit.Test;

public class CachedLayersPageTest extends GeoServerWicketTestSupport {
    
    @Test
    public void testPageLoad() {
        CachedLayersPage page = new CachedLayersPage();

        tester.startPage(page);
        tester.assertRenderedPage(CachedLayersPage.class);

        print(page, true, true);
    }
}
