package it.phoops.geoserver.ols.routing.pgrouting;

import it.phoops.geoserver.ols.OLSAbstractServiceProvider;
import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.routing.Language;
import it.phoops.geoserver.ols.routing.RoutingServiceProvider;
import it.phoops.geoserver.ols.routing.pgrouting.component.PgRoutingTab;
import it.phoops.geoserver.ols.routing.pgrouting.component.PgRoutingTabFactory;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBElement;

import net.opengis.www.xls.AbstractLocationType;
import net.opengis.www.xls.DetermineRouteRequestType;
import net.opengis.www.xls.DetermineRouteResponseType;
import net.opengis.www.xls.DistanceType;
import net.opengis.www.xls.DistanceUnitType;
import net.opengis.www.xls.EnvelopeType;
import net.opengis.www.xls.LineStringType;
import net.opengis.www.xls.ObjectFactory;
import net.opengis.www.xls.PointType;
import net.opengis.www.xls.Pos;
import net.opengis.www.xls.PositionType;
import net.opengis.www.xls.RouteGeometryType;
import net.opengis.www.xls.RouteInstruction;
import net.opengis.www.xls.RouteInstructionsListType;
import net.opengis.www.xls.RoutePlan;
import net.opengis.www.xls.RoutePreferenceType;
import net.opengis.www.xls.RouteSummaryType;
import net.opengis.www.xls.WayPointList;
import net.opengis.www.xls.WayPointType;

