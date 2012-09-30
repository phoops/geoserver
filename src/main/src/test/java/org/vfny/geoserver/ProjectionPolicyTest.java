package org.vfny.geoserver;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.ResourcePool;
import org.geoserver.data.test.MockData;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.data.test.SystemTestData.LayerProperty;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.feature.Feature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class ProjectionPolicyTest extends GeoServerSystemTestSupport {

    static WKTReader WKT = new WKTReader();
    
    
    @Override
    protected void setUpTestData(SystemTestData testData) throws Exception {
        Map<LayerProperty, Object> props = new HashMap<LayerProperty, Object>();
        props.put(LayerProperty.PROJECTION_POLICY, ProjectionPolicy.FORCE_DECLARED);
        props.put(LayerProperty.SRS, 4269);
        testData.setUpVectorLayer(SystemTestData.BASIC_POLYGONS, props);
        
        props.put(LayerProperty.PROJECTION_POLICY, ProjectionPolicy.REPROJECT_TO_DECLARED);
        props.put(LayerProperty.SRS, 4326);
        testData.setUpVectorLayer(MockData.POLYGONS, props);
        
        props.put(LayerProperty.PROJECTION_POLICY, ProjectionPolicy.NONE);
        props.put(LayerProperty.SRS, 3004);
        testData.setUpVectorLayer(MockData.LINES, props);
        
        props.put(LayerProperty.PROJECTION_POLICY, ProjectionPolicy.REPROJECT_TO_DECLARED);
        props.put(LayerProperty.SRS, 4326);
        props.put(LayerProperty.NAME, "MyPoints");
        testData.setUpVectorLayer(MockData.POINTS, props);
        
        testData.setUpDefaultRasterLayers();
        testData.setUpWcs10RasterLayers();
    }
    
    @Test
    public void testForce() throws Exception {
        FeatureTypeInfo fti = getCatalog().getFeatureTypeByName(MockData.BASIC_POLYGONS.getLocalPart());
        assertEquals("EPSG:4269", fti.getSRS());
        assertEquals(ProjectionPolicy.FORCE_DECLARED, fti.getProjectionPolicy());
        FeatureCollection fc = fti.getFeatureSource(null, null).getFeatures();
        assertEquals(CRS.decode("EPSG:4269"), fc.getSchema().getCoordinateReferenceSystem());
        FeatureIterator fi = fc.features();
        Feature f = fi.next();
        fi.close();
        assertEquals(CRS.decode("EPSG:4269"), f.getType().getCoordinateReferenceSystem());
    }
    
    @Test
    public void testReproject() throws Exception {
        FeatureTypeInfo fti = getCatalog().getFeatureTypeByName(MockData.POLYGONS.getLocalPart());
        assertEquals("EPSG:4326", fti.getSRS());
        assertEquals(ProjectionPolicy.REPROJECT_TO_DECLARED, fti.getProjectionPolicy());
        FeatureCollection fc = fti.getFeatureSource(null, null).getFeatures();
        assertEquals(CRS.decode("EPSG:4326"), fc.getSchema().getCoordinateReferenceSystem());
        FeatureIterator fi = fc.features();
        Feature f = fi.next();
        
        //test that geometry was actually reprojected
        Geometry g = (Geometry) f.getDefaultGeometryProperty().getValue();
        assertFalse(g.equalsExact(WKT.read(
                "POLYGON((500225 500025,500225 500075,500275 500050,500275 500025,500225 500025))")));
        fi.close();
        assertEquals(CRS.decode("EPSG:4326"), f.getType().getCoordinateReferenceSystem());
    }
    
    @Test
    public void testLeaveNative() throws Exception {
        FeatureTypeInfo fti = getCatalog().getFeatureTypeByName(MockData.LINES.getLocalPart());
        assertEquals("EPSG:3004", fti.getSRS());
        assertEquals(ProjectionPolicy.NONE, fti.getProjectionPolicy());
        FeatureCollection fc = fti.getFeatureSource(null, null).getFeatures();
        assertEquals(CRS.decode("EPSG:32615"), fc.getSchema().getCoordinateReferenceSystem());
        FeatureIterator fi = fc.features();
        Feature f = fi.next();
        
        //test that the geometry was left in tact
        Geometry g = (Geometry) f.getDefaultGeometryProperty().getValue();
        assertTrue(g.equalsExact(WKT.read("LINESTRING(500125 500025,500175 500075)")));
        
        fi.close();
        assertEquals(CRS.decode("EPSG:32615"), f.getType().getCoordinateReferenceSystem());
    }
    
    @Test
    public void testWithRename() throws Exception {
        FeatureTypeInfo fti = getCatalog().getFeatureTypeByName("MyPoints");
        assertEquals("EPSG:4326", fti.getSRS());
        assertEquals(ProjectionPolicy.REPROJECT_TO_DECLARED, fti.getProjectionPolicy());
        FeatureCollection fc = fti.getFeatureSource(null, null).getFeatures();
        assertEquals(CRS.decode("EPSG:4326"), fc.getSchema().getCoordinateReferenceSystem());
        FeatureIterator fi = fc.features();
        Feature f = fi.next();
        
        //test that geometry was reprojected
        Geometry g = (Geometry) f.getDefaultGeometryProperty().getValue();
        assertFalse(g.equalsExact(WKT.read("POINT(500050 500050)")));
        fi.close();
        assertEquals(CRS.decode("EPSG:4326"), f.getType().getCoordinateReferenceSystem());
    }
    
    @Test
    public void testForceCoverage() throws Exception {
        // force the data to another projection
        Catalog catalog = getCatalog();
        CoverageInfo ci = catalog.getCoverageByName("usa");
        ci.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
        ci.setSRS("EPSG:3857");
        catalog.save(ci);
        
        ci = catalog.getCoverageByName("usa");
        assertEquals(ProjectionPolicy.FORCE_DECLARED, ci.getProjectionPolicy());
        assertEquals("EPSG:3857", ci.getSRS());
        
        // now get the reader via the coverage info
        AbstractGridCoverage2DReader r;
        r = (AbstractGridCoverage2DReader) ci.getGridCoverageReader(null, GeoTools.getDefaultHints());
        assertTrue(CRS.equalsIgnoreMetadata(CRS.decode("EPSG:3857"), r.getCrs()));
        
        // and again without any hint
        r = (AbstractGridCoverage2DReader) ci.getGridCoverageReader(null, null);
        assertTrue(CRS.equalsIgnoreMetadata(CRS.decode("EPSG:3857"), r.getCrs()));
        
        // get the reader straight: we should get back the native projection
        CoverageStoreInfo store = catalog.getCoverageStoreByName("usa");
        final ResourcePool rpool = catalog.getResourcePool();
        r = (AbstractGridCoverage2DReader) rpool.getGridCoverageReader(store, GeoTools.getDefaultHints());
        assertTrue(CRS.equalsIgnoreMetadata(CRS.decode("EPSG:4326"), r.getCrs()));

   }
}