package it.phoops.geoserver.ols.geocoding;

import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSHandler;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.www.xls.GeocodeRequestType;
import net.opengis.www.xls.GeocodeResponseType;

import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;

public class GeocodingHandler implements OLSHandler {

    @Override
    public Document processRequest(ApplicationContext applicationContext, Document request) throws OLSException {
        Map<String,GeocodingServiceProvider>    beans = applicationContext.getBeansOfType(GeocodingServiceProvider.class);
        GeocodingServiceProvider                provider = null;
        
        for (String beanName : beans.keySet()) {
            provider = beans.get(beanName);
            
            System.out.println(beanName + ": " + provider);
        }
        
        JAXBContext             jaxbContext = null;
        GeocodeRequestType      input = null;
        
        try {
            jaxbContext = JAXBContext.newInstance(GeocodeRequestType.class);
            
            Unmarshaller                        unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<GeocodeRequestType>     jaxbElement = unmarshaller.unmarshal(request.getFirstChild(), GeocodeRequestType.class);
            
            input = jaxbElement.getValue();
        } catch (JAXBException e) {
            throw new OLSException("JAXB error", e);
        }
        
        GeocodeResponseType     output = provider.geocode(input);
        Document                domResponse = null;
        
        try {
            Marshaller              marshaller = jaxbContext.createMarshaller();
            DocumentBuilderFactory  domFactory = DocumentBuilderFactory.newInstance();
            
            domFactory.setNamespaceAware(true);
            
            DocumentBuilder         domBuilder = domFactory.newDocumentBuilder();
            
            domResponse = domBuilder.newDocument();
            marshaller.marshal(output, domResponse);
        } catch (JAXBException e) {
            throw new OLSException("JAXB error", e);
        } catch (ParserConfigurationException e) {
            throw new OLSException("JAXP error", e);
        }
        
        return domResponse;
    }

}