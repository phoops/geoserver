package org.geoserver.logging;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.geoserver.config.GeoServer;
import org.geoserver.config.impl.GeoServerImpl;
import org.geotools.data.DataUtilities;
import org.junit.Before;
import org.junit.Test;

public class LegacyLoggingImporterTest {

    GeoServer gs;
    LegacyLoggingImporter importer;
    
    @Before
    public void setUp() throws Exception {
        gs = new GeoServerImpl();
        
        importer = new LegacyLoggingImporter();
        importer.imprt( 
        		DataUtilities.urlToFile(getClass().getResource("services.xml")).getParentFile());
    }
    
    @Test
    public void test() throws Exception {
        assertEquals( "DEFAULT_LOGGING.properties", importer.getConfigFileName() );
        assertFalse( importer.getSuppressStdOutLogging() );
        assertEquals( "logs/geoserver.log", importer.getLogFile());
    }
}
