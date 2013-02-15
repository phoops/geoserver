package it.phoops.geoserver.ols.routing.pgrouting.component;

import org.apache.wicket.model.IModel;

public class PgRoutingTabFactory {
    private static PgRoutingTabFactory         factory = null;
    private PgRoutingTab                        instance = null;
    
    public static PgRoutingTabFactory getPgRoutingTabFactory() {
        if (factory == null) {
            factory = new PgRoutingTabFactory();
        }
        return factory;
    }
    
    public PgRoutingTab getPgRoutingTab(IModel<String> title) {
        if (instance == null) {
            instance = new PgRoutingTab(title);
        }
        return instance;
    }
}
