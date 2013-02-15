package it.phoops.geoserver.ols.web;

import it.phoops.geoserver.ols.OLSInfo;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.web.component.ServiceDropDownChoice;
import it.phoops.geoserver.ols.web.validator.ValidateCheckboxTab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.web.services.BaseServiceAdminPage;

/**
 * 
 * @author aCasini
 * 
 */
public class OLSAdminPage extends BaseServiceAdminPage<OLSInfo> {
	public class OLSGUIService implements Serializable {
		private OLSService service;
		private String	code;
		private String descriptionKey;
		private Component component;
		
		public OLSGUIService() {
		}
		
		public OLSGUIService(OLSService service, String descriptionKey, Component component) {
			super();
			this.service = service;
			this.code = service.toString();
			this.descriptionKey = descriptionKey;
			this.component = component;
		}
		
		public OLSService getService() {
			return service;
		}

		public void setService(OLSService service) {
			this.service = service;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getDescriptionKey() {
			return descriptionKey;
		}

		public void setDescriptionKey(String descriptionKey) {
			this.descriptionKey = descriptionKey;
		}
		
		public Component getComponent() {
			return component;
		}

		public void setComponent(Component component) {
			this.component = component;
		}

		@Override
		public String toString() {
			Localizer	localizer = Application.get().getResourceSettings().getLocalizer();
			return localizer.getString(descriptionKey, component);
		}
	}
	
	private static List<ITab>              tabsOLS = null;
	private List<OLSGUIService>		SERVICES = null;
	private TabbedPanel			tabPanelOLS = null;
	private OLSGUIService			selectedService = null;
	private static Form                           form;
	
    @Override
    protected void build(IModel info, Form form) {
        
        SERVICES = new ArrayList<OLSGUIService>();
        SERVICES.add(new OLSGUIService(OLSService.GEOCODING, "OLSGUIService.geocoding", this));
        SERVICES.add(new OLSGUIService(OLSService.REVERSE_GEOCODING, "OLSGUIService.reverseGeocoding", this));
        SERVICES.add(new OLSGUIService(OLSService.ROUTING_NAVIGATION, "OLSGUIService.routingNavigation", this));
        
        
        ServiceDropDownChoice listServices = new ServiceDropDownChoice("service", new PropertyModel<OLSGUIService>(this, "selectedService"), SERVICES,form);
        
	form.add(listServices);

	if(listServices.getSelectedService() == null){
	    if(tabsOLS == null){
		tabsOLS = new ArrayList<ITab>();
	    }
	    tabPanelOLS = new AjaxTabbedPanel("tabList", tabsOLS);
//	    tabPanelOLS = new TabbedPanel("tabList", tabsOLS);
	    tabPanelOLS.setVisible(false);
	    form.add(tabPanelOLS);
	}
		
    }

    @Override
    protected Class<OLSInfo> getServiceClass() {
        return OLSInfo.class;
    }

    @Override
    protected String getServiceName() {
        return "OLS";
    }
    
//    @Override
//    protected void handleSubmit(OLSInfo info) {
//        // TODO Auto-generated method stub
//        System.out.println("--- Handler Submit");
//        super.handleSubmit(info);
//        
//    }

    public OLSGUIService getSelectedService() {
	return selectedService;
    }

    public void setSelectedService(OLSGUIService selectedService) {
        this.selectedService = selectedService;
    }
    
//    public static void addOLSTab(ITab iTab){
//        if(tabsOLS == null){
//            tabsOLS = new ArrayList<ITab>();
//        }
//        tabsOLS.add(iTab);
//    }
    
//    public static void setForm(Form form){
//        OLSAdminPage.form = form;
//    }
//	
}
