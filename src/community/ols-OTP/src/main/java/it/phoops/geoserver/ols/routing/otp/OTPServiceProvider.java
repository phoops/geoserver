package it.phoops.geoserver.ols.routing.otp;

import it.phoops.geoserver.ols.OLSAbstractServiceProvider;
import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.routing.RoutingServiceProvider;
import it.phoops.geoserver.ols.routing.otp.client.ns0.EncodedPolylineBean;
import it.phoops.geoserver.ols.routing.otp.client.ns0.Itinerary;
import it.phoops.geoserver.ols.routing.otp.client.ns0.Leg;
import it.phoops.geoserver.ols.routing.otp.client.ns0.Leg.Steps;
import it.phoops.geoserver.ols.routing.otp.client.ns0.PlannerError;
import it.phoops.geoserver.ols.routing.otp.client.ns0.Response;
import it.phoops.geoserver.ols.routing.otp.client.ns0.TripPlan;
import it.phoops.geoserver.ols.routing.otp.client.ns0.TripPlan.Itineraries;
import it.phoops.geoserver.ols.routing.otp.client.ns0.WalkStep;
import it.phoops.geoserver.ols.routing.otp.component.OTPTab;
import it.phoops.geoserver.ols.routing.otp.component.OTPTabFactory;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.opengis.www.xls.AbstractLocationType;
import net.opengis.www.xls.DetermineRouteRequestType;
import net.opengis.www.xls.DetermineRouteResponseType;
import net.opengis.www.xls.DistanceType;
import net.opengis.www.xls.DistanceUnitType;
import net.opengis.www.xls.EnvelopeType;
import net.opengis.www.xls.LineStringType;
import net.opengis.www.xls.ObjectFactory;
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

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.config.ServiceInfo;
import org.opentripplanner.util.PolylineEncoder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class OTPServiceProvider extends OLSAbstractServiceProvider implements RoutingServiceProvider {
    //Properties Name
    private static final String  PN_ENDPOINT_ADDRESS = "OLS.serviceProvider.geocoding.otp.service.endpointAddress";
    private static final String  PN_PORT_NUMBER = "OLS.serviceProvider.geocoding.otp.service.portNumber";
    private static final String  PN_ACTIVE_SERVICE = "OLS.serviceProvider.service.active";
    
    private String      descriptionKey;
    private Properties  properties = new Properties();
    
    @Override
    public String getDescriptionKey() {
        return descriptionKey;
    }
    
    public void setDescriptionKey(String description) {
        this.descriptionKey = description;
    }
    
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
    
    public String getActive(){
        return properties.getProperty(PN_ACTIVE_SERVICE);
    }
    
    public void setActive(String activeService){
        properties.setProperty(PN_ACTIVE_SERVICE, activeService);
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
        IModel<String> title = new ResourceModel("OTP ", "OTP");
        return OTPTabFactory.getOTPTabFactory().getOTPTab(title);
    }

    @Override
    public void handleServiceChange(ServiceInfo service,
            List<String> propertyNames, List<Object> oldValues,
            List<Object> newValues) {
        String url = ((OTPTab)getTab()).getUrlOTP();
        String port = ((OTPTab)getTab()).getPortOTP();
        String active = ((OTPTab)getTab()).getActiveOTP();
        
        setEndpointAddress(url);
        setPortNumber(port);
        setActive(active);
        
    }

    @Override
    public boolean isServiceActive() {
        return Boolean.parseBoolean(this.getActive());
    }

    @Override
    public void setPropertiesTab(ITab otpTab) {
        ((OTPTab)otpTab).setUrlOTP(this.getEndpointAddress());
        ((OTPTab)otpTab).setActiveOTP(this.getActive());
        ((OTPTab)otpTab).setPortOTP(this.getPortNumber());
    }

    @Override
    public JAXBElement<DetermineRouteResponseType> geocode(
            DetermineRouteRequestType input) throws OLSException {
        JAXBElement<DetermineRouteResponseType> retval = null;
        DatatypeFactory                         df;
        
        try {
            df = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new OLSException("Datatype factory configuration exception", e);
        }
        
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
        
        PositionType    endPosition = (PositionType)endLocation.getValue();
        
        XMLGregorianCalendar    cal = routePlan.getExpectedEndTime();
        boolean                 endTime = true;
        
        if (cal == null) {
            cal = routePlan.getExpectedStartTime();
            endTime = false;
            
            if (cal == null) {
                cal = df.newXMLGregorianCalendar(new GregorianCalendar());
            }
        }
        
        RoutePreferenceType     preference = routePlan.getRoutePreference();
        
        if (routePlan.isUseRealTimeTraffic()) {
            throw new OLSException("Use of real time traffic information is unsupported");
        }
        
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        
        // Build RESTful OPT request
        Client  client = Client.create();
        
//        client.setConnectTimeout(interval);
//        client.setReadTimeout(interval);
        
        MultivaluedMap<String,String>  queryParams = new MultivaluedMapImpl();
        
        queryParams.add("fromPlace",    formatPosition(startPosition));
        queryParams.add("toPlace",      formatPosition(endPosition));
        queryParams.add("date",         dateFormat.format(cal.toGregorianCalendar().getTime()));
        queryParams.add("time",         timeFormat.format(cal.toGregorianCalendar().getTime()));
        queryParams.add("arriveBy",     Boolean.toString(endTime));
        
        switch (preference) {
        case FASTEST:
            queryParams.add("optimize", "QUICK");
//            queryParams.add("mode",     "CAR,CUSTOM_MOTOR_VEHICLE"); -> non funziona con la versione 0.9.2-SNAPSHOT
            queryParams.add("mode",     "CAR");
            break;
        case SHORTEST:
            queryParams.add("optimize", "QUICK"); // FIXME ???
//            queryParams.add("mode",     "CAR,CUSTOM_MOTOR_VEHICLE"); -> non funziona con la versione 0.9.2-SNAPSHOT
            queryParams.add("mode",     "CAR");
            break;
        case PEDESTRIAN:
            queryParams.add("optimize", "QUICK"); // FIXME ???
            queryParams.add("mode",     "WALK");
            break;
        }

        WebResource     resource = client.resource(getEndpointAddress() + "/ws/plan");
        
        Response        response = resource.queryParams(queryParams).accept(MediaType.TEXT_XML).get(Response.class);

        // Parse OTP Response
        PlannerError    error = response.getError();
        
        if (error != null) {
            throw new OLSException("OTP planner error"); // FIXME articulate
        }
        
        TripPlan        tripPlan = response.getPlan();
        
        if (tripPlan == null) {
            throw new OLSException("Missing OTP trip plan");
        }
        
        Itineraries     itineraries = tripPlan.getItineraries();
        
        if (itineraries == null) {
            throw new OLSException("Missing OTP itineraries");
        }
        
        List<Itinerary> itineraryList = itineraries.getItinerary();
        
        if (itineraryList == null) {
            throw new OLSException("Missing OTP itinerary list");
        }
        
        Itinerary       itinerary = itineraryList.get(0);
        
        if (itinerary == null) {
            throw new OLSException("Missing OTP itinerary");
        }
        
        ObjectFactory                   of = new ObjectFactory();
        DetermineRouteResponseType      determineRouteResponse = of.createDetermineRouteResponseType();
        RouteSummaryType                routeSummary = of.createRouteSummaryType();
        RouteInstructionsListType       routeInstructions = of.createRouteInstructionsListType();
        
        routeSummary.setTotalTime(df.newDuration(itinerary.getDuration()));
        
        double                  totalDistance = 0.0;
        EncodedPolylineBean     legGeometry;
        Geometry                geometry = null;
        GeometryFactory         gf = new GeometryFactory();
        Steps                   legSteps;
        RouteInstruction        routeInstruction;
        DistanceType            distance;
        
        for (Leg leg : itinerary.getLegs().getLeg()) {
            totalDistance += leg.getDistance();
            legGeometry = leg.getLegGeometry();
            
            if (geometry == null) {
                geometry = gf.createLineString(decode(legGeometry).toArray(new Coordinate[0]));
            } else {
                geometry = geometry.union(gf.createLineString(decode(legGeometry).toArray(new Coordinate[0])));
            }
            
            legSteps = leg.getSteps();
            
            for (WalkStep walkStep : legSteps.getWalkSteps()) {
                routeInstruction = of.createRouteInstruction();
                distance = of.createDistanceType();
                distance.setValue(BigDecimal.valueOf(walkStep.getDistance()));
                routeInstruction.setDistance(distance);
                routeInstruction.setInstruction("Procedi in direzione " + walkStep.getAbsoluteDirection() + " per " + walkStep.getDistance() + " su " + walkStep.getStreetName()); // FIXME: i18n
                routeInstructions.getRouteInstructions().add(routeInstruction);
            }
        }
        
        distance = of.createDistanceType();
        distance.setValue(BigDecimal.valueOf(totalDistance));
        distance.setAccuracy(BigDecimal.ONE); // FIXME
        distance.setUom(DistanceUnitType.M); // FIXME ???
        routeSummary.setTotalDistance(distance);
        
        RouteGeometryType       routeGeometry = of.createRouteGeometryType();
        LineStringType          lineString = of.createLineStringType();
        List<Pos>               posList = lineString.getPos();
        Pos                     pos;
        List<Double>            posValues;
        double                  minX = 360;
        double                  maxX = -360;
        double                  minY = 360;
        double                  maxY = -360;
        
        for (Coordinate coord : geometry.getCoordinates()) {
            pos = new Pos();
            posValues = pos.getValues();
            posValues.add(coord.x);
            posValues.add(coord.y);
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
        
        routeGeometry.setLineString(lineString);
        
        EnvelopeType    boundingBox = of.createEnvelopeType();
        
        posList = boundingBox.getPos();
        
        pos = new Pos();
        posValues = pos.getValues();
        posValues.add(minX);
        posValues.add(minY);
        posList.add(pos);
        
        pos = new Pos();
        posValues = pos.getValues();
        posValues.add(maxX);
        posValues.add(maxY);
        posList.add(pos);
        
        routeSummary.setBoundingBox(boundingBox);
        
        determineRouteResponse.setRouteSummary(routeSummary);
        determineRouteResponse.setRouteGeometry(routeGeometry);
        determineRouteResponse.setRouteInstructionsList(routeInstructions);
        
        retval = of.createDetermineRouteResponse(determineRouteResponse);
        
        return retval;
    }
    
    private String formatPosition(PositionType position)
    {
        StringBuffer    sb = new StringBuffer();
        
        for (Double coord : position.getPoint().getPos().getValues()) {
            if (sb.length() != 0) {
                sb.append(",");
            }
            
            sb.append(coord);
        }
        
        return sb.toString();
    }
    
    private static List<Coordinate> decode(EncodedPolylineBean polyline)
    {
        String pointString = polyline.getPoints();

        double lat = 0;
        double lon = 0;

        int strIndex = 0;
        List<Coordinate> points = new ArrayList<Coordinate>();

        while (strIndex < pointString.length()) {
            int[] rLat = PolylineEncoder.decodeSignedNumberWithIndex(pointString, strIndex);
            lat = lat + rLat[0] * 1e-5;
            strIndex = rLat[1];

            int[] rLon = PolylineEncoder.decodeSignedNumberWithIndex(pointString, strIndex);
            lon = lon + rLon[0] * 1e-5;
            strIndex = rLon[1];

            points.add(new Coordinate(lat, lon));
        }

        return points;
    }
}
