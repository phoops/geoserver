/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wcs.responses;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.geoserver.platform.ServiceException;
import org.geotools.coverage.grid.GridCoverage2D;

/**
 * Classes implementing this interface can encode coverages in one or more output formats
 * 
 * @author Alessio Fabiani, GeoSolutions SAS
 * @author Simone Giannecchini, GeoSolutions SAS
 */
public interface CoverageResponseDelegate {
    
    /**
     * Returns true if the specified output format is supported, false otherwise 
     * @param outputFormat
     * @return
     */
    boolean canProduce(String outputFormat);

    /**
     * Returns the content type for the specified output format
     * @param outputFormat
     * @return
     */
    String getMimeType(String outputFormat);

    /**
     * Returns an appropriate file extension for the coverages encoded with this delegate (used
     * mainly when storing the coverage on disk for later retrieval). For example a GeoTiff encoding
     * delegate might return "tif" (no period, just extension).
     * 
     * @return
     */
    String getFileExtension(String outputFormat);

    /**
     * Encodes the coverage in the specified output format onto the output stream
     * @param coverage
     * @param outputFormat
     * @param output
     * @throws ServiceException
     * @throws IOException
     */
    void encode(GridCoverage2D coverage, String outputFormat, OutputStream output) throws ServiceException, IOException;
    
    /**
     * Returns the list of output formats managed by this delegate
     * @return
     */
    List<String> getOutputFormats();
    
}
