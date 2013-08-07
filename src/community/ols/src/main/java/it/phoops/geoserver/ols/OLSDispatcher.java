package it.phoops.geoserver.ols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.util.XMLCatalogResolver;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.impl.DefaultGeoServerFacade;
import org.geoserver.ows.LocalWorkspace;
import org.geoserver.ows.WorkspaceQualifyingCallback;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class OLSDispatcher extends AbstractController {
    private Map<String,OLSHandler>      handlers = new HashMap<String,OLSHandler>();
    private static final String         DEFAULT_WS = "default";
    
    public OLSDispatcher()
    {
    }
    
    public void setHandlers(Map<String,OLSHandler> handlers)
    {
        this.handlers = handlers;
    }
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws Exception {
        if (METHOD_POST.equals(httpRequest.getMethod())) {
            DocumentBuilderFactory      domFactory = DocumentBuilderFactory.newInstance();
            SchemaFactory               schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            schemaFactory.setResourceResolver(new XMLCatalogResolver(new String[]{getClass().getResource("/xsd/catalog.xml").toExternalForm()}));
            
            Schema                      olsSchema = schemaFactory.newSchema(getClass().getResource("/xsd/OLS/olsAll.xsd"));
            
            domFactory.setNamespaceAware(true);
            domFactory.setSchema(olsSchema);
            
            DocumentBuilder             domBuilder = domFactory.newDocumentBuilder();
            OLSDispatcherSAXErrorHandler   errorHandler = new OLSDispatcherSAXErrorHandler();
            
            domBuilder.setErrorHandler(errorHandler);
            
            Document                    domRequest = domBuilder.parse(httpRequest.getInputStream());
            
            if (!errorHandler.getError()) {
                XPath                       xPath = XPathFactory.newInstance().newXPath();
                XPathExpression             xPathExpr;
                
                httpResponse.setContentType("text/xml");
                
                String[] requestArray = httpRequest.getRequestURI().split("/");
                String workspaceName = "";
                if(requestArray.length > 3){
                    workspaceName = requestArray[3];
                }else{
                    workspaceName = DEFAULT_WS;
                }
                
                if(!workspaceName.equalsIgnoreCase(DEFAULT_WS)){
                    for (String path : handlers.keySet()) {
                        xPathExpr = xPath.compile(path);
                        
                        if ((Boolean)xPathExpr.evaluate(domRequest, XPathConstants.BOOLEAN)) {
                            OLSHandler          handler = handlers.get(path);
                            Boolean             wsFound = Boolean.FALSE;
                            // Set configured service provider for this service handler
                            DefaultGeoServerFacade dGeoServer = (DefaultGeoServerFacade)OLS.get().getFacade();
                            WorkspaceInfo wDefautl = OLS.get().getCatalog().getDefaultWorkspace();
                            OLSInfo olsInfo = null;
                            
                            
                            //Set the correct workspace
                            for (ServiceInfo sInfo : dGeoServer.getAllServices()) {
                                if(sInfo.getClass().equals(OLSInfoImpl.class)){
                                    if(sInfo.getWorkspace() != null 
                                            && sInfo.getWorkspace().getName().equalsIgnoreCase(workspaceName)){
                                        OLS.get().getCatalog().setDefaultWorkspace(sInfo.getWorkspace());
                                        wsFound = Boolean.TRUE;
                                        wDefautl = OLS.get().getCatalog().getDefaultWorkspace();
                                        break;
                                    }
                                }
                            }
                            
                            if(!wsFound){
                                logger.info("Error, NO Workspace Found!");
                                httpResponse.getWriter().print("noWorkspace");
                                
                                for (SAXException e : errorHandler.getExceptions()) {
                                    e.printStackTrace(httpResponse.getWriter());
                                }
                                
                                return null;
                            }
                            
                            //Checking the workspace
                            for (ServiceInfo sInfo : dGeoServer.getAllServices()) {
                                if(sInfo.getClass().equals(OLSInfoImpl.class)){
                                    if(sInfo.getWorkspace() != null 
                                            && sInfo.getWorkspace().getName() == wDefautl.getName()){
                                        olsInfo = (OLSInfo) sInfo;
                                        break;
                                    }else{
                                        olsInfo = null;
                                    }
                                }
                            }
                            
                            if(olsInfo != null){
                                OLSServiceProvider sProvidereToSet = null;
                                for (OLSServiceProvider sProvider : olsInfo.getServiceProvider()) {
                                    if(sProvider.getServiceType() == handler.getService()){
                                        sProvidereToSet = sProvider;
                                        break;
                                    }else{
                                        sProvidereToSet = OLS.get().getServiceProvider(handler.getService());
                                    }
                                }
                                handler.setActiveServiceProvider(sProvidereToSet);
                            }else{
                                handler.setActiveServiceProvider(OLS.get().getServiceProvider(handler.getService()));
                            }
                            
                            Document            domResponse = handler.processRequest(domRequest);
                            TransformerFactory  transFactory = TransformerFactory.newInstance();
                            Transformer         transformer = transFactory.newTransformer();
                            
//                            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.transform(new DOMSource(domResponse.getFirstChild()), new StreamResult(httpResponse.getOutputStream()));
                            break;
                        }
                    }
                }else{
                    //Loading the the default configuration - without workspace
                    for (String path : handlers.keySet()) {
                        xPathExpr = xPath.compile(path);
                        
                        if ((Boolean)xPathExpr.evaluate(domRequest, XPathConstants.BOOLEAN)) {
                            OLSHandler handler = handlers.get(path);
                            
                            // Set configured service provider for this service handler
                            handler.setActiveServiceProvider(OLS.get().getServiceProvider(handler.getService()));
                            
                            Document domResponse = handler.processRequest(domRequest);
                            TransformerFactory transFactory = TransformerFactory.newInstance();
                            Transformer transformer = transFactory.newTransformer();
                            
    //                      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.transform(new DOMSource(domResponse.getFirstChild()), new StreamResult(httpResponse.getOutputStream()));
                            break;
                        }
                    }
                }
                
                
            } else {
                httpResponse.getWriter().println("Error parsing XML payload!");
                
                for (SAXException e : errorHandler.getExceptions()) {
                    e.printStackTrace(httpResponse.getWriter());
                }
            }
        } else {
            httpResponse.getWriter().println("Invoke Open LS service via POST method!");
        }
        
        return null;
    }
    
    class OLSDispatcherSAXErrorHandler implements ErrorHandler {
        private boolean                 error = false;
        private List<SAXParseException> exceptions = new ArrayList<SAXParseException>();

        @Override
        public void error(SAXParseException arg0) throws SAXException {
            error = true;
            exceptions.add(arg0);
        }

        @Override
        public void fatalError(SAXParseException arg0) throws SAXException {
            error = true;
            exceptions.add(arg0);
        }

        @Override
        public void warning(SAXParseException arg0) throws SAXException {
            exceptions.add(arg0);
        }
        
        public boolean getError() {
            return error;
        }
        
        public List<SAXParseException> getExceptions() {
            return exceptions;
        }
    }
}