import org.apache.log4j.Logger;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.config.ServiceInfo;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class PgRoutingServiceProvider extends OLSAbstractServiceProvider implements RoutingServiceProvider, Serializable{

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(PgRoutingServiceProvider.class);
    
    public static final double  DEGREES_TO_RADIANS_FACTOR = Math.PI / 180.0;
    public static final double  EARTH_RADIUS = 6378137.0;
    public static final double  DEGREES_TO_METERS_FACTOR = DEGREES_TO_RADIANS_FACTOR * EARTH_RADIUS;
    
    //Properties Name
    private static final String  PN_ENDPOINT_ADDRESS = "OLS.serviceProvider.geocoding.pgrouting.service.endpointAddress";
    private static final String  PN_PORT_NUMBER = "OLS.serviceProvider.geocoding.pgrouting.service.portNumber";
    private static final String  PN_DATABASE = "OLS.serviceProvider.geocoding.pgrouting.service.database";
    private static final String  PN_SCHEMA = "OLS.serviceProvider.geocoding.pgrouting.service.schema";
    private static final String  PN_USER = "OLS.serviceProvider.geocoding.pgrouting.service.user";
    private static final String  PN_PASSWORD = "OLS.serviceProvider.geocoding.pgrouting.service.password";
    private static final String  PN_ACTIVE_SERVICE = "OLS.serviceProvider.service.active";
    private static final String  PN_PGROUTING_ALGORITHM ="OLS.serviceProvider.geocoding.pgrouting.service.shortest.algorithm";
    private static final String  PN_PGROUTING_NODE_TABLE ="OLS.serviceProvider.geocoding.pgrouting.service.node.table";
    private static final String  PN_PGROUTING_EDGE_TABLE ="OLS.serviceProvider.geocoding.pgrouting.service.edge.table";
    private static final String  PN_PGROUTING_EDGE_QUERY ="OLS.serviceProvider.geocoding.pgrouting.service.edge.query";
    private static final String  PN_PGROUTING_UNDIRECTED_QUERY ="OLS.serviceProvider.geocoding.pgrouting.service.undirected.query";
    private static final String  PN_NAVIGATION_INFO     = "OLS.serviceProvider.geocoding.pgrouting.service.navigationInfo";
    private static final String  PN_NAVIGATION_S_INFO   = "OLS.serviceProvider.geocoding.pgrouting.service.navigationShortInfo";
    private static final String  PN_NAVIGATION_REL      = "OLS.serviceProvider.geocoding.pgrouting.service.navigationInfo.relative";
    private static final String  PN_LANGUAGE_INFO       = "OLS.serviceProvider.geocoding.pgrouting.service.languageInfo";
    
    private String      descriptionKey;
    private Properties  properties = new Properties();
    
    public String getEndpointAddress() {
        return properties.getProperty(PN_ENDPOINT_ADDRESS);
    }

    public void setEndpointAddress(String endpointAddress) {
        properties.setProperty(PN_ENDPOINT_ADDRESS, endpointAddress);
    }
    
    public String getPortNumber() {
        return properties.getProperty(PN_PORT_NUMBER);
    }

    public void setPortNumber(String portNumber) {
        properties.setProperty(PN_PORT_NUMBER, portNumber);
    }
    
    public String getDatabase() {
        return properties.getProperty(PN_DATABASE);
    }

    public void setDatabase(String database) {
        properties.setProperty(PN_DATABASE, database);
    }
    
    public String getSchema() {
        return properties.getProperty(PN_SCHEMA);
    }

    public void setSchema(String schema) {
        properties.setProperty(PN_SCHEMA, schema);
    }
    
    public String getUser() {
        return properties.getProperty(PN_USER);
    }

    public void setUser(String user) {
        properties.setProperty(PN_USER, user);
    }
    
    public String getPassword() {
        return properties.getProperty(PN_PASSWORD);
    }

    public void setPassword(String password) {
        properties.setProperty(PN_PASSWORD, password);
    }
    
    public String getAlgorithm() {
        return properties.getProperty(PN_PGROUTING_ALGORITHM);
    }

    public void setAlgorithm(String algorithm) {
        properties.setProperty(PN_PGROUTING_ALGORITHM, algorithm);
    }
    
    public String getNodeTable() {
        return properties.getProperty(PN_PGROUTING_NODE_TABLE);
    }

    public void setNodeTable(String nodeTable) {
        properties.setProperty(PN_PGROUTING_NODE_TABLE, nodeTable);
    }
    
    public String getEdgeTable() {
        return properties.getProperty(PN_PGROUTING_EDGE_TABLE);
    }

    public void setEdgeTable(String edgeTable) {
        properties.setProperty(PN_PGROUTING_EDGE_TABLE, edgeTable);
    }
    
    public String getEdgeQuery() {
        return properties.getProperty(PN_PGROUTING_EDGE_QUERY);
    }

    public void setEdgeQuery(String edgeQuery) {
        properties.setProperty(PN_PGROUTING_EDGE_QUERY, edgeQuery);
    }
    
    public String getUndirectedQuery() {
        return properties.getProperty(PN_PGROUTING_UNDIRECTED_QUERY);
    }

    public void setUndirectedQuery(String undirectedQuery) {
        properties.setProperty(PN_PGROUTING_UNDIRECTED_QUERY, undirectedQuery);
    }
    
    public String getNavigationInfo() {
        return properties.getProperty(PN_NAVIGATION_INFO);
    }

    public void setNavigationInfo(String navigationInfo) {
        properties.setProperty(PN_NAVIGATION_INFO, navigationInfo);
    }
    
    public String getNavigationInfoShort() {
        return properties.getProperty(PN_NAVIGATION_S_INFO);
    }
    
    public void setNavigationInfoShort(String navigationInfoShort){
        properties.setProperty(PN_NAVIGATION_S_INFO, navigationInfoShort);
    }
    
    public String getNavigationInfoRel(){
        return properties.getProperty(PN_NAVIGATION_REL);
    }
    
    public void setNavigationInfoRel(String navigationInfoRel){
        properties.setProperty(PN_NAVIGATION_REL, navigationInfoRel);
    }
    
    public String getLanguage(){
        return properties.getProperty(PN_LANGUAGE_INFO);
    }
    
    public void setLanguage(String language){
        properties.setProperty(PN_LANGUAGE_INFO, language);
    }
    
    public String getActive(){
        return properties.getProperty(PN_ACTIVE_SERVICE);
    }
    
    public void setActive(String activeService){
        properties.setProperty(PN_ACTIVE_SERVICE, activeService);
    }
    
    @Override
    public String getDescriptionKey() {
        return descriptionKey;
    }
    
    public void setDescriptionKey(String description) {
        this.descriptionKey = description;
    }
    
    @Override
    public Properties getProperties() {
        return properties;
    }
    
    @Override
    public OLSService getServiceType() {
        return OLSService.ROUTING_NAVIGATION;
    }
    @Override
    public ITab getTab() {
        IModel<String> title = new ResourceModel("pgRouting", "pgRouting");
        return PgRoutingTabFactory.getPgRoutingTabFactory().getPgRoutingTab(title);
    }
    @Override
    public void handleServiceChange(ServiceInfo service,
            List<String> propertyNames, List<Object> oldValues,
            List<Object> newValues) {
        String host = ((PgRoutingTab)getTab()).getHostPgRouting();
        if(host == null)
            host = "";
        String port = ((PgRoutingTab)getTab()).getPortPgRouting();
        if(port == null)
            port = "";
        String db = ((PgRoutingTab)getTab()).getDbPgRouting();
        if(db == null)
            db = "";
        String schema = ((PgRoutingTab)getTab()).getSchemaPgRouting();
        if(schema == null)
            schema = "";
        String user = ((PgRoutingTab)getTab()).getUserPgRouting();
        if(user == null)
            user = "";
        String psw = ((PgRoutingTab)getTab()).getPswPgRouting();
        if(psw == null)
            psw = "";
        String active = ((PgRoutingTab)getTab()).getActivePgRouting();
        
        String algorithm = "";
        if(((PgRoutingTab)getTab()).getSelectedAlgorithm() != null){
            algorithm = ((PgRoutingTab)getTab()).getSelectedAlgorithm().getCode();
        }
        
        String nodeTable = ((PgRoutingTab)getTab()).getNodeTableRouting();
        if(nodeTable == null)
            nodeTable = "";
        String edgeTable = ((PgRoutingTab)getTab()).getEdgeTableRouting();
        if(edgeTable == null)
            edgeTable = "";
        String edgeQuery = ((PgRoutingTab)getTab()).getEdgeQueryRouting();
        if(edgeQuery == null)
            edgeQuery = "";
        String undirectedQuery = ((PgRoutingTab)getTab()).getUndirectedQueryRouting();
        if(undirectedQuery == null)
            undirectedQuery = "";
        String navigation       = ((PgRoutingTab)getTab()).getNavigationInfo();
        String navigationS      = ((PgRoutingTab)getTab()).getNavigationInfoShort();
        String navigationR      = ((PgRoutingTab)getTab()).getNavigationInfoRel();
        String language = "";
        if(((PgRoutingTab)getTab()).getSelectedLanguage() != null){
            language = ((PgRoutingTab)getTab()).getSelectedLanguage().getCode();
        }
            
        setActive(active);
        setEndpointAddress(host);
        setPortNumber(port);
        setDatabase(db);
        setSchema(schema);
        setUser(user);
        setPassword(psw);
        setAlgorithm(algorithm);
        setNodeTable(nodeTable);
        setEdgeTable(edgeTable);
        setEdgeQuery(edgeQuery);
        setUndirectedQuery(undirectedQuery);
        setNavigationInfo(navigation);
        setNavigationInfoShort(navigationS);
        setNavigationInfoRel(navigationR);
        setLanguage(language);
    }
    
    @Override
    public boolean isServiceActive() {
        return Boolean.parseBoolean(this.getActive());
    }
    @Override
    public void setPropertiesTab(ITab pgRoutingTab) {
        ((PgRoutingTab)pgRoutingTab).setActivePgRouting(this.getActive());
        ((PgRoutingTab)pgRoutingTab).setHostPgRouting(this.getEndpointAddress());
        ((PgRoutingTab)pgRoutingTab).setPortPgRouting(this.getPortNumber());
        ((PgRoutingTab)pgRoutingTab).setDbPgRouting(this.getDatabase());
        ((PgRoutingTab)pgRoutingTab).setSchemaPgRouting(this.getSchema());
        ((PgRoutingTab)pgRoutingTab).setUserPgRouting(this.getUser());
        ((PgRoutingTab)pgRoutingTab).setPswPgRouting(this.getPassword());
        Algorithm algorithm = Algorithm.get(this.getAlgorithm());
        if(algorithm == null){
            ((PgRoutingTab)pgRoutingTab).setCodeAlgorithmSelected(1);
        }else{
            ((PgRoutingTab)pgRoutingTab).setCodeAlgorithmSelected(Integer.parseInt(algorithm.getCode()));
        }
        ((PgRoutingTab)pgRoutingTab).setNodeTableRouting(this.getNodeTable());
        ((PgRoutingTab)pgRoutingTab).setEdgeTableRouting(this.getEdgeTable());
        ((PgRoutingTab)pgRoutingTab).setEdgeQueryRouting(this.getEdgeQuery());
        ((PgRoutingTab)pgRoutingTab).setUndirectedQueryRouting(this.getUndirectedQuery());
        ((PgRoutingTab)pgRoutingTab).setNavigationInfo(this.getNavigationInfo());
        ((PgRoutingTab)pgRoutingTab).setNavigationInfoShort(this.getNavigationInfoShort());
        ((PgRoutingTab)pgRoutingTab).setNavigationInfoRel(this.getNavigationInfoRel());
        Language language = Language.get(this.getLanguage());
        if(language == null){
            ((PgRoutingTab)pgRoutingTab).setCodeLanguageSelected(1);
        }else{
            ((PgRoutingTab)pgRoutingTab).setCodeLanguageSelected(Integer.parseInt(language.getCode()));
        }
    }

    @Override
    public JAXBElement<DetermineRouteResponseType> route(
            DetermineRouteRequestType input) throws OLSException {
        JAXBElement<DetermineRouteResponseType> retval = null;
        
        // Parse request parameters
        RoutePlan routePlan = input.getRoutePlan();
        
        if (routePlan == null) {
            throw new OLSException("Route plan is missing");
        }
        
        WayPointList                                    wpList = routePlan.getWayPointList();
        WayPointType                                    startPoint = wpList.getStartPoint();
        JAXBElement<? extends AbstractLocationType>     startLocation = startPoint.getLocation();
        
        if (!(startLocation.getValue() instanceof PositionType)) {
            throw new OLSException("Unsupported start point location");
        }
        
        PositionType                                    startPosition = (PositionType)startLocation.getValue();
        WayPointType                                    endPoint = wpList.getEndPoint();
        JAXBElement<? extends AbstractLocationType>     endLocation = endPoint.getLocation();
        
        if (!(endLocation.getValue() instanceof PositionType)) {
            throw new OLSException("Unsupported end point location");
        }
        
        PositionType                                    endPosition = (PositionType)endLocation.getValue();
        List<WayPointType>                              viaPointsList = wpList.getViaPoints();
        List<PositionType>                              viaPosition = new ArrayList<PositionType>();
        JAXBElement<? extends AbstractLocationType>     viaLocation;
        
        if(viaPointsList.size() != 0){
            for (WayPointType viaPoint : viaPointsList) {
                viaLocation = viaPoint.getLocation();
                
                if (!(viaLocation.getValue() instanceof PositionType)) {
                    throw new OLSException("Unsupported via point location");
                }
                
                viaPosition.add((PositionType)viaLocation.getValue());
            }
        }
        
        viaPosition.add(endPosition);
        
        RoutePreferenceType     preference = routePlan.getRoutePreference();
        boolean                 directed;
        String                  query;
        
        switch (preference) {
        case PEDESTRIAN:
            directed = false;
            query = getUndirectedQuery();
            break;
        default:
            directed = true;
            query = getEdgeQuery();
            break;
        }

        Map<String, Object>     params = new HashMap<String, Object>();
        
        params.put("dbtype",    "postgis");
        params.put("host",      getEndpointAddress());
        params.put("port",      new Integer(getPortNumber()));
        params.put("database",  getDatabase());
        params.put("schema",    getSchema());
        params.put("user",      getUser());
        params.put("passwd",    getPassword());
        
        DataStore               pgDatastore = null;
        Connection              connection = null;
        CallableStatement       statement = null;
        ResultSet               rs = null;
        
        try {
            pgDatastore = DataStoreFinder.getDataStore(params); // org.geotools.jdbc.JDBCDataStore
            
            SimpleFeatureSource         nodes = pgDatastore.getFeatureSource(getNodeTable());
            ObjectFactory               of = new ObjectFactory();
            DetermineRouteResponseType  determineRouteResponse = of.createDetermineRouteResponseType();
            RouteSummaryType            routeSummary = of.createRouteSummaryType();
            RouteInstructionsListType   routeInstructions = of.createRouteInstructionsListType();
            double                      totalDistance = 0.0;
            DistanceType                distance;
            Geometry                    routeGeometry = null;
            
            do {
                endPosition = viaPosition.remove(0);
                
                Feature             startNode = findNearestNode(nodes, startPosition);
                Feature             endNode = findNearestNode(nodes, endPosition);
                String              startId = startNode.getIdentifier().getID();
                String              endId = endNode.getIdentifier().getID();
                
                connection = ((JDBCDataStore)pgDatastore).getConnection(Transaction.AUTO_COMMIT);
                // shortest_path -- SELECT id, source, target, cost FROM edge_table
                // shortest_path_astar -- SELECT id, source, target, cost, x1, y1, x2, y2 FROM edge_table
                if (Algorithm.DIJKSTRA.toString().equals(getAlgorithm())) {
                    statement = connection.prepareCall("{call shortest_path(?, ?, ?, ?, ?)}");
                } else {
                    statement = connection.prepareCall("{call shortest_path_astar(?, ?, ?, ?, ?)}");
                }
                
                statement.setString(1, query);                      // SQL
                statement.setInt(2, extractNodeId(nodes, startId)); // source id
                statement.setInt(3, extractNodeId(nodes, endId));   // target id
                statement.setBoolean(4, directed);                  // directed
                statement.setBoolean(5, directed);                  // has reverse cost
                
                rs = statement.executeQuery();
                
                SimpleFeatureSource         edges = pgDatastore.getFeatureSource(getEdgeTable());
                FilterFactory               ff = CommonFactoryFinder.getFilterFactory();
                Filter                      filter;
                SimpleFeatureCollection     sfc;
                SimpleFeature               edge;
                LineString                  edgeGeometry;
                Point                       edgeCentroid;
                RouteInstruction            routeInstruction;
                BigDecimal                  bdValue; 
                int                         vertexId;
                int                         edgeId;
                double                      cost;
                double                      length;
                double                      bearing;
                double                      startingBearing;
                double                      endingBearing = 0.0;
                Coordinate                  startCoordinate;
                Coordinate                  secondCoordinate;
                Coordinate                  preLastCoordinate;
                Coordinate                  endCoordinate;
                AbsoluteDirection           absoluteDirection;
                RelativeDirection           relativeDirection;
                String                      resultFormatter;
                
                String languageInfo = getLanguage();
                
                if(languageInfo.equals("1")){
                    Locale.setDefault(Locale.ITALIAN);
                } else if(languageInfo.equals("2")){
                    Locale.setDefault(Locale.ENGLISH);
                }
                
                Locale locale = Locale.getDefault();
                ResourceBundle messages = ResourceBundle.getBundle("GeoServerApplication", locale);
                    
                while (rs.next()) {
                    vertexId = rs.getInt(1);
                    edgeId = rs.getInt(2);
                    cost = rs.getDouble(3);
                    
                    if (edgeId != -1) {
                        filter = ff.id(Collections.singleton(ff.featureId(getEdgeTable() + "." + edgeId)));
                        sfc = edges.getFeatures(filter);
                        edge = (SimpleFeature)sfc.toArray()[0];
                        edgeGeometry = (LineString)edge.getDefaultGeometryProperty().getValue();
                        edgeCentroid = edgeGeometry.getCentroid();
                        length = edgeGeometry.getLength() * DEGREES_TO_METERS_FACTOR * Math.cos(edgeCentroid.getY() * DEGREES_TO_RADIANS_FACTOR);
                        
                        if (!Integer.valueOf(vertexId).equals(edge.getAttribute("source"))) {
                            // reverse edge direction
                            edgeGeometry = (LineString)edgeGeometry.reverse();
                        }
                        
                        startCoordinate = edgeGeometry.getStartPoint().getCoordinate();
                        endCoordinate = edgeGeometry.getEndPoint().getCoordinate();
                        bearing = Math.atan((endCoordinate.y - startCoordinate.y)/(endCoordinate.x - startCoordinate.x));
                        absoluteDirection = AbsoluteDirection.getDirectionFromBearing(bearing);
                        
                        secondCoordinate = edgeGeometry.getCoordinateN(1);
                        startingBearing = Math.atan((secondCoordinate.y - startCoordinate.y)/(secondCoordinate.x - startCoordinate.x));
                        
                        if (routeGeometry == null) {
                            routeGeometry = edgeGeometry;
                            relativeDirection = null;
                        } else {
                            routeGeometry = routeGeometry.union(edgeGeometry);
                            relativeDirection = RelativeDirection.getDirectionFromBearing(Math.PI / 2.0 - endingBearing + startingBearing);
                        }
                        
                        preLastCoordinate = edgeGeometry.getCoordinateN(edgeGeometry.getNumGeometries() - 1);
                        endingBearing = Math.atan((endCoordinate.y - preLastCoordinate.y)/(endCoordinate.x - preLastCoordinate.x));
                        
                        routeInstruction = of.createRouteInstruction();
                        
                        distance = of.createDistanceType();
                        bdValue = BigDecimal.valueOf(length * 0.001);
                        bdValue = bdValue.setScale(2, BigDecimal.ROUND_DOWN);
                        
                        distance.setValue(bdValue);
                        routeInstruction.setDistance(distance);
                        
                        totalDistance += length;
                        
                        resultFormatter = "";
                        
                        if (edge.getAttribute("name") != null && !edge.getAttribute("name").equals("")) {
                            if (relativeDirection != null) {
                                resultFormatter = "<IMG SRC='../resources/img/navigation/" + (relativeDirection.toString().toLowerCase()) + ".png' ALIGN='absmiddle'> " + MessageFormat.format(getNavigationInfoRel(), edge.getAttribute("name"), bdValue);
                            } else {
                                resultFormatter = MessageFormat.format(getNavigationInfo(), messages.getString(absoluteDirection.toString()), bdValue, edge.getAttribute("name"));
                            }
                        } else {
                            if (relativeDirection != null) {
                                resultFormatter = "<IMG SRC='../resources/img/navigation/" + (relativeDirection.toString().toLowerCase()) + ".png' ALIGN='absmiddle'> " + MessageFormat.format(getNavigationInfoRel(), edge.getAttribute("name"), bdValue);
                            } else {
                                resultFormatter = MessageFormat.format(getNavigationInfoShort(), messages.getString(absoluteDirection.toString()), bdValue);
                            }
                        }
                        
    //                    routeInstruction.setInstruction(relativeDirection + " - " + edge.getAttribute("name").toString() + " - " + length + " - " + absoluteDirection); // FIXME
                        
                        RouteGeometryType routeGeoInstruction = of.createRouteGeometryType();
//                        PointType pointType = of.createPointType();
                        LineStringType lineStringType = of.createLineStringType();
                        Pos posInstruction = new Pos();
                        List<Double> posValues;
                        List<Pos> posList;
                        posValues = posInstruction.getValues();
                        posList = lineStringType.getPos();
                        
                        posValues.add(preLastCoordinate.y);
                        posValues.add(preLastCoordinate.x);
                        posList.add(posInstruction);
                        posList.add(posInstruction);
                        
                        routeGeoInstruction.setLineString(lineStringType);
                        routeInstruction.setRouteInstructionGeometry(routeGeoInstruction);
                        routeInstruction.setInstruction(resultFormatter);
                        routeInstructions.getRouteInstructions().add(routeInstruction);
                    }
                }
                startPosition = endPosition;
            } while (viaPosition.size() > 0);
        

            distance = of.createDistanceType();
            distance.setValue(BigDecimal.valueOf(totalDistance));
            distance.setAccuracy(BigDecimal.ONE); // FIXME
            distance.setUom(DistanceUnitType.M); // FIXME ???
            routeSummary.setTotalDistance(distance);
            
            RouteGeometryType       routeGeometryXLS = of.createRouteGeometryType();
            LineStringType          lineString = of.createLineStringType();
            List<Pos>               posList = lineString.getPos();
            Pos                     pos;
            List<Double>            posValues;
            double                  minX = 360;
            double                  maxX = -360;
            double                  minY = 360;
            double                  maxY = -360;
            
            for (Coordinate coord : routeGeometry.getCoordinates()) {
                pos = new Pos();
                posValues = pos.getValues();
                posValues.add(coord.y);
                posValues.add(coord.x);
                posList.add(pos);
                
                // Bounding box extraction:
                if (coord.x < minX) {
                    minX = coord.x;
                }
                if (coord.y < minY) {
                    minY = coord.y;
                }
                if (coord.x > maxX) {
                    maxX = coord.x;
                }
                if (coord.y > maxY) {
                    maxY = coord.y;
                }
            }
            
            routeGeometryXLS.setLineString(lineString);
            
            EnvelopeType    boundingBox = of.createEnvelopeType();
            
            posList = boundingBox.getPos();
            
            pos = new Pos();
            posValues = pos.getValues();
            posValues.add(minY);
            posValues.add(minX);
            posList.add(pos);
            
            pos = new Pos();
            posValues = pos.getValues();
            posValues.add(maxY);
            posValues.add(maxX);
            posList.add(pos);
            
            routeSummary.setBoundingBox(boundingBox);
            
            determineRouteResponse.setRouteSummary(routeSummary);
            determineRouteResponse.setRouteGeometry(routeGeometryXLS);
            determineRouteResponse.setRouteInstructionsList(routeInstructions);
            
            retval = of.createDetermineRouteResponse(determineRouteResponse);
        } catch (IOException e) {
            throw new OLSException("Error accessing pgDatastore", e);
        } catch (SQLException e) {
            throw new OLSException("Error calling pgRouting", e);
        } finally {
            if (pgDatastore != null) {
                if (rs != null) {
                    ((JDBCDataStore)pgDatastore).closeSafe(rs);
                }
                
                if (statement != null) {
                    ((JDBCDataStore)pgDatastore).closeSafe(statement);
                }
                
                if (connection != null) {
                    ((JDBCDataStore)pgDatastore).closeSafe(connection);
                }
                
                pgDatastore.dispose();
            }
        }
        
        return retval;
    }
    
    private Feature findNearestNode(SimpleFeatureSource nodes, PositionType position) throws IOException
    {
        List<Double>                    ordinates = position.getPoint().getPos().getValues();
        Double                          y = ordinates.get(0);
        Double                          x = ordinates.get(1);
        GeometryFactory                 gf = new GeometryFactory();
        Geometry                        point = gf.createPoint(new Coordinate(x.doubleValue(), y.doubleValue()));
        double                          radius = 0.0000001; // degrees
        double                          distance = 1000.0;
        FilterFactory                   ff = CommonFactoryFinder.getFilterFactory();
        FeatureType                     schema = nodes.getSchema();
        GeometryDescriptor              gd = schema.getGeometryDescriptor();
        String                          geometryPropertyName = gd.getLocalName();
        CoordinateReferenceSystem       targetCRS = gd.getCoordinateReferenceSystem();
        Filter                          filter;
        SimpleFeatureCollection         sfc;
        
        do {
            filter = ff.bbox(geometryPropertyName, x - radius, y - radius, x + radius, y + radius, targetCRS.toString());
            sfc = nodes.getFeatures(filter);
            radius *= 2.0;
        } while (sfc.isEmpty() && radius < distance);
        
        FeatureIterator fi = sfc.features();
        Feature         node;
        Geometry        nodeGeometry;
        double          currentDistance;
        Feature         nearestNode = null;
        
        while (fi.hasNext()) {
            node = fi.next();
            
            nodeGeometry = (Geometry)node.getDefaultGeometryProperty().getValue();
            currentDistance = nodeGeometry.distance(point);
            
            if (currentDistance < distance) {
                distance = currentDistance;
                nearestNode = node;
            }
        }
        
        fi.close();
        
        return nearestNode;
    }
    
    private int extractNodeId(SimpleFeatureSource featureSource, String fid)
    {
        String  name = featureSource.getName().getLocalPart();
        
        return Integer.valueOf(fid.substring(name.length() + 1));
    }
}

