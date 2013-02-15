package it.phoops.geoserver.ols.geocoding;

import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSServiceProvider;

import javax.xml.bind.JAXBElement;

import net.opengis.www.xls.GeocodeRequestType;
import net.opengis.www.xls.GeocodeResponseType;

public interface GeocodingServiceProvider extends OLSServiceProvider {
    public abstract JAXBElement<GeocodeResponseType> geocode(GeocodeRequestType input) throws OLSException;
}
