package it.phoops.geoserver.ols.geocoding.rfc59;

import static org.junit.Assert.assertNotNull;

import javax.xml.bind.JAXBElement;

import net.opengis.www.xls.AddressType;
import net.opengis.www.xls.GeocodeRequestType;
import net.opengis.www.xls.GeocodeResponseType;
import net.opengis.www.xls.NamedPlaceClassification;
import net.opengis.www.xls.ObjectFactory;
import net.opengis.www.xls.Place;
import net.opengis.www.xls.Street;
import net.opengis.www.xls.StreetAddress;

import org.junit.Before;
import org.junit.Test;

public class RFC59ServiceProviderTest {
    private RFC59ServiceProvider       serviceProvider;
    
    @Before
    public void setUp() throws Exception {
        serviceProvider = (RFC59ServiceProvider) new RFC59ServiceProvider();
        serviceProvider.setEndpointAddress("http://webtrial.regione.toscana.it/normaws/Musume.jws?wsdl");
        serviceProvider.setTimeout("60000");
        serviceProvider.setAlgorithm("2");
    }
    
    @Test
    public void testGeocodeStreet() throws Exception {
        ObjectFactory   of = new ObjectFactory();
        
        assertNotNull(of);
        
        GeocodeRequestType      request = of.createGeocodeRequestType();
        AddressType             address = of.createAddressType();
        StreetAddress           streetAddress = of.createStreetAddress();
        Street                  street = of.createStreet();
        Place                   place;
        
        address.setCountryCode("IT");
        address.setPostalCode("50018");
        
        street.setValue("Via Roma");
        
        streetAddress.getStreets().add(street);
        address.setStreetAddress(streetAddress);
        
        place = of.createPlace();
        place.setType(NamedPlaceClassification.MUNICIPALITY);
        place.setValue("Scandicci");
        address.getPlaces().add(place);
        
        place = of.createPlace();
        place.setType(NamedPlaceClassification.COUNTRY_SECONDARY_SUBDIVISION);
        place.setValue("FI");
        address.getPlaces().add(place);
        
        request.getAddresses().add(address);
        
        JAXBElement<GeocodeResponseType>        response = serviceProvider.geocode(request);
        assertNotNull(response);
    }
}
