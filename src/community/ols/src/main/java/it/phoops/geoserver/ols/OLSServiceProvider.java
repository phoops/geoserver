package it.phoops.geoserver.ols;

import java.util.Properties;

public interface OLSServiceProvider {
    public abstract OLSService getServiceType();
    public abstract boolean isServiceActive();
}
