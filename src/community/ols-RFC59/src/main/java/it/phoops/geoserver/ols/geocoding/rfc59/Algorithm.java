package it.phoops.geoserver.ols.geocoding.rfc59;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Algorithm {
    TERM_QUERIES("1"),
    FUZZY_QUERIES("2"),
    RADIX("3");

    private static final Map<String,Algorithm> _lookup = new HashMap<String,Algorithm>();
    private String code;

    Algorithm(String code) {
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
        for (Algorithm pivot : EnumSet.allOf(Algorithm.class)) {
            _lookup.put(pivot.getCode(), pivot);
        }
    }
    
    public static Algorithm get(String code) {
        return _lookup.get(code);
    }
}