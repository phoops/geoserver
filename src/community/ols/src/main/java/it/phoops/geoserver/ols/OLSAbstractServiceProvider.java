/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package it.phoops.geoserver.ols;

import java.util.List;
import java.util.Properties;

import org.geoserver.config.ConfigurationListener;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;

public abstract class OLSAbstractServiceProvider implements OLSServiceProvider,
        OLSServiceProviderGUI, ConfigurationListener {
    public static final double  DEGREES_TO_RADIANS_FACTOR = Math.PI / 180.0;
    public static final double  EARTH_RADIUS = 6378137.0;
    public static final double  DEGREES_TO_METERS_FACTOR = DEGREES_TO_RADIANS_FACTOR * EARTH_RADIUS;
    
    private String descriptionKey;

    protected Properties properties = new Properties();

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
    public void handleGlobalChange(GeoServerInfo global, List<String> propertyNames,
            List<Object> oldValues, List<Object> newValues) {
    }

    @Override
    public void handlePostGlobalChange(GeoServerInfo global) {
    }

    @Override
    public void handleSettingsAdded(SettingsInfo settings) {
    }

    @Override
    public void handleSettingsModified(SettingsInfo settings, List<String> propertyNames,
            List<Object> oldValues, List<Object> newValues) {
    }

    @Override
    public void handleSettingsPostModified(SettingsInfo settings) {
    }

    @Override
    public void handleSettingsRemoved(SettingsInfo settings) {
    }

    @Override
    public void handleLoggingChange(LoggingInfo logging, List<String> propertyNames,
            List<Object> oldValues, List<Object> newValues) {
    }

    @Override
    public void handlePostLoggingChange(LoggingInfo logging) {
    }

    @Override
    public void handlePostServiceChange(ServiceInfo service) {
    }

    @Override
    public void handleServiceRemove(ServiceInfo service) {
    }

    @Override
    public void reloaded() {
    }
}