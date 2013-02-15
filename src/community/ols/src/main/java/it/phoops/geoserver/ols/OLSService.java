package it.phoops.geoserver.ols;

public enum OLSService {
    GEOCODING("1"),
    REVERSE_GEOCODING("2"),
    ROUTING_NAVIGATION("3");
    
    private String code;
    
    OLSService(String code) {
    	this.code = code;
    }
    
    @Override
    public String toString() {
    	return code;
    }
}
