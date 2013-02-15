package it.phoops.geoserver.ols.routing.pgrouting.component;

import it.phoops.geoserver.ols.OLS;
import it.phoops.geoserver.ols.OLSInfo;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.OLSServiceProvider;
import it.phoops.geoserver.ols.OLSServiceProviderGUI;
import it.phoops.geoserver.ols.routing.pgrouting.Algorithm;
import it.phoops.geoserver.ols.routing.pgrouting.PgRoutingServiceProvider;
import it.phoops.geoserver.ols.web.validator.ValidateCheckboxTab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.PatternValidator;
import org.geoserver.config.GeoServer;

public class PgRoutingTab extends AbstractTab implements ValidateCheckboxTab{
    private String                              activePgRouting;
    private String                              hostPgRouting;
    private String                              portPgRouting;
    private String                              pswPgRouting;
    private String                              dbPgRouting;
    private String                              userPgRouting;
    private int                                 codeAlgorithmSelected;
    private PgRoutingPanel                      instancePanel;
    private List<ShortestPathAlgorithmType>     algorithmList = null;
    private ShortestPathAlgorithmType           selectedAlgorithm;
    
    public class ShortestPathAlgorithmType implements Serializable{
        private Algorithm               algorithm;
        private String                  code;
        private String                  descriptionKey;
        private Component               component;
        
        public ShortestPathAlgorithmType() {}
        
        public ShortestPathAlgorithmType(Algorithm algorithm, String descriptionKey){
            super();
            this.algorithm = algorithm;
            this.code = algorithm.toString();
            this.descriptionKey = descriptionKey;
        }
        
        public Algorithm getService() {
            return algorithm;
        }

        public void setService(Algorithm algorithm) {
            this.algorithm = algorithm;
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
            Localizer localizer = Application.get().getResourceSettings().getLocalizer();
            return localizer.getString(descriptionKey, component);
        }
    }

    public PgRoutingTab(IModel<String> title) {
        super(title);
        algorithmList = new ArrayList<PgRoutingTab.ShortestPathAlgorithmType>();
        algorithmList.add(new ShortestPathAlgorithmType(Algorithm.DIJKSTRA, "ShortestPathAlgorithmType.dijkstra"));//1
        algorithmList.add(new ShortestPathAlgorithmType(Algorithm.A_STAR, "ShortestPathAlgorithmType.a.star"));//2
        algorithmList.add(new ShortestPathAlgorithmType(Algorithm.SHOOTING_STAR, "ShortestPathAlgorithmType.shooting.star"));//3
    }

    @Override
    public String getCheckboxValue() {
        if(instancePanel != null)
            return instancePanel.getCheckboxPgRouting().getModelObject().toString();
        return this.getActivePgRouting();
    }

    @Override
    public void setChecckboxValue(String value) {
        if(instancePanel != null){
            instancePanel.setActivePgRouting(value);
            instancePanel.getCheckboxPgRouting().setModelObject(Boolean.parseBoolean(value));
        }else{
            setActivePgRouting(value);
        }
    }

    @Override
    public Panel getPanel(String panelId) {
        if(instancePanel == null){
            instancePanel = new PgRoutingPanel(panelId);
            instancePanel.add(new ShortestPathDropDownChoice("algorithm", new PropertyModel<ShortestPathAlgorithmType>(this, "selectedAlgorithm"), algorithmList));
        }
        instancePanel.setActivePgRouting(activePgRouting);
        instancePanel.getCheckboxPgRouting().setModelObject(Boolean.parseBoolean(activePgRouting));
        instancePanel.setHostPgRouting(hostPgRouting);
        instancePanel.setPortPgRouting(portPgRouting);
        instancePanel.setPswPgRouting(pswPgRouting);
        instancePanel.setDbPgRouting(dbPgRouting);
        instancePanel.setUserPgRouting(userPgRouting);
        instancePanel.setAlgorithmList(this.algorithmList);
        instancePanel.setSelectedAlgorithm(this.algorithmList.get(getCodeAlgorithmSelected()-1));
        return instancePanel;
    }
    
    public String getActivePgRouting() {
        if(instancePanel != null)
            return instancePanel.getActivePgRouting();
        return activePgRouting;
    }

    public void setActivePgRouting(String activePgRouting) {
        if(instancePanel != null)
            instancePanel.setActivePgRouting(activePgRouting);
        this.activePgRouting = activePgRouting;
    }

    public String getHostPgRouting() {
        if(instancePanel != null)
            return instancePanel.getHostPgRouting();
        return hostPgRouting;
    }

    public void setHostPgRouting(String hostPgRouting) {
        if(instancePanel != null)
            instancePanel.setHostPgRouting(hostPgRouting);
        this.hostPgRouting = hostPgRouting;
    }

    public String getPortPgRouting() {
        if(instancePanel != null)
            return instancePanel.getPortPgRouting();
        return portPgRouting;
    }

    public void setPortPgRouting(String portPgRouting) {
        if(instancePanel != null)
            instancePanel.setPortPgRouting(portPgRouting);
        this.portPgRouting = portPgRouting;
    }

    public String getPswPgRouting() {
        if(instancePanel != null)
            return instancePanel.getPswPgRouting();
        return pswPgRouting;
    }

    public void setPswPgRouting(String pswPgRouting) {
        if(instancePanel != null)
            instancePanel.setPswPgRouting(pswPgRouting);
        this.pswPgRouting = pswPgRouting;
    }

    public String getDbPgRouting() {
        if(instancePanel != null)
            return instancePanel.getDbPgRouting();
        return dbPgRouting;
    }

