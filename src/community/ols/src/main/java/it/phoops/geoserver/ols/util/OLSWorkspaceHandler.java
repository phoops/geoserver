package it.phoops.geoserver.ols.util;

import javax.xml.bind.JAXBContext;

import net.opengis.www.xls.GeocodeRequestType;

import org.w3c.dom.Document;

import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSHandler;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.OLSServiceProvider;

public class OLSWorkspaceHandler implements OLSHandler{

    @Override
    public OLSService getService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setServiceProvider(OLSServiceProvider provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setActiveServiceProvider(OLSServiceProvider provider) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Document processRequest(Document request) throws OLSException {
        JAXBContext             jaxbContext = null;
        GeocodeRequestType      input = null;
        
        
        return null;
    }

}
