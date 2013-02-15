package it.phoops.geoserver.ols;

import java.util.Properties;

import org.apache.wicket.extensions.markup.html.tabs.ITab;

public interface OLSServiceProviderGUI {
    public abstract String getDescriptionKey();
    public abstract ITab getTab();
    public abstract void setPropertiesTab(ITab iTab);
    public abstract Properties getProperties();
}
