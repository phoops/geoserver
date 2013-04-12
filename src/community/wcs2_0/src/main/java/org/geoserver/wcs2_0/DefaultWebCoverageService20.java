/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wcs2_0;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.opengis.wcs20.DescribeCoverageType;
import net.opengis.wcs20.GetCapabilitiesType;
import net.opengis.wcs20.GetCoverageType;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.platform.OWS20Exception;
import org.geoserver.wcs.WCSInfo;
import org.geoserver.wcs.responses.CoverageResponseDelegateFinder;
import org.geoserver.wcs2_0.exception.WCS20Exception;
import org.geoserver.wcs2_0.response.MIMETypeMapper;
import org.geoserver.wcs2_0.response.WCS20DescribeCoverageTransformer;
import org.geoserver.wcs2_0.util.EnvelopeAxesLabelsMapper;
import org.geoserver.wcs2_0.util.NCNameResourceCodec;
import org.geoserver.wcs2_0.util.StringUtils;
import org.geotools.util.logging.Logging;
import org.geotools.xml.transform.TransformerBase;
import org.opengis.coverage.grid.GridCoverage;

/**
 * Default implementation of the Web Coverage Service 2.0
 * 
 * @author Emanuele Tajariol (etj) - GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class DefaultWebCoverageService20 implements WebCoverageService20 {

    protected Logger LOGGER = Logging.getLogger(DefaultWebCoverageService20.class);
    
    private MIMETypeMapper mimemapper;

    private Catalog catalog;

    private GeoServer geoServer;

    private CoverageResponseDelegateFinder responseFactory;
    
    /** Utility class to map envelope dimension*/
    private EnvelopeAxesLabelsMapper envelopeAxesMapper;

    public DefaultWebCoverageService20(GeoServer geoServer, CoverageResponseDelegateFinder responseFactory, EnvelopeAxesLabelsMapper envelopeDimensionsMapper,MIMETypeMapper mimemappe) {
        this.geoServer = geoServer;
        this.catalog = geoServer.getCatalog();
        this.responseFactory = responseFactory;
        this.envelopeAxesMapper=envelopeDimensionsMapper;
        this.mimemapper=mimemappe;
    }
    
    @Override
    public WCSInfo getServiceInfo() {
        return geoServer.getService(WCSInfo.class);
    }


    @Override
    public TransformerBase getCapabilities(GetCapabilitiesType request) {
        checkService(request.getService());

        return new GetCapabilities(getServiceInfo(),responseFactory).run(request);
    }

    @Override
    public WCS20DescribeCoverageTransformer describeCoverage(DescribeCoverageType request) {
        checkService(request.getService());
        checkVersion(request.getVersion());
        
        if( request.getCoverageId() == null || request.getCoverageId().isEmpty() ) {
            throw new OWS20Exception("Required parameter coverageId missing", WCS20Exception.WCS20ExceptionCode.EmptyCoverageIdList, "coverageId");
        }
        
        // check coverages are legit
        List<String> badCoverageIds = new ArrayList<String>();

        for (String encodedCoverageId : (List<String>)request.getCoverageId()) {
            LayerInfo layer = NCNameResourceCodec.getCoverage(catalog, encodedCoverageId);
            if(layer == null) {
                badCoverageIds.add(encodedCoverageId);
            }
        }
        if(!badCoverageIds.isEmpty()) {
            String mergedIds = StringUtils.merge(badCoverageIds);
            throw new WCS20Exception("Could not find the requested coverage(s): " + mergedIds
                    , WCS20Exception.WCS20ExceptionCode.NoSuchCoverage, "coverageId");
        }

        WCSInfo wcs = getServiceInfo();

        WCS20DescribeCoverageTransformer describeTransformer = new WCS20DescribeCoverageTransformer(wcs, catalog, responseFactory,envelopeAxesMapper,mimemapper);
        describeTransformer.setEncoding(Charset.forName(wcs.getGeoServer().getSettings().getCharset()));
        return describeTransformer;
    }

    @Override
    public GridCoverage getCoverage(GetCoverageType request) {
        checkService(request.getService());
        checkVersion(request.getVersion());

        if( request.getCoverageId() == null || "".equals(request.getCoverageId()) ) {
            throw new OWS20Exception("Required parameter coverageId missing", WCS20Exception.WCS20ExceptionCode.EmptyCoverageIdList, "coverageId");
        }
        
        return new GetCoverage(getServiceInfo(), catalog,envelopeAxesMapper).run(request);
    }

    private void checkVersion(String version) {
        if(version == null) {
            throw new WCS20Exception("Missing version", OWS20Exception.OWSExceptionCode.MissingParameterValue, version);
        }

        if ( ! WCS20Const.V201.equals(version) && ! WCS20Const.V20.equals(version)) {
            throw new WCS20Exception("Could not understand version:" + version);
        }
    }

    private void checkService(String serviceName) {
        if( serviceName == null ) {
            throw new WCS20Exception("Missing service name", OWS20Exception.OWSExceptionCode.MissingParameterValue, "service");
        }
        if( ! "WCS".equals(serviceName)) {
            throw new WCS20Exception("Error in service name, epected value: WCS", OWS20Exception.OWSExceptionCode.InvalidParameterValue, serviceName);
        }
    }

}
