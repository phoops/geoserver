package it.phoops.geoserver.ols.geocoding.rfc59;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum DataSource {
    REGIONE_TOSCANA("1"),
    OTHER("2");

    private static final Map<String,DataSource> _lookup = new HashMap<String,DataSource>();
    private String code;

    DataSource(String code) {
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
        for (DataSource pivot : EnumSet.allOf(DataSource.class)) {
            _lookup.put(pivot.getCode(), pivot);
        }
    }
    
    public static DataSource get(String code) {
        return _lookup.get(code);
    }
}