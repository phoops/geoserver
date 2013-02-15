package it.phoops.geoserver.ols.geocoding.rfc59;

import it.phoops.geoserver.ols.OLSAbstractServiceProvider;
import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.geocoding.GeocodingServiceProvider;
import it.phoops.geoserver.ols.geocoding.rfc59.component.RFC59Tab;
import it.phoops.geoserver.ols.geocoding.rfc59.component.RFC59TabFactory;
import it.toscana.regione.normaws.AmbiguitaIndItem;
import it.toscana.regione.normaws.DatiGeoreferenziazioneInd;
import it.toscana.regione.normaws.DatiNormalizzazioneInd;
import it.toscana.regione.normaws.DatiNormalizzazioneLoc;
import it.toscana.regione.normaws.IndirizzoRiconosciuto;
import it.toscana.regione.normaws.MusumeServiceLocator;
import it.toscana.regione.normaws.MusumeSoapBindingStub;
import it.toscana.regione.normaws.RispostaNormalizzataType;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBElement;
import javax.xml.rpc.ServiceException;

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

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.config.ServiceInfo;

public class RFC59ServiceProvider extends OLSAbstractServiceProvider implements GeocodingServiceProvider {
    public static final DataSource DATA_SOURCE = DataSource.REGIONE_TOSCANA;
    public static final String COUNTRY_CODE = "IT";

    // Properties names
    public static final String  PN_ENDPOINT_ADDRESS = "OLS.serviceProvider.geocoding.rfc59.service.endpointAddress";
    public static final String  PN_GEOCODING_ALGORITHM = "OLS.serviceProvider.geocoding.rfc59.algorithm";
    public static final String  PN_TIMEOUT = "OLS.serviceProvider.geocoding.rfc59.service.timeout";
    public static final String  PN_ACTIVE_SERVICE = "OLS.serviceProvider.service.active";
    
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

    public String getAlgorithm() {
        return properties.getProperty(PN_GEOCODING_ALGORITHM);
    }

    public void setAlgorithm(String algorithm) {
        properties.setProperty(PN_GEOCODING_ALGORITHM, algorithm);
    }

    public String getTimeout() {
        return properties.getProperty(PN_TIMEOUT);
    }

    public void setTimeout(String timeout) {
        properties.setProperty(PN_TIMEOUT, timeout);
    }
    
    public String getActive(){
        return properties.getProperty(PN_ACTIVE_SERVICE);
    }
    
    public void setActive(String activeService){
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
                IModel<String> title = new ResourceModel("RFC59 ", "RFC59");
                return RFC59TabFactory.getRFC59TabFactory().getRFC59Tab(title);
        }

        @Override
        public void setPropertiesTab(ITab rfc59Tab) {
                ((RFC59Tab)rfc59Tab).setUrlRFC59(this.getEndpointAddress());
                ((RFC59Tab)rfc59Tab).setTimeoutRFC59(this.getTimeout());
                Algorithm algorithm = Algorithm.get(this.getAlgorithm());
                ((RFC59Tab)rfc59Tab).setCodeAlgorithmSelected(Integer.parseInt(algorithm.getCode()));
                ((RFC59Tab)rfc59Tab).setActiveRFC59(this.getActive());
       }

