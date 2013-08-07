package it.phoops.geoserver.ols;


public interface OLSServiceProvider {
    public abstract OLSService getServiceType();
    public abstract boolean isServiceActive();
}
