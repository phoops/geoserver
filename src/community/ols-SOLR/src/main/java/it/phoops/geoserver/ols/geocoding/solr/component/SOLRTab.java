package it.phoops.geoserver.ols.geocoding.solr.component;

import it.phoops.geoserver.ols.OLS;
import it.phoops.geoserver.ols.OLSInfo;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.OLSServiceProvider;
import it.phoops.geoserver.ols.OLSServiceProviderGUI;
import it.phoops.geoserver.ols.geocoding.solr.SOLRServiceProvider;
import it.phoops.geoserver.ols.web.validator.ValidateCheckboxTab;

import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.config.GeoServer;

public class SOLRTab extends AbstractTab implements ValidateCheckboxTab{
	private String 			urlSOLR;
	private String                 activeSOLR;
	private SOLR9Panel 		instancePanel;
	
	public String getUrlSOLR() {
	    if(instancePanel != null)
	        return instancePanel.getUrlSOLR();
	    return urlSOLR;
	}

	public void setUrlSOLR(String urlSOLR) {
	    if(instancePanel != null)
	        instancePanel.setUrlSOLR(urlSOLR);
	    this.urlSOLR = urlSOLR;
	}
	
	public String getActiveSOLR() {
	    if(instancePanel != null)
	        return instancePanel.getActiveSOLR();
            return activeSOLR;
        }

        public void setActiveSOLR(String activeSOLR) {
            if(instancePanel != null)
                instancePanel.setActiveSOLR(activeSOLR);
            this.activeSOLR = activeSOLR;
        }

        public SOLR9Panel getInstancePanel() {
	    return instancePanel;
	}

	public void setInstancePanel(SOLR9Panel instancePanel) {
		this.instancePanel = instancePanel;
	}

	@Override
        public String getCheckboxValue() {
	    if(instancePanel != null)
                return instancePanel.getCheckboxSOLR().getModelObject().toString();
	    return this.getActiveSOLR();
        }
    
        @Override
        public void setChecckboxValue(String value) {
            if(instancePanel != null){
                instancePanel.setActiveSOLR(value);
                instancePanel.getCheckboxSOLR().setModelObject(Boolean.parseBoolean(value));
            }else{
                setActiveSOLR(value);
            }
        }

        @Override
	public Panel getPanel(String panelId) {
	    if(instancePanel == null)
	        instancePanel = new SOLR9Panel(panelId);
	    instancePanel.setActiveSOLR(activeSOLR);
	    instancePanel.getCheckboxSOLR().setModelObject(Boolean.parseBoolean(activeSOLR));
	    instancePanel.setUrlSOLR(urlSOLR);
	    return instancePanel;
	}
    
        public SOLRTab(IModel<String> title) {
    	    super(title);
	}
	
	private static class SOLR9Panel extends Panel{
	    private String 	urlSOLR;
	    private String     activeSOLR;
	    private CheckBox   checkboxSOLR;
	
	    
	    public SOLR9Panel(String id){
	        super(id);
	        checkboxSOLR = new CheckBox("checkboxSOLR", Model.of(Boolean.FALSE)){
                    
                    @Override
                    protected boolean wantOnSelectionChangedNotifications() {
                        return true;
                    }
                        
                    @Override
                    public void onSelectionChanged() {
                        super.onSelectionChanged();
                        activeOnlyOneCheck(this.getModelObject());
                        setActiveSOLR(this.getModelObject().toString());
                    }    
                    
                    private void activeOnlyOneCheck(Boolean value){
                          OLS ols = OLS.get();
                          GeoServer gs = ols.getGeoServer();
                          OLSInfo olsInfo = gs.getService(OLSInfo.class);
                          List<OLSServiceProvider> serviceProvider = olsInfo.getServiceProvider();
                          
                          for (OLSServiceProvider provider : serviceProvider) {
                              if(provider.getServiceType() == OLSService.GEOCODING
                                      && !(provider instanceof SOLRServiceProvider)
                                      && value){
                                  
                                  OLSServiceProviderGUI providerGUI = (OLSServiceProviderGUI)provider;
                                  Boolean notValue = !value;
                                  String strValue = notValue.toString();
                                  providerGUI.getProperties().setProperty("OLS.serviceProvider.service.active", strValue);
                                  ((ValidateCheckboxTab)providerGUI.getTab()).setChecckboxValue(strValue);
                              }
                          }
                      }
                };
                add(checkboxSOLR);
	        add(new TextField("urlSOLR",new PropertyModel(this,"urlSOLR")));
    	    }
	    
	    public String getUrlSOLR() {
	        return urlSOLR;
	    }

	    public void setUrlSOLR(String urlSOLR) {
	        this.urlSOLR = urlSOLR;
	    }

            public String getActiveSOLR() {
                return activeSOLR;
            }
    
            public void setActiveSOLR(String activeSOLR) {
                this.activeSOLR = activeSOLR;
            }
    
            public CheckBox getCheckboxSOLR() {
                return checkboxSOLR;
            }
    
            public void setCheckboxSOLR(CheckBox checkboxSOLR) {
                this.checkboxSOLR = checkboxSOLR;
            }
	}
}
