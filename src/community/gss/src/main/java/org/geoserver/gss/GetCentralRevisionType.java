/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gss;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * The GetCentralRevision request
 * 
 * @author aaime
 */
public class GetCentralRevisionType extends GSSRequest {
    List<QName> typeName = new ArrayList<QName>();

    public List<QName> getTypeNames() {
        return typeName;
    }

    public void setTypeName(List<QName> typeName) {
        this.typeName = typeName;
    }

}
