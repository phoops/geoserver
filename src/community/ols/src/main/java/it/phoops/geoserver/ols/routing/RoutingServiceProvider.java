package it.phoops.geoserver.ols.routing;

import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSServiceProvider;

import javax.xml.bind.JAXBElement;

import net.opengis.www.xls.DetermineRouteRequestType;
import net.opengis.www.xls.DetermineRouteResponseType;

public interface RoutingServiceProvider extends OLSServiceProvider {
    public abstract JAXBElement<DetermineRouteResponseType> geocode(DetermineRouteRequestType input) throws OLSException;
}
