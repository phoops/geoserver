package it.phoops.geoserver.ols.routing.pgrouting;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Algorithm {
    DIJKSTRA("1"),
    A_STAR("2"),
    SHOOTING_STAR("3");
    
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
