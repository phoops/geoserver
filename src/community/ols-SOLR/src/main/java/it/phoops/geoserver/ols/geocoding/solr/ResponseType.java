package it.phoops.geoserver.ols.geocoding.solr;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ResponseType {
    GEOCODING_OK("0"), 
    UNKNOWN_ADDRESS("1"), 
    UNKNOWN_MUNICIPALITY("2"), 
    INPUT_PARAMETER_ERROR("3"), 
    AMBIGUOUS_MUNICIPALITY("6"), 
    AMBIGUOUS_ADDRESS("7"), 
    MUNICIPALITY_DATA_ONLY("8"), 
    DATA_ACCESS_ERROR("9");

    private static final Map<String, ResponseType> _lookup = new HashMap<String, ResponseType>();

    private String code;

    ResponseType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    static {
        for (ResponseType pivot : EnumSet.allOf(ResponseType.class)) {
            _lookup.put(pivot.getCode(), pivot);
        }
    }

    public static ResponseType get(String code) {
        return _lookup.get(code);
    }
}
