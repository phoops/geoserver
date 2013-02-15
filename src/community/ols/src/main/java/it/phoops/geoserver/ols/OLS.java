package it.phoops.geoserver.ols;


import org.geoserver.config.GeoServer;
import org.geoserver.platform.GeoServerExtensions;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A facade providing access to the OLS configuration details
 * 
 */
public class OLS implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private final GeoServer geoserver;

    public OLS(GeoServer geoserver) {
        this.geoserver = geoserver;
    }

//    public Catalog getCatalog() {
//        return geoserver.getCatalog();
//    }
//
    public OLSInfo getServiceInfo() {
        return geoserver.getService(OLSInfo.class);
    }

//    public Style getStyleByName(String styleName) throws IOException {
//        StyleInfo styleInfo = getCatalog().getStyleByName(styleName);
//        return styleInfo == null ? null : styleInfo.getStyle();
//    }
//
//    public LayerInfo getLayerByName(String layerName) {
//        return getCatalog().getLayerByName(layerName);
//    }
//
//    public LayerGroupInfo getLayerGroupByName(String layerGroupName) {
//        return getCatalog().getLayerGroupByName(layerGroupName);
//    }
//
    public boolean isEnabled() {
        OLSInfo serviceInfo = getServiceInfo();
        return serviceInfo.isEnabled();
    }

    public OLSServiceProvider getServiceProvider(OLSService service) {
        return this.geoserver.getService(OLSInfo.class).findServiceActive(service);
    }
    
    public GeoServer getGeoServer() {
        return this.geoserver;
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(final ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public static ApplicationContext getApplicationContext(){
    	return applicationContext;
    }

    public static OLS get() {
        return GeoServerExtensions.bean(OLS.class);
    }
}

