package it.phoops.geoserver.ols.reverse.solr;

import it.phoops.geoserver.ols.OLSAbstractServiceProvider;
import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.geocoding.ReverseGeocodingServiceProvider;
import it.phoops.geoserver.ols.reverse.solr.component.SOLRTabReverse;
import it.phoops.geoserver.ols.reverse.solr.component.SOLRTabReverseFactory;
import it.phoops.geoserver.ols.solr.utils.SolrPager;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBElement;

import net.opengis.www.xls.AbstractStreetLocatorType;
import net.opengis.www.xls.AddressType;
import net.opengis.www.xls.BuildingLocatorType;
import net.opengis.www.xls.GeocodeMatchCode;
import net.opengis.www.xls.GeocodeResponseList;
import net.opengis.www.xls.GeocodeResponseType;
import net.opengis.www.xls.GeocodedAddressType;
import net.opengis.www.xls.NamedPlaceClassification;
import net.opengis.www.xls.ObjectFactory;
import net.opengis.www.xls.Place;
import net.opengis.www.xls.PointType;
import net.opengis.www.xls.Pos;
import net.opengis.www.xls.PositionType;
import net.opengis.www.xls.ReverseGeocodeRequestType;
import net.opengis.www.xls.ReverseGeocodeResponseType;
import net.opengis.www.xls.ReverseGeocodedLocationType;
import net.opengis.www.xls.Street;
import net.opengis.www.xls.StreetAddress;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.config.ServiceInfo;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class SOLRReverseServiceProvider extends OLSAbstractServiceProvider implements ReverseGeocodingServiceProvider{
        public static final String COUNTRY_CODE = "IT";
        
	//property name
	private static final String  PN_ENDPOINT_ADDRESS = "OLS.serviceProvider.reverse.solr.service.endpointAddress";
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
		return OLSService.REVERSE_GEOCODING;
	}

	@Override
	public ITab getTab() {
		IModel<String> title = new ResourceModel("SOLR ", "SOLR");
	    return SOLRTabReverseFactory.getSOLRTabReverseFactory().getSOLRTabReverse(title);
	}

	@Override
	public void setPropertiesTab(ITab solrTabReverse) {
	    ((SOLRTabReverse)solrTabReverse).setUrlSOLRReverse(this.getEndpointAddress());
	    ((SOLRTabReverse)solrTabReverse).setActiveSOLRReverse(this.getActive());
	}

	@Override
	public void handleServiceChange(ServiceInfo service,
			List<String> propertyNames, List<Object> oldValues,
			List<Object> newValues) {
        String url = ((SOLRTabReverse)getTab()).getUrlSOLRReverse();
        String active = ((SOLRTabReverse)getTab()).getActiveSOLRReverse();
        
        setEndpointAddress(url);
        setActive(active);
	}
	
	@Override
	public boolean isServiceActive() {
		return Boolean.parseBoolean(this.getActive());
	}

	@Override
	public JAXBElement<ReverseGeocodeResponseType> geocode(
			ReverseGeocodeRequestType input) throws OLSException {
	        ObjectFactory                                           of = new ObjectFactory();
	        ReverseGeocodeResponseType                              output = of.createReverseGeocodeResponseType();
	        JAXBElement<ReverseGeocodeResponseType>                 retval = of.createReverseGeocodeResponse(output);
	        ReverseGeocodedLocationType                             listItem;
	        SolrServer                                              solrServer = new HttpSolrServer(getEndpointAddress());
	        ModifiableSolrParams                                    solrParams = new ModifiableSolrParams();
	        SolrDocumentList                                        solrDocs;
	        PositionType                                            positionType;
	        PointType                                               pointType;
	        PointType                                               point;
	        Pos                                                     pos;
	        List<Double>                                            coordinates;
	        WKTReader                                               wktReader = null;
	        StringReader                                            sr;
	        Geometry                                                geometry;
	        List<AddressType>                                       reverseAddresses;
	        AddressType                                             returnAddress;
	        List<Place>                                             places;
	        Place                                                   place;
	        StreetAddress                                           streetAddress;
	        Street                                                  street;
	        GeocodeMatchCode                                        geocodeMatchCode;
	        List<ReverseGeocodedLocationType>                       responseList = output.getReverseGeocodedLocations();
	        
	        
	        solrParams.set("q", "");
	        positionType = input.getPosition();
	        //Check if the position exist
	        if(positionType == null){
	            throw new OLSException("No match Position Type");
	        }
	        pointType = positionType.getPoint();
	        //Check if the point exist
	        if(pointType == null){
	            throw new OLSException("Point missing in reverse geocode request");
	        }
	        pos = pointType.getPos();
	        coordinates = pos.getValues();

	        SolrQuery query = new SolrQuery("centerline:\"Intersects(Circle("+coordinates.get(0)+","+coordinates.get(1)+", d=0.0001))\"");
	        
	        try {
	            //CAll Solr
	            solrDocs = SolrPager.query(solrServer, query);

	            for (SolrDocument solrDoc : solrDocs) {
	                if (wktReader == null) {
	                        wktReader = new WKTReader();
	                }
	                listItem = of.createReverseGeocodedLocationType();
	                sr = new StringReader(solrDoc.getFieldValue("centroid").toString());
	                
	                try {
	                        geometry = wktReader.read(sr);
	                        
	                        point = of.createPointType();
	                        pos = of.createPos();
	                        
	                        pos.setDimension(BigInteger.valueOf(2));
	                        
	                        coordinates = pos.getValues();
	                        
	                        coordinates.add(Double.valueOf(geometry.getCoordinate().getOrdinate(0)));
	                        coordinates.add(Double.valueOf(geometry.getCoordinate().getOrdinate(1)));
	                        // pos.setSrsName(value);
	                        
	                        point.setPos(pos);
	                        // point.setSrsName(value);
	                        // point.setId(value);
	                        listItem.setPoint(point);
	                    } catch (ParseException e) {
	                        throw new OLSException("WKT parse error: " + e.getLocalizedMessage(), e);
	                    }
	                
	                returnAddress = of.createAddressType();
	                returnAddress.setCountryCode(COUNTRY_CODE);
	                places = returnAddress.getPlaces();
	                
	                if (solrDoc.getFieldValue("municipality") != null) {
	                        place = of.createPlace();
	                        
	                        place.setType(NamedPlaceClassification.MUNICIPALITY);
	                        place.setValue(solrDoc.getFieldValue("municipality").toString());
	                        
	                        places.add(place);
	                }
	                
	                if (solrDoc.getFieldValue("country_subdivision") != null) {
	                        place = of.createPlace();
	                        
	                        place.setType(NamedPlaceClassification.COUNTRY_SECONDARY_SUBDIVISION);
	                        place.setValue(solrDoc.getFieldValue("country_subdivision").toString());
	                        
	                        places.add(place);
	                }
	                place = of.createPlace();
	                
	                place.setType(NamedPlaceClassification.COUNTRY_SUBDIVISION);
	                place.setValue("Toscana");
	                    
	                places.add(place);
	                
	                // Manca
	                // returnAddress.setPostalCode(datiNormalizzazioneInd.getCap());
	                
	                streetAddress = of.createStreetAddress();
	                street = of.createStreet();
	                street.setValue(solrDoc.getFieldValue("name").toString());
	                streetAddress.getStreets().add(street);
	                returnAddress.setStreetAddress(streetAddress);
	                
	                listItem.setAddress(returnAddress);
	                
	                geocodeMatchCode = of.createGeocodeMatchCode();
	                geocodeMatchCode.setMatchType("SOLR");
	                geocodeMatchCode.setAccuracy(new Float(1));
	                
	                responseList.add(listItem);
	            }
	            
	        } catch (SolrServerException e) {
	            throw new OLSException("SOLR error: " + e.getLocalizedMessage(), e);
	        }
	        
		return retval;
	}
}