enum AbsoluteDirection {
    NORTH,
    NORTHEAST,
    EAST,
    SOUTHEAST,
    SOUTH,
    SOUTHWEST,
    WEST,
    NORTHWEST;
    
    public static final double  EAST_NORTH_EAST = 22.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  NORTH_NORTH_EAST = 67.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  NORTH_NORTH_WEST = 112.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  WEST_NORTH_WEST = 157.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  WEST_SOUTH_WEST = 202.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  SOUTH_SOUTH_WEST = 247.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  SOUTH_SOUTH_EAST = 292.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  EAST_SOUTH_EAST = 337.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    
    public static AbsoluteDirection getDirectionFromBearing(double bearing)
    {
        AbsoluteDirection   retval;
        
        while (bearing < 0) {
            bearing += 2*Math.PI;
        }
        
        while (bearing > 2*Math.PI) {
            bearing -= 2*Math.PI;
        }
        
        if (bearing > EAST_SOUTH_EAST) {
            retval = AbsoluteDirection.EAST;
        } else if (bearing > SOUTH_SOUTH_EAST) {
            retval = AbsoluteDirection.SOUTHEAST;
        } else if (bearing > SOUTH_SOUTH_WEST) {
            retval = AbsoluteDirection.SOUTH;
        } else if (bearing > WEST_SOUTH_WEST) {
            retval = AbsoluteDirection.SOUTHWEST;
        } else if (bearing > WEST_NORTH_WEST) {
            retval = AbsoluteDirection.WEST;
        } else if (bearing > NORTH_NORTH_WEST) {
            retval = AbsoluteDirection.NORTHWEST;
        } else if (bearing > NORTH_NORTH_EAST) {
            retval = AbsoluteDirection.NORTH;
        } else if (bearing > EAST_NORTH_EAST) {
            retval = AbsoluteDirection.NORTHEAST;
        } else { // BETWEEN 0 AND EAST_NORTH_EAST
            retval = AbsoluteDirection.EAST;
        }
        
        return retval;
    }
}

