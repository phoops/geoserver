package it.phoops.geoserver.ols;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geoserver.config.ServiceInfo;
import org.geoserver.config.impl.DefaultGeoServerFacade;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class OLSDispacherWS extends AbstractController{
    private Map<String,OLSHandler>      handlers = new HashMap<String,OLSHandler>();
    
    public OLSDispacherWS(){}
    
    public void setHandlers(Map<String,OLSHandler> handlers)
    {
        this.handlers = handlers;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws Exception {
        String                      list = "";
        if (METHOD_GET.equals(httpRequest.getMethod())) {
//            DocumentBuilderFactory      domFactory = DocumentBuilderFactory.newInstance();
//            SchemaFactory               schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//            ArrayList<String>           wsList = new ArrayList<String>();
            
            
            DefaultGeoServerFacade dGeoServer = (DefaultGeoServerFacade)OLS.get().getFacade();
            int index = 1;
            for (ServiceInfo sInfo : dGeoServer.getAllServices()) {
                if(sInfo.getClass().equals(OLSInfoImpl.class)){
                    if(sInfo.getWorkspace() != null){
                        if(index == dGeoServer.getAllServices().size()){
                            list+=sInfo.getWorkspace().getName();
                        }else{
                            list+=sInfo.getWorkspace().getName()+";";
                        }
                        index++;
                    }
                }
            }
        }
        
        httpResponse.getWriter().print(list);
        
        return null;
    }



}
