package it.phoops.geoserver.ols.geocoding.solr;

import it.phoops.geoserver.ols.OLSAbstractServiceProvider;
import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.geocoding.GeocodingServiceProvider;
import it.phoops.geoserver.ols.geocoding.solr.component.SOLRTab;
import it.phoops.geoserver.ols.geocoding.solr.component.SOLRTabFactory;
import it.phoops.geoserver.ols.solr.utils.SolrPager;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBElement;

import net.opengis.www.xls.AbstractStreetLocatorType;
import net.opengis.www.xls.AddressType;
import net.opengis.www.xls.BuildingLocatorType;
import net.opengis.www.xls.GeocodeMatchCode;
import net.opengis.www.xls.GeocodeRequestType;
import net.opengis.www.xls.GeocodeResponseList;
import net.opengis.www.xls.GeocodeResponseType;
import net.opengis.www.xls.GeocodedAddressType;
import net.opengis.www.xls.NamedPlaceClassification;
import net.opengis.www.xls.ObjectFactory;
import net.opengis.www.xls.Place;
import net.opengis.www.xls.PointType;
import net.opengis.www.xls.Pos;
import net.opengis.www.xls.Street;
import net.opengis.www.xls.StreetAddress;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
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

public class SOLRServiceProvider extends OLSAbstractServiceProvider implements GeocodingServiceProvider {
    public static final String COUNTRY_CODE = "IT";

    // Properties Name
    private static final String PN_ENDPOINT_ADDRESS = "OLS.serviceProvider.geocoding.solr.service.endpointAddress";
    private static final String PN_ACTIVE_SERVICE = "OLS.serviceProvider.service.active";

    private String descriptionKey;
    private Properties properties = new Properties();

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

    public String getActive() {
        return properties.getProperty(PN_ACTIVE_SERVICE);
    }

    public void setActive(String activeService) {
        properties.setProperty(PN_ACTIVE_SERVICE, activeService);
    }

