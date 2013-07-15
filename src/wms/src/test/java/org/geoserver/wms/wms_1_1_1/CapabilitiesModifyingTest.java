/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wms.wms_1_1_1;

import static org.junit.Assert.*;

import org.custommonkey.xmlunit.XMLAssert;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.ResourceErrorHandling;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.mockrunner.mock.web.MockHttpServletResponse;

public class CapabilitiesModifyingTest extends GeoServerSystemTestSupport {
    
    @Before
    public void resetWmsConfigChanges() {
        GeoServerInfo global = getGeoServer().getGlobal();
        global.setResourceErrorHandling(null);
        getGeoServer().save(global);
    }
    
    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);
        
        for (FeatureTypeInfo ft : getCatalog().getFeatureTypes()) {
            ft.setLatLonBoundingBox(null);
            getCatalog().save(ft);
        }
    }
    
    @Test
    public void testMisconfiguredLayerGeneratesErrorDocumentInDefaultConfig() throws Exception {
        MockHttpServletResponse response = getAsServletResponse(
                "wms?service=WMS&request=GetCapabilities&version=1.1.1");
        assertTrue("Response does not contain ServiceExceptionReport: " + response.getOutputStreamContent(),
                response.getOutputStreamContent().endsWith("</ServiceExceptionReport>"));
    }
    
    @Test
    public void testMisconfiguredLayerIsSkippedWhenWMSServiceIsConfiguredThatWay() throws Exception {
        GeoServerInfo global = getGeoServer().getGlobal();
        global.setResourceErrorHandling(ResourceErrorHandling.SKIP_MISCONFIGURED_LAYERS);
        getGeoServer().save(global);
        
        Document caps = getAsDOM(
                "wms?service=WMS&request=GetCapabilities&version=1.1.1");
        
        assertEquals("WMT_MS_Capabilities", caps.getDocumentElement().getTagName());
        // we misconfigured all the layers in the server, so there should be no named layers now.
        XMLAssert.assertXpathEvaluatesTo("", "//Layer/Name/text()", caps);
    }
    
    @Test
    public void testMisconfiguredLayerGeneratesErrorDocumentInDefaultConfig_1_3_0() throws Exception {
        MockHttpServletResponse response = getAsServletResponse(
                "wms?service=WMS&request=GetCapabilities&version=1.3.0");
        assertTrue("Response does not contain ServiceExceptionReport: " + response.getOutputStreamContent(),
                response.getOutputStreamContent().endsWith("</ServiceExceptionReport>"));
    }
    
    @Test
    public void testMisconfiguredLayerIsSkippedWhenWMSServiceIsConfiguredThatWay_1_3_0() throws Exception {
        GeoServerInfo global = getGeoServer().getGlobal();
        global.setResourceErrorHandling(ResourceErrorHandling.SKIP_MISCONFIGURED_LAYERS);
        getGeoServer().save(global);
        
        Document caps = getAsDOM(
                "wms?service=WMS&request=GetCapabilities&version=1.3.0");
        
        assertEquals("WMS_Capabilities", caps.getDocumentElement().getTagName());
        // we misconfigured all the layers in the server, so there should be no named layers now.
        XMLAssert.assertXpathEvaluatesTo("", "//Layer/Name/text()", caps);
    }
}