        @Override
    	public void handleServiceChange(ServiceInfo service,
    			List<String> propertyNames, List<Object> oldValues,
    			List<Object> newValues) {
                String active = ((RFC59Tab)getTab()).getActiveRFC59();
                String timeout = ((RFC59Tab)getTab()).getTimeoutRFC59();
    		String url = ((RFC59Tab)getTab()).getUrlRFC59();
    		String algorithm = ((RFC59Tab)getTab()).getSelectedAlgorithm().getCode();
    		
    		setEndpointAddress(url);
    		setTimeout(timeout);
    		setAlgorithm(algorithm);
    		setActive(active);
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
        MusumeSoapBindingStub                                   binding = null;
        RispostaNormalizzataType                                rispostaNormalizzata;
        List<GeocodeResponseList>                               responseList = output.getGeocodeResponseLists();
        GeocodeResponseList                                     listItem;
        List<Place>                                             places;
        StreetAddress                                           streetAddress;
        List<Street>                                            streets;
        Street                                                  street;
        String                                                  streetName;
        JAXBElement<? extends AbstractStreetLocatorType>        streetLocator;
        BuildingLocatorType                                     buildingLocator;
        String                                                  buildingNumber;
        String                                                  municipality;
        String                                                  countrySecondarySubdivision;
        List<GeocodedAddressType>                               geocodedAddresses;
        IndirizzoRiconosciuto                                   indirizzoRiconosciuto;
        GeocodedAddressType                                     geocodedAddress;
        PointType                                               point;
        Pos                                                     pos;
        List<Double>                                            coordinates;
        DatiGeoreferenziazioneInd                               datiGeoreferenziazioneInd;
        AddressType                                             returnAddress;
        DatiNormalizzazioneInd                                  datiNormalizzazioneInd;
        GeocodeMatchCode                                        geocodeMatchCode;
        DatiNormalizzazioneLoc                                  datiNormalizzazioneLoc;

        for (AddressType address : input.getAddresses()) {
            // We manage only requests regarding Italy (moreover, Tuscany...)
            if (!COUNTRY_CODE.equalsIgnoreCase(address.getCountryCode())) {
                throw new OLSException("Unsupported country code: " + address.getCountryCode());
            }
            
            // We cannot parse freeform requests in this backend
            if (address.getFreeFormAddress() != null && !address.getFreeFormAddress().equals("")) {
                throw new OLSException("Cannot parse free form requests");
            }
            
            // check for structured address presence
            streetAddress = address.getStreetAddress();
            
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
                
                streetName = street.getOfficialName();
                
                if (street.getTypePrefix() != null && !street.getTypePrefix().equals("")) {
                    streetName = street.getTypePrefix() + " " + streetName;
                }
                
                if (street.getTypeSuffix() != null && !street.getTypeSuffix().equals("")) {
                    streetName += street.getTypeSuffix();
                }
            } else {
                // Use free form data
                streetName = street.getValue();
            }
            
            // Check for building number (optional)
            buildingNumber = null;
            streetLocator = streetAddress.getStreetLocation();
            
            if (streetLocator != null) {
                if (streetLocator.getValue() instanceof BuildingLocatorType) {
                    buildingLocator = (BuildingLocatorType)streetLocator.getValue();
                    buildingNumber = buildingLocator.getNumber();
                    
                    if (buildingLocator.getSubdivision() != null && !buildingLocator.getSubdivision().equals("")) {
                        buildingNumber += "/" + buildingLocator.getSubdivision();
                    }
                }
            }
            
            // Check places: municipality has to be there (at least) (and once, please)
            places = address.getPlaces();
            
            if (places == null || places.size() < 1) {
                throw new OLSException("Places list missing or empty in geocoding request");
            }
            
            municipality = null;
            countrySecondarySubdivision = null;
            
            for (Place place : places) {
                switch (place.getType()) {
                case MUNICIPALITY:
                    if (municipality != null && !municipality.equals("")) {
                        throw new OLSException("Too many municipalities in geocoding request: old one " + municipality + " new one: " + place.getValue());
                    }
                    
                    municipality = place.getValue();
                    break;
                case COUNTRY_SECONDARY_SUBDIVISION:
                    if (countrySecondarySubdivision != null && !countrySecondarySubdivision.equals("")) {
                        throw new OLSException("Too many country secondary subdivisions in geocoding request: old one " + countrySecondarySubdivision + " new one: " + place.getValue());
                    }
                    
                    countrySecondarySubdivision = place.getValue();
                    break;
                default:
                    break;
                }
            }
            
            if (municipality == null || municipality.equals("")) {
                throw new OLSException("Municipality missing or empty in geocoding request");
            }
            
            // Lazy bind to web service
            if (binding == null) {
                try {
                    binding = (MusumeSoapBindingStub) new MusumeServiceLocator().getMusume(new URL(getEndpointAddress()));
                    // Time out after a minute
                    binding.setTimeout(Integer.valueOf(getTimeout()));
                } catch (ServiceException e) {
                    throw new OLSException("JAX-RPC error: " + e.getLocalizedMessage(), e);
                } catch (MalformedURLException e) {
                    throw new OLSException("Malformed URL error: " + e.getLocalizedMessage(), e);
                }
            }

            try {
                // Call RFC59 web service
                rispostaNormalizzata = binding.richiesta(Algorithm.get(getAlgorithm()).toString(),
                        streetName + (buildingNumber == null ? "" : ", " + buildingNumber), municipality, countrySecondarySubdivision,
                        address.getPostalCode(), DATA_SOURCE.toString());

                listItem = of.createGeocodeResponseList();
                listItem.setNumberOfGeocodedAddresses(BigInteger.ZERO);
                geocodedAddresses = listItem.getGeocodedAddresses();
                
                switch (ResponseType.get(rispostaNormalizzata.getTipoRispostaNorm())) {
                case GEOCODING_OK:
                    listItem.setNumberOfGeocodedAddresses(BigInteger.ONE);
                    indirizzoRiconosciuto = rispostaNormalizzata.getIndirizzoRiconosciuto();
                    
                    geocodedAddress = of.createGeocodedAddressType();
                    point = of.createPointType();
                    pos = of.createPos();
                    
                    pos.setDimension(BigInteger.valueOf(2));
                    
                    coordinates = pos.getValues();
                    datiGeoreferenziazioneInd = indirizzoRiconosciuto.getDatiGeoreferenziazioneInd();
                    coordinates.add(Double.valueOf(datiGeoreferenziazioneInd.getLongitudine()));
                    coordinates.add(Double.valueOf(datiGeoreferenziazioneInd.getLatitudine()));
                    
//                    pos.setSrsName(value);
                    
                    point.setPos(pos);
//                    point.setSrsName(value);
//                    point.setId(value);
                    geocodedAddress.setPoint(point);
                    returnAddress = of.createAddressType();
                    datiNormalizzazioneInd = indirizzoRiconosciuto.getDatiNormalizzazioneInd();
                    
                    returnAddress.setCountryCode(COUNTRY_CODE);
                    places = returnAddress.getPlaces();
                    
                    Place place = of.createPlace();
                    
                    place.setType(NamedPlaceClassification.MUNICIPALITY);
                    place.setValue(datiNormalizzazioneInd.getLocalita());
                    
                    places.add(place);
                    
                    place = of.createPlace();
                    
                    place.setType(NamedPlaceClassification.COUNTRY_SECONDARY_SUBDIVISION);
                    place.setValue(datiNormalizzazioneInd.getProvincia());
                    
                    places.add(place);
                    
                    place = of.createPlace();
                    
                    place.setType(NamedPlaceClassification.COUNTRY_SUBDIVISION);
                    place.setValue("Toscana");
                    
                    places.add(place);
                    
                    returnAddress.setPostalCode(datiNormalizzazioneInd.getCap());
                    
                    streetAddress = of.createStreetAddress();
                    street = of.createStreet();
                    
                    if (datiNormalizzazioneInd.getCivico() != null && !datiNormalizzazioneInd.getCivico().equals("") && !datiNormalizzazioneInd.getCivico().equals("0")) {
                        street.setValue(datiNormalizzazioneInd.getDug() + " " + datiNormalizzazioneInd.getToponimo() + ", " + datiNormalizzazioneInd.getCivico());
                    } else {
                        street.setValue(datiNormalizzazioneInd.getDug() + " " + datiNormalizzazioneInd.getToponimo());
                    }
                    
                    streetAddress.getStreets().add(street);
                    
                    returnAddress.setStreetAddress(streetAddress);
                    
                    geocodedAddress.setAddress(returnAddress);
                    
                    geocodeMatchCode = of.createGeocodeMatchCode();
                    geocodeMatchCode.setMatchType("RFC59-" + getAlgorithm());
                    geocodeMatchCode.setAccuracy(new Float(1));
                    
                    geocodedAddress.setGeocodeMatchCode(geocodeMatchCode);
                    geocodedAddresses.add(geocodedAddress);
                    break;
                case UNKNOWN_ADDRESS:
                    break;
                case UNKNOWN_MUNICIPALITY:
                    break;
                case INPUT_PARAMETER_ERROR:
                    throw new OLSException("Parameter error");
                case AMBIGUOUS_MUNICIPALITY:
//                    rispostaNormalizzata.getLocalitaAmbigua().getAmbiguitaLoc().getAmbiguitaLocItem()[0].get;
                    break;
                case AMBIGUOUS_ADDRESS:
                    listItem.setNumberOfGeocodedAddresses(BigInteger.valueOf(rispostaNormalizzata.getIndirizzoAmbiguo().getAmbiguitaInd().getAmbiguitaIndItemCount()));
                    
                    for (AmbiguitaIndItem ambiguitaInd : rispostaNormalizzata.getIndirizzoAmbiguo().getAmbiguitaInd().getAmbiguitaIndItem()) {
                        geocodedAddress = of.createGeocodedAddressType();
                        point = of.createPointType();
                        pos = of.createPos();
                        
                        pos.setDimension(BigInteger.valueOf(2));
                        
                        coordinates = pos.getValues();
                        coordinates.add(Double.valueOf(ambiguitaInd.getX()));
                        coordinates.add(Double.valueOf(ambiguitaInd.getY()));
                        
//                        pos.setSrsName(value);
                        
                        point.setPos(pos);
//                        point.setSrsName(value);
//                        point.setId(value);
                        geocodedAddress.setPoint(point);
                        returnAddress = of.createAddressType();
                        
                        returnAddress.setCountryCode(COUNTRY_CODE);
                        
                        streetAddress = of.createStreetAddress();
                        street = of.createStreet();
                        
                        if (ambiguitaInd.getCivico() != null && !ambiguitaInd.getCivico().equals("") && !ambiguitaInd.getCivico().equals("0")) {
                            street.setValue(ambiguitaInd.getIndirizzo() + ", " + ambiguitaInd.getCivico());
                        } else {
                            street.setValue(ambiguitaInd.getIndirizzo());
                        }
                        
                        streetAddress.getStreets().add(street);
                        
                        returnAddress.setStreetAddress(streetAddress);
                        
                        geocodedAddress.setAddress(returnAddress);
                        
                        geocodeMatchCode = of.createGeocodeMatchCode();
                        geocodeMatchCode.setMatchType("RFC59-" + getAlgorithm());
                        geocodeMatchCode.setAccuracy(new Float(0));
                        
                        geocodedAddress.setGeocodeMatchCode(geocodeMatchCode);
                        geocodedAddresses.add(geocodedAddress);
                    }
                    break;
                case MUNICIPALITY_DATA_ONLY:
                    datiNormalizzazioneLoc = rispostaNormalizzata.getLocalitaNormalizzata().getDatiNormalizzazioneLoc();
                    listItem.setNumberOfGeocodedAddresses(BigInteger.ONE);
                    geocodedAddress = of.createGeocodedAddressType();
                    point = of.createPointType();
                    pos = of.createPos();
                    
                    pos.setDimension(BigInteger.valueOf(2));
                    
                    coordinates = pos.getValues();
                    coordinates.add(Double.valueOf(datiNormalizzazioneLoc.getXSezioneIstat()));
                    coordinates.add(Double.valueOf(datiNormalizzazioneLoc.getYSezioneIstat()));
                    
//                    pos.setSrsName(value);
                    
                    point.setPos(pos);
//                    point.setSrsName(value);
//                    point.setId(value);
                    geocodedAddress.setPoint(point);
                    returnAddress = of.createAddressType();
                    
                    returnAddress.setCountryCode(COUNTRY_CODE);
                    places = returnAddress.getPlaces();
                    
                    place = of.createPlace();
                    
                    place.setType(NamedPlaceClassification.MUNICIPALITY_SUBDIVISION);
                    place.setValue(datiNormalizzazioneLoc.getFrazione());
                    
                    places.add(place);
                    
                    place = of.createPlace();
                    
                    place.setType(NamedPlaceClassification.MUNICIPALITY);
                    place.setValue(datiNormalizzazioneLoc.getLocalita());
                    
                    places.add(place);
                    
                    place = of.createPlace();
                    
                    place.setType(NamedPlaceClassification.COUNTRY_SECONDARY_SUBDIVISION);
                    place.setValue(datiNormalizzazioneLoc.getProvincia());
                    
                    places.add(place);
                    
                    place = of.createPlace();
                    
                    place.setType(NamedPlaceClassification.COUNTRY_SUBDIVISION);
                    place.setValue("Toscana");
                    
                    places.add(place);
                    
                    returnAddress.setPostalCode(datiNormalizzazioneLoc.getCap());
                    
                    geocodedAddress.setAddress(returnAddress);
                    
                    geocodeMatchCode = of.createGeocodeMatchCode();
                    geocodeMatchCode.setMatchType("RFC59-" + getAlgorithm());
                    geocodeMatchCode.setAccuracy(new Float(1));
                    
                    geocodedAddress.setGeocodeMatchCode(geocodeMatchCode);
                    geocodedAddresses.add(geocodedAddress);
                    break;
                case DATA_ACCESS_ERROR:
                    throw new OLSException("Data access error");
                }

                responseList.add(listItem);
            } catch (RemoteException e) {
                throw new OLSException("Remote error: " + e.getLocalizedMessage(), e);
            }
        }

        return retval;
    }
}