    @Override
    public OLSService getServiceType() {
        return OLSService.GEOCODING;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public ITab getTab() {
        IModel<String> title = new ResourceModel("SOLR ", "SOLR");
        return SOLRTabFactory.getSOLRTabFactory().getSOLRTab(title);
    }

    @Override
    public void handleServiceChange(ServiceInfo service, List<String> propertyNames,
            List<Object> oldValues, List<Object> newValues) {
        String url = ((SOLRTab) getTab()).getUrlSOLR();
        String active = ((SOLRTab) getTab()).getActiveSOLR();

        setEndpointAddress(url);
        setActive(active);

    }

    @Override
    public void setPropertiesTab(ITab solrTab) {
        ((SOLRTab) solrTab).setUrlSOLR(this.getEndpointAddress());
        ((SOLRTab) solrTab).setActiveSOLR(this.getActive());
    }

    @Override
    public boolean isServiceActive() {
        return Boolean.parseBoolean(this.getActive());
    }

    @Override
    public JAXBElement<GeocodeResponseType> geocode(GeocodeRequestType input) throws OLSException {
        ObjectFactory                                           of = new ObjectFactory();
        GeocodeResponseType                                     output = of.createGeocodeResponseType();
        JAXBElement<GeocodeResponseType>                        retval = of.createGeocodeResponse(output);
        String                                                  solrQuery;
        SolrServer                                              solrServer = new HttpSolrServer(getEndpointAddress());
        ModifiableSolrParams                                    solrParams = new ModifiableSolrParams();
        SolrDocumentList                                        solrDocs;
        List<GeocodeResponseList>                               responseList = output.getGeocodeResponseLists();
        GeocodeResponseList                                     listItem;
        List<Place>                                             places;
        StreetAddress                                           streetAddress;
        List<Street>                                            streets;
        Street                                                  street;
        JAXBElement<? extends AbstractStreetLocatorType>        streetLocator;
        BuildingLocatorType                                     buildingLocator;
        String                                                  buildingNumber;
        String                                                  municipality;
        String                                                  countrySecondarySubdivision;
        List<GeocodedAddressType>                               geocodedAddresses;
        GeocodedAddressType                                     geocodedAddress;
        PointType                                               point;
        Pos                                                     pos;
        WKTReader                                               wktReader = null;
        StringReader                                            sr;
        Geometry                                                geometry;
        List<Double>                                            coordinates;
        Place                                                   place;
        AddressType                                             returnAddress;
        GeocodeMatchCode                                        geocodeMatchCode;
        
        for (AddressType address : input.getAddresses()) {
            // We manage only requests regarding Italy (moreover, Tuscany...)
            if (!COUNTRY_CODE.equalsIgnoreCase(address.getCountryCode())) {
                throw new OLSException("Unsupported country code: " + address.getCountryCode());
            }

            streetAddress = address.getStreetAddress();

            // Pass freeform requests to SOLR
            if (address.getFreeFormAddress() != null && !address.getFreeFormAddress().equals("")) {
                solrQuery = "name:\"" + ClientUtils.escapeQueryChars(address.getFreeFormAddress()) + "\"";
            } else {
                // check for structured address presence
                if (streetAddress == null) {
                    throw new OLSException("StreetAddress missing in geocoding request");
                }

                // Check for streets presence
                streets = streetAddress.getStreets();

                if (streets == null || streets.size() < 1) {
                    throw new OLSException("Streets list missing or empty in geocoding request");
                }

                // Only first street is handled here
                if (streets.size() > 1) {
                    throw new OLSException("Cannot manage street crossing");
                }

                street = streets.get(0);

                if (street == null) {
                    throw new OLSException("Street missing in geocoding request");
                }

                // Check for street name presence
                if (street.getValue() == null || street.getValue().equals("")) {
                    // Use structured data
                    if (street.getOfficialName() == null || street.getOfficialName().equals("")) {
                        throw new OLSException("Street name missing in geocoding request");
                    }
                    
                    solrQuery = "street_name:\"" + ClientUtils.escapeQueryChars(street.getOfficialName()) + "\"";
                    
                    if (street.getTypePrefix() != null && !street.getTypePrefix().equals("")) {
                        solrQuery += " AND street_type:\"" + ClientUtils.escapeQueryChars(street.getTypePrefix()) + "\"";
                    }
                } else {
                    // Use free form data
                    //check if the street contains a number
//                    String[] stgStreet = street.getValue().split(",");
//                    if(stgStreet.length > 1){
//                        String stgNumeber = stgStreet[1];
//                        try{
//                            Integer.parseInt(stgNumeber.trim());
//                        }catch(NumberFormatException e){
//                            throw new OLSException("Cannot manage street number");
//                        }
//                        solrQuery = "name:\"" + ClientUtils.escapeQueryChars(stgStreet[0]) +"\"" + " AND number: \""+ ClientUtils.escapeQueryChars(stgNumeber.trim()) +"\"";
//                    }else{
                        solrQuery = "name:\"" + ClientUtils.escapeQueryChars(street.getValue()) + "\"";
//                    }
                }
            }

            // Check for building number (optional)
            buildingNumber = null;
            streetLocator = streetAddress.getStreetLocation();

            if (streetLocator != null) {
                if (streetLocator.getValue() instanceof BuildingLocatorType) {
                    buildingLocator = (BuildingLocatorType) streetLocator.getValue();
                    buildingNumber = buildingLocator.getNumber();

                    if (buildingLocator.getSubdivision() != null
                            && !buildingLocator.getSubdivision().equals("")) {
                        buildingNumber += "/" + buildingLocator.getSubdivision();
                    }
                    
                    solrQuery += " AND building_number:\"" + ClientUtils.escapeQueryChars(buildingNumber) + "\"";
                }
            }

            // Check places: municipality has to be there (at least) (and once, please)
            places = address.getPlaces();

            if (places == null || places.size() < 1) {
                throw new OLSException("Places list missing or empty in geocoding request");
            }

            municipality = null;
            countrySecondarySubdivision = null;

            for (Place pivot : places) {
                switch (pivot.getType()) {
                case MUNICIPALITY:
                    if (municipality != null && !municipality.equals("")) {
                        throw new OLSException(
                                "Too many municipalities in geocoding request: old one "
                                        + municipality + " new one: " + pivot.getValue());
                    }

                    municipality = pivot.getValue();
                    solrQuery += " AND municipality:\"" + ClientUtils.escapeQueryChars(municipality) + "\"";
                    break;
                case COUNTRY_SECONDARY_SUBDIVISION:
                    if (countrySecondarySubdivision != null
                            && !countrySecondarySubdivision.equals("")) {
                        throw new OLSException(
                                "Too many country secondary subdivisions in geocoding request: old one "
                                        + countrySecondarySubdivision + " new one: "
                                        + pivot.getValue());
                    }

                    countrySecondarySubdivision = pivot.getValue();
                    solrQuery += " AND country_subdivision:\"" + ClientUtils.escapeQueryChars(countrySecondarySubdivision) + "\"";
                    break;
                default:
                    break;
                }
            }
            
            solrParams.set("q", solrQuery);

            try {
                // Call SOLR
                solrDocs = SolrPager.query(solrServer, solrParams);
                
                listItem = of.createGeocodeResponseList();
                listItem.setNumberOfGeocodedAddresses(BigInteger.valueOf(solrDocs.getNumFound()));
                geocodedAddresses = listItem.getGeocodedAddresses();
                
                for (SolrDocument solrDoc : solrDocs) {
                    if (wktReader == null) {
                        wktReader = new WKTReader();
                    }
                    
                    sr = new StringReader(solrDoc.getFieldValue("centroid").toString());
                    
                    try {
                        geometry = wktReader.read(sr);
                        
                        geocodedAddress = of.createGeocodedAddressType();
                        point = of.createPointType();
                        pos = of.createPos();
                        
                        pos.setDimension(BigInteger.valueOf(2));
                        
                        coordinates = pos.getValues();
                        
                        coordinates.add(Double.valueOf(geometry.getCoordinate().getOrdinate(0)));
                        coordinates.add(Double.valueOf(geometry.getCoordinate().getOrdinate(1)));
//                        pos.setSrsName(value);
                        
                        point.setPos(pos);
//                        point.setSrsName(value);
//                        point.setId(value);
                        geocodedAddress.setPoint(point);
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
                    
                    // FIXME Manca, aggiungere a schema.xml?
//                  returnAddress.setPostalCode(datiNormalizzazioneInd.getCap());
                    
                    streetAddress = of.createStreetAddress();
                    street = of.createStreet();
                    
                    // building_number
//                    if (solrDoc.getFieldValue("building_number") != null && !solrDoc.getFieldValue("building_number").equals("") && !solrDoc.getFieldValue("building_number").equals("0")) {
//                        street.setValue(solrDoc.getFieldValue("name") + ", " + solrDoc.getFieldValue("building_number"));
//                    } else {
                        street.setValue(solrDoc.getFieldValue("name").toString());
//                    }
                    
                    streetAddress.getStreets().add(street);
                    
                    returnAddress.setStreetAddress(streetAddress);
                    
                    geocodedAddress.setAddress(returnAddress);
                    
                    geocodeMatchCode = of.createGeocodeMatchCode();
                    geocodeMatchCode.setMatchType("SOLR");
                    geocodeMatchCode.setAccuracy(new Float(1));
                    
                    geocodedAddress.setGeocodeMatchCode(geocodeMatchCode);
                    geocodedAddresses.add(geocodedAddress);
                }

                responseList.add(listItem);
            } catch (SolrServerException e) {
                throw new OLSException("SOLR error: " + e.getLocalizedMessage(), e);
            }
        }

        return retval;
    }

}
