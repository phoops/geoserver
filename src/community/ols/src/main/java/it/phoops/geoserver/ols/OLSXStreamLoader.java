package it.phoops.geoserver.ols;

import java.util.ArrayList;

import org.geoserver.config.ConfigurationListener;
import org.geoserver.config.GeoServer;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamServiceLoader;
import org.geoserver.platform.GeoServerResourceLoader;

/**
 * Loads and persist the {@link OLSInfo} object to and from xstream persistence.
 * 
 * @author aCasini
 * 
 */
public class OLSXStreamLoader extends XStreamServiceLoader<OLSInfo> {

    public OLSXStreamLoader(GeoServerResourceLoader resourceLoader) {
        super(resourceLoader, "ols");

    }

    @Override
    public Class<OLSInfo> getServiceClass() {
        return OLSInfo.class;
    }

    @Override
    protected OLSInfo createServiceFromScratch(GeoServer gs0) {
        OLSInfoImpl ols = new OLSInfoImpl();
        ols.setName("OLS");

        return ols;
    }

    @Override
    protected void initXStreamPersister(XStreamPersister xp, GeoServer gs) {
        super.initXStreamPersister(xp, gs);
        xp.getXStream().alias("ols", OLSInfo.class, OLSInfoImpl.class);
    }

    @Override
    protected OLSInfo initialize(OLSInfo service) {
        super.initialize(service);
        if (service.getExceptionFormats() == null) {
            ((OLSInfoImpl) service).setExceptionFormats(new ArrayList<String>());
        }
        // if (service.getVersions().isEmpty()) {
        // service.getVersions().add(new Version("1.0.0"));
        // service.getVersions().add(new Version("1.1.1"));
        // }
        // Version v201 = new Version("2.0.1");
        // if(!service.getVersions().contains(v201)) {
        // service.getVersions().add(v201);
        // }
        return service;
    }

}
