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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class OLSDispatcher extends AbstractController {
    private Map<String,OLSHandler>  handlers = new HashMap<String,OLSHandler>();
    
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
                
                for (String path : handlers.keySet()) {
                    xPathExpr = xPath.compile(path);
                    
                    if ((Boolean)xPathExpr.evaluate(domRequest, XPathConstants.BOOLEAN)) {
                        OLSHandler          handler = handlers.get(path);
                        
                        // Set configured service provider for this service handler
                        handler.setActiveServiceProvider(OLS.get().getServiceProvider(handler.getService()));
                        
                        Document            domResponse = handler.processRequest(domRequest);
                        TransformerFactory  transFactory = TransformerFactory.newInstance();
                        Transformer         transformer = transFactory.newTransformer();
                        
//                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.transform(new DOMSource(domResponse.getFirstChild()),
                              new StreamResult(httpResponse.getOutputStream()));
                        break;
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