enum RelativeDirection {
    CONTINUE,
    SLIGHTLY_RIGHT,
    RIGHT,
    HARD_RIGHT,
    UTURN,
    HARD_LEFT,
    LEFT,
    SLIGHTLY_LEFT;
    
    public static final double  EAST_NORTH_EAST = 22.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  NORTH_NORTH_EAST = 67.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  NORTH_NORTH_WEST = 112.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  WEST_NORTH_WEST = 157.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  WEST_SOUTH_WEST = 202.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  SOUTH_SOUTH_WEST = 247.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  SOUTH_SOUTH_EAST = 292.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    public static final double  EAST_SOUTH_EAST = 337.5 * PgRoutingServiceProvider.DEGREES_TO_RADIANS_FACTOR;
    
    public static RelativeDirection getDirectionFromBearing(double bearing)
    {
        RelativeDirection   retval;
        
        while (bearing < 0) {
            bearing += 2*Math.PI;
        }
        
        while (bearing > 2*Math.PI) {
            bearing -= 2*Math.PI;
        }
        
        if (bearing > EAST_SOUTH_EAST) {
            retval = RelativeDirection.RIGHT;
        } else if (bearing > SOUTH_SOUTH_EAST) {
            retval = RelativeDirection.HARD_RIGHT;
        } else if (bearing > SOUTH_SOUTH_WEST) {
            retval = RelativeDirection.UTURN;
        } else if (bearing > WEST_SOUTH_WEST) {
            retval = RelativeDirection.HARD_LEFT;
        } else if (bearing > WEST_NORTH_WEST) {
            retval = RelativeDirection.LEFT;
        } else if (bearing > NORTH_NORTH_WEST) {
            retval = RelativeDirection.SLIGHTLY_LEFT;
        } else if (bearing > NORTH_NORTH_EAST) {
            retval = RelativeDirection.CONTINUE;
        } else if (bearing > EAST_NORTH_EAST) {
            retval = RelativeDirection.SLIGHTLY_RIGHT;
        } else { // BETWEEN 0 AND EAST_NORTH_EAST
            retval = RelativeDirection.RIGHT;
        }
        
        return retval;
    }
}
