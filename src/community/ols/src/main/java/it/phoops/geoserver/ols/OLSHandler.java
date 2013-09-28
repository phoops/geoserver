package it.phoops.geoserver.ols;

import javax.xml.bind.JAXBElement;

import net.opengis.www.xls.AbstractResponseParametersType;
import net.opengis.www.xls.RequestType;

public interface OLSHandler {
    public abstract OLSService getService();
    public abstract void setServiceProvider(OLSServiceProvider provider);
    public abstract void setActiveServiceProvider(OLSServiceProvider provider);
    public abstract JAXBElement<? extends AbstractResponseParametersType> processRequest(RequestType request, String lang, String srsName) throws OLSException;
}
