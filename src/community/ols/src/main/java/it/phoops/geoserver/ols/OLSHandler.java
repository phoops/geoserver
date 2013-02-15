package it.phoops.geoserver.ols;

import org.w3c.dom.Document;

public interface OLSHandler {
    public abstract OLSService getService();
    public abstract void setServiceProvider(OLSServiceProvider provider);
    public abstract void setActiveServiceProvider(OLSServiceProvider provider);
    public abstract Document processRequest(Document request) throws OLSException;
}
