package it.phoops.geoserver.ols;

import java.util.ArrayList;
import java.util.List;

import org.geoserver.config.ServiceInfo;

/**
 * Configuration object for Open Location Service.
 * 
 * @author aCasini
 * 
 */
public interface OLSInfo extends ServiceInfo {
        public abstract List<OLSServiceProvider> getServiceProvider();
        public abstract void setServiceProvider(OLSServiceProvider provider);
        public void addServiceProvide(OLSServiceProvider provider);
        public OLSServiceProvider findServiceNotActive(OLSAbstractServiceProvider provider, OLSService service);
        public OLSServiceProvider findServiceActive(OLSService service);
}
