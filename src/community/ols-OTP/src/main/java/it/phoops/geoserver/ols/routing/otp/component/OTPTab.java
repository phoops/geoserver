package it.phoops.geoserver.ols.routing.otp.component;

import it.phoops.geoserver.ols.OLS;
import it.phoops.geoserver.ols.OLSInfo;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.OLSServiceProvider;
import it.phoops.geoserver.ols.OLSServiceProviderGUI;
import it.phoops.geoserver.ols.routing.otp.OTPServiceProvider;
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

public class OTPTab extends AbstractTab implements ValidateCheckboxTab{
    private String      urlOTP;
    private String      activeOTP;
    private String      portOTP;
    private OTPPanel    instancePanel;

    public String getUrlOTP() {
        if(instancePanel != null)
            return instancePanel.getUrlOTP();
        return urlOTP;
    }

    public void setUrlOTP(String urlOTP) {
        if(instancePanel != null)
            instancePanel.setUrlOTP(urlOTP);
        this.urlOTP = urlOTP;
    }

    public String getActiveOTP() {
        if(instancePanel != null)
            return instancePanel.getActiveOTP();
        return activeOTP;
    }

    public void setActiveOTP(String activeOTP) {
        if(instancePanel != null)
            instancePanel.setActiveOTP(activeOTP);
        this.activeOTP = activeOTP;
    }

    public String getPortOTP() {
        if(instancePanel != null)
            return instancePanel.getPortOTP();
        return portOTP;
    }

    public void setPortOTP(String portOTP) {
        if(instancePanel != null)
            instancePanel.setPortOTP(portOTP);
        this.portOTP = portOTP;
    }

    public OTPPanel getInstancePanel() {
        return instancePanel;
    }

    public void setInstancePanel(OTPPanel instancePanel) {
        this.instancePanel = instancePanel;
    }

    public OTPTab(IModel<String> title) {
        super(title);
    }

    @Override
    public String getCheckboxValue() {
        if(instancePanel != null)
            return instancePanel.getCheckboxOTP().getModelObject().toString();
        return this.getActiveOTP();
    }

    @Override
    public void setChecckboxValue(String value) {
        if(instancePanel != null){
            instancePanel.setActiveOTP(value);
            instancePanel.getCheckboxOTP().setModelObject(Boolean.parseBoolean(value));
        }else{
            setActiveOTP(value);
        }
    }

    @Override
    public Panel getPanel(String panelId) {
        if(instancePanel == null)
            instancePanel = new OTPPanel(panelId);
        instancePanel.setActiveOTP(activeOTP);
        instancePanel.getCheckboxOTP().setModelObject(Boolean.parseBoolean(activeOTP));
        instancePanel.setUrlOTP(urlOTP);
        instancePanel.setPortOTP(portOTP);
        return instancePanel;
    }
    
    private static class OTPPanel extends Panel{
        private String          urlOTP;
        private String          activeOTP;
        private String          portOTP;
        private CheckBox        checkboxOTP;

        public OTPPanel(String id) {
            super(id);
            checkboxOTP = new CheckBox("checkboxOTP", Model.of(Boolean.FALSE)){
                
                @Override
                protected boolean wantOnSelectionChangedNotifications() {
                    return true;
                }
                    
                @Override
                public void onSelectionChanged() {
                    super.onSelectionChanged();
                    activeOnlyOneCheck(this.getModelObject());
                    setActiveOTP(this.getModelObject().toString());
                }    
                
                private void activeOnlyOneCheck(Boolean value){
                      OLS ols = OLS.get();
                      GeoServer gs = ols.getGeoServer();
                      OLSInfo olsInfo = gs.getService(OLSInfo.class);
                      List<OLSServiceProvider> serviceProvider = olsInfo.getServiceProvider();
                      
                      for (OLSServiceProvider provider : serviceProvider) {
                          if(provider.getServiceType() == OLSService.ROUTING_NAVIGATION
                                  && !(provider instanceof OTPServiceProvider)
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
            add(checkboxOTP);
            add(new TextField("urlOTP",new PropertyModel(this,"urlOTP")));
            add(new TextField("portOTP",new PropertyModel(this,"portOTP")));
        }

        public String getUrlOTP() {
            return urlOTP;
        }

        public void setUrlOTP(String urlOTP) {
            this.urlOTP = urlOTP;
        }

        public String getActiveOTP() {
            return activeOTP;
        }

        public void setActiveOTP(String activeOTP) {
            this.activeOTP = activeOTP;
        }

        public String getPortOTP() {
            return portOTP;
        }

        public void setPortOTP(String portOTP) {
            this.portOTP = portOTP;
        }

        public CheckBox getCheckboxOTP() {
            return checkboxOTP;
        }

        public void setCheckboxOTP(CheckBox checkboxOTP) {
            this.checkboxOTP = checkboxOTP;
        }
        
        
    
    }

}
