package it.phoops.geoserver.ols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geoserver.config.impl.ServiceInfoImpl;

/**
 * 
 * Default implementation for the {@link OLSInfo} bean.
 * 
 * @author aCasini
 * 
 */
public class OLSInfoImpl extends ServiceInfoImpl implements OLSInfo {
    private List<OLSServiceProvider>  servicesProviders = new ArrayList<OLSServiceProvider>();
    
    @Override
    public List<OLSServiceProvider> getServiceProvider() {
    	if(servicesProviders == null)
			servicesProviders = new ArrayList<OLSServiceProvider>();
    	return servicesProviders;
    }

    @Override
    public void setServiceProvider(OLSServiceProvider provider) {
    	if(servicesProviders == null)
			servicesProviders = new ArrayList<OLSServiceProvider>();
        servicesProviders.add(provider);
    }
    
    @Override
    public void addServiceProvide(OLSServiceProvider provider){
        boolean exist = false;
        OLSServiceProvider toRemove = null;
        for (OLSServiceProvider element : servicesProviders) {
            if(element.getClass() == provider.getClass()){
                exist = true;
                toRemove = element;
                break;
            }
        }
        
        if(exist){
           servicesProviders.remove(toRemove);
           servicesProviders.add(provider);
        }else{
            servicesProviders.add(provider);
        }
    }
    
    @Override
    public OLSServiceProvider findServiceNotActive(OLSAbstractServiceProvider provider, OLSService service){
        OLSServiceProvider serviceFound = null;
        for (OLSServiceProvider serviceProvider : getServiceProvider()) {
           if(serviceProvider.getServiceType() == service
                   && serviceProvider.getClass() == provider.getClass()
                   && !(Boolean.parseBoolean(((OLSAbstractServiceProvider)serviceProvider).getProperties().getProperty("OLS.serviceProvider.service.active")))){
               serviceFound = serviceProvider;
               break;
           }
        }
        return serviceFound;
    }
    
    @Override
    public OLSServiceProvider findServiceActive(OLSService service) {
        OLSServiceProvider serviceFound = null;
        for (OLSServiceProvider serviceProvider : getServiceProvider()) {
           if(serviceProvider.getServiceType() == service
                   && (Boolean.parseBoolean(((OLSAbstractServiceProvider)serviceProvider).getProperties().getProperty("OLS.serviceProvider.service.active")))){
               serviceFound = serviceProvider;
               break;
           }
        }
        return serviceFound;
    }
}
