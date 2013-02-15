package it.phoops.geoserver.ols.routing.pgrouting;

import it.phoops.geoserver.ols.OLSAbstractServiceProvider;
import it.phoops.geoserver.ols.OLSException;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.routing.RoutingServiceProvider;
import it.phoops.geoserver.ols.routing.pgrouting.component.PgRoutingTab;
import it.phoops.geoserver.ols.routing.pgrouting.component.PgRoutingTabFactory;

import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBElement;

import net.opengis.www.xls.DetermineRouteRequestType;
import net.opengis.www.xls.DetermineRouteResponseType;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.config.ServiceInfo;

public class PgRoutingServiceProvider extends OLSAbstractServiceProvider implements RoutingServiceProvider {
    //Properties Name
    private static final String  PN_ENDPOINT_ADDRESS = "OLS.serviceProvider.geocoding.pgrouting.service.endpointAddress";
    private static final String  PN_PORT_NUMBER = "OLS.serviceProvider.geocoding.pgrouting.service.portNumber";
    private static final String  PN_DATABASE = "OLS.serviceProvider.geocoding.pgrouting.service.database";
    private static final String  PN_USER = "OLS.serviceProvider.geocoding.pgrouting.service.user";
    private static final String  PN_PASSWORD = "OLS.serviceProvider.geocoding.pgrouting.service.password";
    private static final String  PN_ACTIVE_SERVICE = "OLS.serviceProvider.service.active";
    private static final String  PN_PGROUTING_ALGORITHM ="OLS.serviceProvider.geocoding.pgrouting.service.shortest.algorithm";
    
    private String      descriptionKey;
    private Properties  properties = new Properties();
    
    public String getEndpointAddress() {
        return properties.getProperty(PN_ENDPOINT_ADDRESS);
    }

    public void setEndpointAddress(String endpointAddress) {
        properties.setProperty(PN_ENDPOINT_ADDRESS, endpointAddress);
    }
    
    public String getPortNumber() {
        return properties.getProperty(PN_PORT_NUMBER);
    }

    public void setPortNumber(String portNumber) {
        properties.setProperty(PN_PORT_NUMBER, portNumber);
    }
    
    public String getDatabase() {
        return properties.getProperty(PN_DATABASE);
    }

    public void setDatabase(String database) {
        properties.setProperty(PN_DATABASE, database);
    }
    
    public String getUser() {
        return properties.getProperty(PN_USER);
    }

    public void setUser(String user) {
        properties.setProperty(PN_USER, user);
    }
    
    public String getPassword() {
        return properties.getProperty(PN_PASSWORD);
    }

    public void setPassword(String password) {
        properties.setProperty(PN_PASSWORD, password);
    }
    
    public String getAlgorithm() {
        return properties.getProperty(PN_PGROUTING_ALGORITHM);
    }

    public void setAlgorithm(String algorithm) {
        properties.setProperty(PN_PGROUTING_ALGORITHM, algorithm);
    }
    
    public String getActive(){
        return properties.getProperty(PN_ACTIVE_SERVICE);
    }
    
    public void setActive(String activeService){
        properties.setProperty(PN_ACTIVE_SERVICE, activeService);
    }
    
    @Override
    public String getDescriptionKey() {
        return descriptionKey;
    }
    
    public void setDescriptionKey(String description) {
        this.descriptionKey = description;
    }
    
    @Override
    public Properties getProperties() {
        return properties;
    }
    
    @Override
    public OLSService getServiceType() {
        return OLSService.ROUTING_NAVIGATION;
    }
    @Override
    public ITab getTab() {
        IModel<String> title = new ResourceModel("pgRouting", "pgRouting");
        return PgRoutingTabFactory.getPgRoutingTabFactory().getPgRoutingTab(title);
    }
    @Override
    public void handleServiceChange(ServiceInfo service,
            List<String> propertyNames, List<Object> oldValues,
            List<Object> newValues) {
        String host = ((PgRoutingTab)getTab()).getHostPgRouting();
        if(host == null)
            host = "";
        String port = ((PgRoutingTab)getTab()).getPortPgRouting();
        if(port == null)
            port = "";
        String db = ((PgRoutingTab)getTab()).getDbPgRouting();
        if(db == null)
            db = "";
        String user = ((PgRoutingTab)getTab()).getUserPgRouting();
        if(user == null)
            user = "";
        String psw = ((PgRoutingTab)getTab()).getPswPgRouting();
        if(psw == null)
            psw = "";
        String active = ((PgRoutingTab)getTab()).getActivePgRouting();
        
        String algorithm = ((PgRoutingTab)getTab()).getSelectedAlgorithm().getCode();
        
        
        setActive(active);
        setEndpointAddress(host);
        setPortNumber(port);
        setDatabase(db);
        setUser(user);
        setPassword(psw);
        setAlgorithm(algorithm);
        
    }
    
    @Override
    public boolean isServiceActive() {
        return Boolean.parseBoolean(this.getActive());
    }
    @Override
    public void setPropertiesTab(ITab pgRoutingTab) {
        ((PgRoutingTab)pgRoutingTab).setActivePgRouting(this.getActive());
        ((PgRoutingTab)pgRoutingTab).setHostPgRouting(this.getEndpointAddress());
        ((PgRoutingTab)pgRoutingTab).setPortPgRouting(this.getPortNumber());
        ((PgRoutingTab)pgRoutingTab).setDbPgRouting(this.getDatabase());
        ((PgRoutingTab)pgRoutingTab).setUserPgRouting(this.getUser());
        ((PgRoutingTab)pgRoutingTab).setPswPgRouting(this.getPassword());
        Algorithm algorithm = Algorithm.get(this.getAlgorithm());
        ((PgRoutingTab)pgRoutingTab).setCodeAlgorithmSelected(Integer.parseInt(algorithm.getCode()));
    }

    @Override
    public JAXBElement<DetermineRouteResponseType> geocode(
            DetermineRouteRequestType input) throws OLSException {
        // TODO Auto-generated method stub
        System.out.println("-- Chiamato servizio pgRouting");
        return null;
    }
}
