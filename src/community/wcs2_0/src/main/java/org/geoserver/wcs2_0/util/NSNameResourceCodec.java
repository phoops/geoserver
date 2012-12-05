/* Copyright (c) 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wcs2_0.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geotools.util.MapEntry;
import org.geotools.util.logging.Logging;

/**
 * De/encode a workspace and a resource name into a single string.
 * <p/>
 * Some external formats do not allow to use semicolons in some strings.
 * This class offers methods to encode and decode workspace and names into a single string
 * without using semicolons.
 * <p/>
 * We simply use a "__" as separator. This should reduce the conflicts with existing underscores.
 * This encoding is not unique, so the {@link #decode(java.lang.String) decode} method
 * return a list of possibile workspace,name combinations. You'll need to check which workspace
 * is really existing.
 * <p/>
 * You may use the {@link #getLayer(org.geoserver.catalog.Catalog, java.lang.String) getLayer()} method
 * to just retrieve the matching layers.
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class NSNameResourceCodec {
    protected static Logger LOGGER = Logging.getLogger(NSNameResourceCodec.class);


    private final static String DELIMITER = String.valueOf("__");

    public static String encode(ResourceInfo resource) {
        return encode(resource.getNamespace().getPrefix(), resource.getName());
    }

    public static String encode(String workspaceName, String resourceName) {
        return workspaceName + DELIMITER + resourceName;
    }


    /**
     * Search in the catalog the Layers matching the encoded id.
     * <p/>
     *
     * @return A possibly empty list of the matching layers, or null if the encoded id could not be decoded.
     */
    public static List<LayerInfo> getLayers(Catalog catalog, String encodedResourceId) {
        List<MapEntry<String, String>> decodedList = decode(encodedResourceId);
        if(decodedList.isEmpty()) {
            LOGGER.info("Could not decode id '"+encodedResourceId+"'");
            return null;
        }

        List<LayerInfo> ret = new ArrayList<LayerInfo>();

        for (MapEntry<String, String> mapEntry : decodedList) {
            String namespace = mapEntry.getKey();
            String covName = mapEntry.getValue();
            NamespaceInfo nsInfo = catalog.getNamespaceByPrefix(namespace);
            if(nsInfo != null) {
                LayerInfo layer = catalog.getLayerByName(namespace+":"+covName);
                if(layer != null)
                    ret.add(layer);
            }
        }

        LOGGER.info("Could not find layer with id '"+encodedResourceId+"'");
        return ret;
    }

    /**
     *
     * @return a List of possibile workspace/name pairs, possibly empty if the input could not be decoded;
     */
    public static List<MapEntry<String,String>> decode(String qualifiedName) {
        int lastPos = qualifiedName.lastIndexOf(DELIMITER);

        if( lastPos == -1)
            return Collections.EMPTY_LIST;

        List<MapEntry<String,String>> ret = new ArrayList<MapEntry<String, String>>();
        while (lastPos > -1) {
            String ws   = qualifiedName.substring(0, lastPos);
            String name = qualifiedName.substring(lastPos+DELIMITER.length());
            ret.add(new MapEntry<String, String>(ws, name));
            lastPos = qualifiedName.lastIndexOf(DELIMITER, lastPos-1);
        }
        return ret;
    }

}
