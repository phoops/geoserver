package it.phoops.geoserver.ols.web.component;

import it.phoops.geoserver.ols.OLS;
import it.phoops.geoserver.ols.OLSAbstractServiceProvider;
import it.phoops.geoserver.ols.OLSInfo;
import it.phoops.geoserver.ols.OLSInfoImpl;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.OLSServiceProvider;
import it.phoops.geoserver.ols.util.ApplicationContextUtil;
import it.phoops.geoserver.ols.web.OLSAdminPage;
import it.phoops.geoserver.ols.web.OLSAdminPage.OLSGUIService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.config.GeoServer;
import org.springframework.context.ApplicationContext;

/**
 * DropDownChoice component to select the service OLS
 *
 */
public class ServiceDropDownChoice extends DropDownChoice<OLSGUIService>{
	private OLSGUIService			selectedService = null;
	private Form				form;

	public ServiceDropDownChoice(String id, PropertyModel<OLSGUIService> model, List<OLSGUIService> displayData, Form form) {
	    super(id,model,displayData);
	    this.form = form;
	}
	
	@Override
	protected void onSelectionChanged(OLSGUIService newSelection) {
	    System.out.println("onSelectionChanged: "+ newSelection);
	    super.onSelectionChanged(newSelection);
	    setSelectedService(newSelection);
	    showPanelOLS(form);
	}

	@Override
	protected boolean wantOnSelectionChangedNotifications() {
	    return true;
	}

	public OLSGUIService getSelectedService() {
	    return selectedService;
	}


	public void setSelectedService(OLSGUIService selectedService) {
	    this.selectedService = selectedService;
	}
	
	private void showPanelOLS(Form form){
	    ApplicationContextUtil appContextUtil = ApplicationContextUtil.getIstance();
    	    ApplicationContext appContext = appContextUtil.getAppContext();
    	
    	    OLSService selectedService = getSelectedService().getService();
    	
    	    OLS ols = OLS.get();
    	    GeoServer gs = ols.getGeoServer();
    	    OLSInfo olsInfo = gs.getService(OLSInfo.class);
    	    
    	    OLSAbstractServiceProvider     activeProvider = (OLSAbstractServiceProvider)olsInfo.findServiceActive(selectedService);
    	    OLSAbstractServiceProvider     provider = null;
            OLSAbstractServiceProvider     notActiveProvider = null;
    	
            //Revome Listener
            for (Object listener : gs.getListeners().toArray()) {
    	        if (listener instanceof OLSAbstractServiceProvider) {
    	            gs.removeListener((OLSAbstractServiceProvider)listener);
    	        }
    	    }
    	    
    	    //Choose the correct beans
    	    Map<String,OLSAbstractServiceProvider>    beans = appContext.getBeansOfType(OLSAbstractServiceProvider.class);
    	    final List<ITab> tabsOLS = new ArrayList<ITab>();

            for (String beanName : beans.keySet()) {
                provider = beans.get(beanName);

                if(provider.getServiceType() == selectedService){
                    if (activeProvider != null) {
                            if (activeProvider instanceof OLSAbstractServiceProvider && provider.getClass().equals(activeProvider.getClass())) {
                                    provider = (OLSAbstractServiceProvider)activeProvider;
                                    gs.addListener(provider);
                            }else{
                                notActiveProvider = (OLSAbstractServiceProvider)olsInfo.findServiceNotActive(provider, selectedService);
                                if(notActiveProvider != null)
                                    provider = notActiveProvider;
                                gs.addListener(provider);
                                olsInfo.addServiceProvide(provider);
//                                olsInfo.setServiceProvider(provider);
                            }
                    } else {
//                            olsInfo.setServiceProvider(provider);
                            notActiveProvider = (OLSAbstractServiceProvider)olsInfo.findServiceNotActive(provider, selectedService);
                            if(notActiveProvider != null)
                                provider = notActiveProvider;
                            olsInfo.addServiceProvide(provider);
//                            activeProvider = provider;
                            gs.addListener(provider);
                    }
                    ITab tab = provider.getTab();
                    provider.setPropertiesTab(tab);
                    tabsOLS.add(tab);
                }
            }
    	
            form.remove("tabList");
            form.add(new TabbedPanel("tabList", tabsOLS){

                @Override
                protected WebMarkupContainer newLink(String linkId, final int index) {
                    return new Link<Void>(linkId){
                        private static final long serialVersionUID = 1L;
                        
                        @Override
                        public void onClick(){
//                            System.out.println("Click sul TAB: "+index);
//                            System.out.println(tabsOLS.size());
//                            
////                            tabsOLS.get(0).getPanel("panelId");
//                            System.out.println(tabsOLS.get(0).getClass());
//                            
                            setSelectedTab(index);
                        }
                    };
                    
                }
                
            });
	}
}
