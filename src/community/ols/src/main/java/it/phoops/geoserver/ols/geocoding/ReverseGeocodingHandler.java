package it.phoops.geoserver.ols.geocoding;

import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSHandler;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.OLSServiceProvider;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.www.xls.ReverseGeocodeRequestType;
import net.opengis.www.xls.ReverseGeocodeResponseType;

import org.w3c.dom.Document;

public class ReverseGeocodingHandler implements OLSHandler {
    private ReverseGeocodingServiceProvider    provider;

    @Override
    public Document processRequest(Document request) throws OLSException {
    	JAXBContext             	   jaxbContext = null;
    	ReverseGeocodeRequestType      input = null;
        
        try {
            jaxbContext = JAXBContext.newInstance(ReverseGeocodeRequestType.class);
            
            Unmarshaller        						unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<ReverseGeocodeRequestType>      jaxbElement = unmarshaller.unmarshal(request.getFirstChild(), ReverseGeocodeRequestType.class);
            
            input = jaxbElement.getValue();
        } catch (JAXBException e) {
            throw new OLSException("JAXB error", e);
        }
        
        JAXBElement<ReverseGeocodeResponseType>      output = provider.reverseGeocode(input);
        Document                                	domResponse = null;
        
        try {
            Marshaller              marshaller = jaxbContext.createMarshaller();
            DocumentBuilderFactory  domFactory = DocumentBuilderFactory.newInstance();
            
            domFactory.setNamespaceAware(true);
            
            DocumentBuilder         domBuilder = domFactory.newDocumentBuilder();
            
            domResponse = domBuilder.newDocument();
            marshaller.marshal(output, domResponse);
        } catch (JAXBException e) {
            throw new OLSException("JAXB error: " + e.getLocalizedMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new OLSException("JAXP error: " + e.getLocalizedMessage(), e);
        }
        
        return domResponse;
    }

    @Override
    public OLSService getService() {
        return OLSService.REVERSE_GEOCODING;
    }

    @Override
    public void setServiceProvider(OLSServiceProvider provider) {
        this.provider = (ReverseGeocodingServiceProvider)provider;
    }
    
    @Override
    public void setActiveServiceProvider(OLSServiceProvider provider) {
        if(provider.isServiceActive())
            this.provider = (ReverseGeocodingServiceProvider)provider;
    }
}