    public void setDbPgRouting(String dbPgRouting) {
        if(instancePanel != null)
            instancePanel.setDbPgRouting(dbPgRouting);
        this.dbPgRouting = dbPgRouting;
    }
    
    public String getUserPgRouting() {
        if(instancePanel != null)
            return instancePanel.getUserPgRouting();
        return userPgRouting;
    }

    public void setUserPgRouting(String userPgRouting) {
        if(instancePanel != null)
            instancePanel.setUserPgRouting(userPgRouting);
        this.userPgRouting = userPgRouting;
    }

    public ShortestPathAlgorithmType getSelectedAlgorithm() {
        if(instancePanel != null)
            return instancePanel.getSelectedAlgorithm();
        return selectedAlgorithm;
    }

    public void setSelectedAlgorithm(ShortestPathAlgorithmType selectedAlgorithm) {
        if(instancePanel != null)
            instancePanel.setSelectedAlgorithm(selectedAlgorithm);
        this.selectedAlgorithm = selectedAlgorithm;
    }

    public int getCodeAlgorithmSelected() {
        return codeAlgorithmSelected;
    }

    public void setCodeAlgorithmSelected(int codeAlgorithmSelected) {
        this.codeAlgorithmSelected = codeAlgorithmSelected;
    }

    public PgRoutingPanel getInstancePanel() {
        return instancePanel;
    }

    public void setInstancePanel(PgRoutingPanel instancePanel) {
        this.instancePanel = instancePanel;
    }

    private static class PgRoutingPanel extends Panel{
        private String                                          activePgRouting;
        private String                                          hostPgRouting;
        private String                                          portPgRouting;
        private String                                          userPgRouting;
        private String                                          dbPgRouting;
        private String                                          pswPgRouting;
        private CheckBox                                        checkboxPgRouting;
        private PasswordTextField                               password;
        private List<ShortestPathAlgorithmType>                 algorithmList = null;
        private ShortestPathAlgorithmType                       selectedAlgorithm;
        
        //1 digit, 1 lower, 1 upper, 1 symbol "@#$%", from 6 to 20
        private final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
    
        public PgRoutingPanel(String id) {
            super(id);
            checkboxPgRouting = new CheckBox("checkboxPgRouting", Model.of(Boolean.FALSE)){
                
                @Override
                protected boolean wantOnSelectionChangedNotifications() {
                    return true;
                }
                    
                @Override
                public void onSelectionChanged() {
                    super.onSelectionChanged();
                    activeOnlyOneCheck(this.getModelObject());
                    setActivePgRouting(this.getModelObject().toString());
                }    
                
                private void activeOnlyOneCheck(Boolean value){
                      OLS ols = OLS.get();
                      GeoServer gs = ols.getGeoServer();
                      OLSInfo olsInfo = gs.getService(OLSInfo.class);
                      List<OLSServiceProvider> serviceProvider = olsInfo.getServiceProvider();
                      
                      for (OLSServiceProvider provider : serviceProvider) {
                          if(provider.getServiceType() == OLSService.ROUTING_NAVIGATION
                                  && !(provider instanceof PgRoutingServiceProvider)
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
            //Actiche Check
            add(checkboxPgRouting);
            //Host
            add(new TextField("hostPgRouting",new PropertyModel(this,"hostPgRouting")));
            //Port
            add(new TextField("portPgRouting",new PropertyModel(this,"portPgRouting")));
            //Database
            add(new TextField("dbPgRouting",new PropertyModel(this,"dbPgRouting")));
            //User
            add(new TextField("userPgRouting",new PropertyModel(this,"userPgRouting")));
            //Password field
            password = new PasswordTextField("pswPgRouting",Model.of(""));
            password.add(new PatternValidator(PASSWORD_PATTERN));
            add(password);
        }

        public String getActivePgRouting() {
            return activePgRouting;
        }

        public void setActivePgRouting(String activePgRouting) {
            this.activePgRouting = activePgRouting;
        }

        public String getHostPgRouting() {
            return hostPgRouting;
        }

        public void setHostPgRouting(String hostPgRouting) {
            this.hostPgRouting = hostPgRouting;
        }

        public String getPortPgRouting() {
            return portPgRouting;
        }

        public void setPortPgRouting(String portPgRouting) {
            this.portPgRouting = portPgRouting;
        }

        public String getPswPgRouting() {
            return password.getModelObject();
        }

        public void setPswPgRouting(String pswPgRouting) {
            this.password.setModelObject(pswPgRouting);
        }

        public String getDbPgRouting() {
            return dbPgRouting;
        }

        public void setDbPgRouting(String dbPgRouting) {
            this.dbPgRouting = dbPgRouting;
        }

        public CheckBox getCheckboxPgRouting() {
            return checkboxPgRouting;
        }

        public String getUserPgRouting() {
            return userPgRouting;
        }

        public void setUserPgRouting(String userPgRouting) {
            this.userPgRouting = userPgRouting;
        }
        
        public ShortestPathAlgorithmType getSelectedAlgorithm() {
            return selectedAlgorithm;
        }

        public void setSelectedAlgorithm(ShortestPathAlgorithmType selectedAlgorithm) {
            this.selectedAlgorithm = selectedAlgorithm;
        }
        
        public void setCheckboxPgRouting(CheckBox checkboxPgRouting) {
            this.checkboxPgRouting = checkboxPgRouting;
        }
        
        public List<ShortestPathAlgorithmType> getAlgorithmList() {
            return algorithmList;
        }

        public void setAlgorithmList(List<ShortestPathAlgorithmType> algorithmList) {
            this.algorithmList = algorithmList;
        }
    }
}
