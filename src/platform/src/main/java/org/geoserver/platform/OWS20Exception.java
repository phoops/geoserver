/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.platform;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Class for exceptions generated by an OWS2.0 service.
 * <p>
 * It adds support to HTTP return codes.
 * </p>
 *
 * @author Emanuele Tajariol - GeoSolutions
 */
public class OWS20Exception extends ServiceException {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(OWS20Exception.class);

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 7254349181794561724L;

    public static class OWSExceptionCode {

        private final static Map<String, OWSExceptionCode> codes = new HashMap<String, OWSExceptionCode>();

        public final static OWSExceptionCode OperationNotSupported = new OWSExceptionCode("OperationNotSupported", 501, "Not Implemented");
        public final static OWSExceptionCode MissingParameterValue = new OWSExceptionCode("MissingParameterValue", 400, "Bad request");
        public final static OWSExceptionCode InvalidParameterValue = new OWSExceptionCode("InvalidParameterValue", 400, "Bad request");
        public final static OWSExceptionCode VersionNegotiationFailed = new OWSExceptionCode("VersionNegotiationFailed", 400, "Bad request");
        public final static OWSExceptionCode InvalidUpdateSequence = new OWSExceptionCode("InvalidUpdateSequence", 400, "Bad request");
        public final static OWSExceptionCode OptionNotSupported = new OWSExceptionCode("OptionNotSupported", 501, "Not Implemented");
        public final static OWSExceptionCode NoApplicableCode = new OWSExceptionCode("NoApplicableCode", 500, "Not Implemented");
        public final static OWSExceptionCode InvalidCoverageType = new OWSExceptionCode("InvalidCoverageType", 404, "Coverage addressed is not a grid coverage");

        private final String exceptionCode;
        private final Integer httpCode;
        private final String httpMessage;

        public OWSExceptionCode(String exceptionCode) {
            this(exceptionCode, null, null);
        }

        public OWSExceptionCode(String exceptionCode, Integer httpCode) {
            this(exceptionCode, httpCode, null);
        }

        protected OWSExceptionCode(String exceptionCode, Integer httpCode, String message) {
            this.exceptionCode = exceptionCode;
            this.httpCode = httpCode;
            this.httpMessage = message;

            OWSExceptionCode old = codes.put(exceptionCode, this);

            if(old != null) {
                LOGGER.warning("Replacing exception code " + old + " with " + this);
            }
        }

        public String getExceptionCode() {
            return exceptionCode;
        }

        public Integer getHttpCode() {
            return httpCode;
        }

        public String getHttpMessage() {
            return httpMessage;
        }

        public static OWSExceptionCode getByCode(String code) {
            return codes.get(code);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[exCode:"+ exceptionCode + " httpCode=" + httpCode + " httpMessage=" + httpMessage + ']';
        }
    }
    /**
     * HTTP response code. May be null when not specified.
     */
    protected Integer httpCode;

    /**
     * Constructs the exception from a message.
     *
     * @param message The message describing the exception.
     */
    public OWS20Exception(String message) {
        super(message);
    }

    /**
     * Constructs the exception from a message and causing exception.
     *
     * @param message The message describing the exception.
     * @param cause The case of the exception.
     */
    public OWS20Exception(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs the exception from a message, causing exception, and code.
     *
     * @param message The message describing the exception.
     * @param cause The case of the exception.
     * @param code The application specific exception code for the exception.
     */
    public OWS20Exception(String message, Throwable cause, String code) {
        this(message, cause);
    }

    /**
     * Constructs the exception from a message, causing exception, code, and locator.
     *
     * @param message The message describing the exception.
     * @param cause The case of the exception.
     * @param code The application specific exception code for the exception.
     * @param locator The application specific locator for the exception.
     */
    protected OWS20Exception(String message, Throwable cause, String code, String locator) {
        super(message, cause, code);
    }

    public OWS20Exception(String message, Throwable cause, OWSExceptionCode code, String locator) {
        super(message, cause, code.getExceptionCode());
        setHttpCode(code.getHttpCode());
    }

    /**
     * Constructs the exception from a message, and code.
     *
     * @param message The message describing the exception.
     * @param code The application specific exception code for the exception.
     */
    protected OWS20Exception(String message, String code) {
        super(message);
    }

    public OWS20Exception(String message, OWSExceptionCode code) {
        super(message, code.getExceptionCode());
        setHttpCode(code.getHttpCode());
    }

    /**
     * Constructs the exception from a message,code, and locator.
     *
     * @param message The message describing the exception.
     * @param code The application specific exception code for the exception.
     * @param locator The application specific locator for the exception.
     */
    protected OWS20Exception(String message, String code, String locator) {
        super(message, code);
    }

    public OWS20Exception(String message, OWSExceptionCode code, String locator) {
        super(message, code.getExceptionCode(), locator);
        setHttpCode(code.getHttpCode());
    }

    /**
     * Constructs the exception from a causing exception.
     *
     * @param cause The case of the exception.
     */
    public OWS20Exception(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs the exception from causing exception, and code.
     *
     * @param cause The case of the exception.
     * @param code The application specific exception code for the exception.
     */
    protected OWS20Exception(Throwable cause, String code) {
        super(cause);
    }

    /**
     * Constructs the exception from a causing exception, code, and locator.
     *
     * @param cause The case of the exception.
     * @param code The application specific exception code for the exception.
     * @param locator The application specific locator for the exception.
     */
    protected OWS20Exception(Throwable cause, String code, String locator) {
        super(cause, code);
    }

    public OWS20Exception(Throwable cause, OWSExceptionCode code, String locator) {
        super(cause, code.getExceptionCode());
        setHttpCode(code.getHttpCode());
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
    }

    @Override
    public String toString() {
        String msg = super.toString();

        if (httpCode == null) {
            return msg;
        } else {
            return msg + NEW_LINE + "HTTPcode:" + httpCode;
        }
    }
}
