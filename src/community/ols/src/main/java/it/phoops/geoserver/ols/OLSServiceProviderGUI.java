/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package it.phoops.geoserver.ols;

import java.util.Properties;

import org.apache.wicket.extensions.markup.html.tabs.ITab;

public interface OLSServiceProviderGUI {
    public abstract String getDescriptionKey();
    public abstract ITab getTab();
    public abstract void setPropertiesTab(ITab iTab);
    public abstract Properties getProperties();
}
