package it.phoops.geoserver.ols.routing.pgrouting.component;

import it.phoops.geoserver.ols.OLS;
import it.phoops.geoserver.ols.OLSInfo;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.OLSServiceProvider;
import it.phoops.geoserver.ols.OLSServiceProviderGUI;
import it.phoops.geoserver.ols.routing.Language;
import it.phoops.geoserver.ols.routing.LanguageDropDownChoice;
import it.phoops.geoserver.ols.routing.LanguageType;
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
import org.geoserver.config.GeoServer;

public class PgRoutingTab extends AbstractTab implements ValidateCheckboxTab{
    private String                              activePgRouting;
    private String                              hostPgRouting;
    private String                              portPgRouting;
    private String                              pswPgRouting;
    private String                              dbPgRouting;
    private String                              schemaPgRouting;
    private String                              userPgRouting;
    private String                              nodeTableRouting;
    private String                              edgeTableRouting;
    private String                              edgeQueryRouting;
    private String                              undirectedQueryRouting;
    private int                                 codeAlgorithmSelected;
    private PgRoutingPanel                      instancePanel;
    private List<ShortestPathAlgorithmType>     algorithmList = null;
    private ShortestPathAlgorithmType           selectedAlgorithm;
    private LanguageType                        selectedLanguage;
    private String                              navigationInfo;
    private String                              navigationInfoShort;
    private String                              navigationInfoRel;
    private List<LanguageType>                  languageList = null;
    private int                                 codeLanguageSelected;
    
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
        languageList = new ArrayList<LanguageType>();
        languageList.add(new LanguageType(Language.ITA, "ols.navigation.ita"));//1
        languageList.add(new LanguageType(Language.ENG, "ols.navigation.eng"));//2
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
            instancePanel.add(new LanguageDropDownChoice("language", new PropertyModel<LanguageType>(this, "selectedLanguage"), languageList));
        }
        instancePanel.setActivePgRouting(activePgRouting);
        instancePanel.getCheckboxPgRouting().setModelObject(Boolean.parseBoolean(activePgRouting));
        instancePanel.setHostPgRouting(hostPgRouting);
        instancePanel.setPortPgRouting(portPgRouting);
        instancePanel.setPswPgRouting(pswPgRouting);
        instancePanel.setDbPgRouting(dbPgRouting);
        instancePanel.setSchemaPgRouting(schemaPgRouting);
        instancePanel.setUserPgRouting(userPgRouting);
        instancePanel.setNodeTableRouting(nodeTableRouting);
        instancePanel.setEdgeTableRouting(edgeTableRouting);
        instancePanel.setEdgeQueryRouting(edgeQueryRouting);
        instancePanel.setUndirectedQueryRouting(undirectedQueryRouting);
        instancePanel.setAlgorithmList(this.algorithmList);
        instancePanel.setSelectedAlgorithm(this.algorithmList.get(getCodeAlgorithmSelected()-1));
        instancePanel.setNavigationInfo(navigationInfo);
        instancePanel.setNavigationInfoShort(navigationInfoShort);
        instancePanel.setNavigationInfoRel(navigationInfoRel);
        instancePanel.setSelectedLanguage(this.languageList.get(getCodeLanguageSelected()-1));
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
    
    public String getSchemaPgRouting() {
        if(instancePanel != null)
            return instancePanel.getSchemaPgRouting();
        return schemaPgRouting;
    }

    public void setSchemaPgRouting(String schemaPgRouting) {
        if(instancePanel != null)
            instancePanel.setSchemaPgRouting(schemaPgRouting);
        this.schemaPgRouting = schemaPgRouting;
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
    
    public String getNodeTableRouting() {
        if (instancePanel != null)
            return instancePanel.getNodeTableRouting();
        return nodeTableRouting;
    }

    public void setNodeTableRouting(String nodeTableRouting) {
        if(instancePanel != null)
            instancePanel.setNodeTableRouting(nodeTableRouting);
        this.nodeTableRouting = nodeTableRouting;
    }
    
    public String getEdgeTableRouting() {
        if (instancePanel != null)
            return instancePanel.getEdgeTableRouting();
        return edgeTableRouting;
    }

    public void setEdgeTableRouting(String edgeTableRouting) {
        if(instancePanel != null)
            instancePanel.setEdgeTableRouting(edgeTableRouting);
        this.edgeTableRouting = edgeTableRouting;
    }
    
    public String getEdgeQueryRouting() {
        if (instancePanel != null)
            return instancePanel.getEdgeQueryRouting();
        return edgeQueryRouting;
    }

    public void setUndirectedQueryRouting(String undirectedQueryRouting) {
        if(instancePanel != null)
            instancePanel.setEdgeQueryRouting(undirectedQueryRouting);
        this.undirectedQueryRouting = undirectedQueryRouting;
    }
    
    public String getUndirectedQueryRouting() {
        if (instancePanel != null)
            return instancePanel.getUndirectedQueryRouting();
        return undirectedQueryRouting;
    }

    public void setEdgeQueryRouting(String edgeQueryRouting) {
        if(instancePanel != null)
            instancePanel.setEdgeQueryRouting(edgeQueryRouting);
        this.edgeQueryRouting = edgeQueryRouting;
    }
    
    public String getNavigationInfo() {
        if(instancePanel != null)
            return instancePanel.getNavigationInfo();
        return navigationInfo;
    }

    public void setNavigationInfo(String navigationInfo) {
        if(instancePanel != null)
            instancePanel.setNavigationInfo(navigationInfo);
        this.navigationInfo = navigationInfo;
    }

    public String getNavigationInfoShort() {
        if(instancePanel != null)
            return instancePanel.getNavigationInfoShort();
        return navigationInfoShort;
    }

    public void setNavigationInfoShort(String navigationInfoShort) {
        if(instancePanel != null)
            instancePanel.setNavigationInfoShort(navigationInfoShort);
        this.navigationInfoShort = navigationInfoShort;
    }

    public String getNavigationInfoRel() {
        if(instancePanel != null)
            return instancePanel.getNavigationInfoRel();
        return navigationInfoRel;
    }

    public void setNavigationInfoRel(String navigationInfoRel) {
        if(instancePanel != null)
            instancePanel.setNavigationInfoRel(navigationInfoRel);
        this.navigationInfoRel = navigationInfoRel;
    }

    public LanguageType getSelectedLanguage() {
        if(instancePanel != null)
            return instancePanel.getSelectedLanguage();
        return selectedLanguage;
    }

    public void setSelectedLanguage(LanguageType selectedLanguage) {
        if(instancePanel != null)
            instancePanel.setSelectedLanguage(selectedLanguage);
        this.selectedLanguage = selectedLanguage;
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

    public int getCodeLanguageSelected() {
        return codeLanguageSelected;
    }

    public void setCodeLanguageSelected(int codeLanguageSelected) {
        this.codeLanguageSelected = codeLanguageSelected;
    }

    private static class PgRoutingPanel extends Panel{
        private String                                          activePgRouting;
        private String                                          hostPgRouting;
        private String                                          portPgRouting;
        private String                                          userPgRouting;
        private String                                          dbPgRouting;
        private String                                          schemaPgRouting;
        private String                                          pswPgRouting;
        private String                                          nodeTableRouting;
        private String                                          edgeTableRouting;
        private String                                          edgeQueryRouting;
        private String                                          undirectedQueryRouting;
        private String                                          navigationInfo;
        private String                                          navigationInfoShort;
        private String                                          navigationInfoRel;
        private LanguageType                                    selectedLanguage;
        private List<LanguageType>                              languageList = null;
        private CheckBox                                        checkboxPgRouting;
        private PasswordTextField                               password;
        private List<ShortestPathAlgorithmType>                 algorithmList = null;
        private ShortestPathAlgorithmType                       selectedAlgorithm;
        
//        //1 digit, 1 lower, 1 upper, 1 symbol "@#$%", from 6 to 20
//        private final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
//    
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
            //Schema
            add(new TextField("schemaPgRouting",new PropertyModel(this,"schemaPgRouting")));
            //User
            add(new TextField("userPgRouting",new PropertyModel(this,"userPgRouting")));
            //Password field
            password = new PasswordTextField("pswPgRouting",Model.of(""));
            add(password);
            //Node table
            add(new TextField("nodeTableRouting",new PropertyModel(this,"nodeTableRouting")));
            //Edge table
            add(new TextField("edgeTableRouting",new PropertyModel(this,"edgeTableRouting")));
            //Edge query
            add(new TextField("edgeQueryRouting",new PropertyModel(this,"edgeQueryRouting")));
            //Undirected query
            add(new TextField("undirectedQueryRouting",new PropertyModel(this,"undirectedQueryRouting")));
            
            add(new TextField("navigationInfo",new PropertyModel(this,"navigationInfo")));
            add(new TextField("navigationInfoShort",new PropertyModel(this,"navigationInfoShort")));
            add(new TextField("navigationInfoRel",new PropertyModel(this,"navigationInfoRel")));
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

        public String getSchemaPgRouting() {
            return schemaPgRouting;
        }

        public void setSchemaPgRouting(String schemaPgRouting) {
            this.schemaPgRouting = schemaPgRouting;
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

        public String getNodeTableRouting() {
            return nodeTableRouting;
        }

        public void setNodeTableRouting(String nodeTableRouting) {
            this.nodeTableRouting = nodeTableRouting;
        }

        public String getEdgeTableRouting() {
            return edgeTableRouting;
        }

        public void setEdgeTableRouting(String edgeTableRouting) {
            this.edgeTableRouting = edgeTableRouting;
        }

        public String getEdgeQueryRouting() {
            return edgeQueryRouting;
        }

        public void setEdgeQueryRouting(String edgeQueryRouting) {
            this.edgeQueryRouting = edgeQueryRouting;
        }

        public String getUndirectedQueryRouting() {
            return undirectedQueryRouting;
        }

        public void setUndirectedQueryRouting(String undirectedQueryRouting) {
            this.undirectedQueryRouting = undirectedQueryRouting;
        }

        public String getNavigationInfo() {
            return navigationInfo;
        }

        public void setNavigationInfo(String navigationInfo) {
            this.navigationInfo = navigationInfo;
        }

        public String getNavigationInfoShort() {
            return navigationInfoShort;
        }

        public void setNavigationInfoShort(String navigationInfoShort) {
            this.navigationInfoShort = navigationInfoShort;
        }

        public String getNavigationInfoRel() {
            return navigationInfoRel;
        }

        public void setNavigationInfoRel(String navigationInfoRel) {
            this.navigationInfoRel = navigationInfoRel;
        }

        public List<LanguageType> getLanguageList() {
            return languageList;
        }

        public void setLanguageList(List<LanguageType> languageList) {
            this.languageList = languageList;
        }

        public LanguageType getSelectedLanguage() {
            return selectedLanguage;
        }

        public void setSelectedLanguage(LanguageType selectedLanguage) {
            this.selectedLanguage = selectedLanguage;
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
