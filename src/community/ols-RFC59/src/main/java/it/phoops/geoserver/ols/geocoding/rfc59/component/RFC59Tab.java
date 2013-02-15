package it.phoops.geoserver.ols.geocoding.rfc59.component;


import it.phoops.geoserver.ols.OLS;
import it.phoops.geoserver.ols.OLSInfo;
import it.phoops.geoserver.ols.OLSService;
import it.phoops.geoserver.ols.OLSServiceProvider;
import it.phoops.geoserver.ols.OLSServiceProviderGUI;
import it.phoops.geoserver.ols.geocoding.rfc59.Algorithm;
import it.phoops.geoserver.ols.geocoding.rfc59.RFC59ServiceProvider;
import it.phoops.geoserver.ols.web.validator.ValidateCheckboxTab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.config.GeoServer;

public class RFC59Tab extends AbstractTab implements ValidateCheckboxTab{

	private String 						urlRFC59;
	private String 						timeoutRFC59;
	private String                                         activeRFC59;
	private int                                            codeAlgorithmSelected;
	private RFC59Panel 					instancePanel;
	private List<OLSAlgorithmType>				algorithmList = null;
	private OLSAlgorithmType 				selectedAlgorithm;
//	private RFC59Tab					instanceTabRFC59 = null;
	
	
	public class OLSAlgorithmType implements Serializable {
	    private Algorithm 			algorithm;
	    private String		        code;
	    private String 			descriptionKey;
	    private Component 			component;
	    
		
	    public OLSAlgorithmType() {}
		
	    public OLSAlgorithmType(Algorithm algorithm, String descriptionKey) {
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
	
	@Override
        public String getCheckboxValue() {
	    if(instancePanel != null)
                return this.instancePanel.getCheckBoxRFC59().getModelObject().toString();
	    return this.getActiveRFC59();
        }
    
        @Override
        public void setChecckboxValue(String value) {
            if(instancePanel != null){
                instancePanel.setActiveRFC59(value);
                instancePanel.getCheckBoxRFC59().setModelObject(Boolean.parseBoolean(value));
            }else{
                setActiveRFC59(value);
            }
              
        }

        public String getAlgorithmDescriptionKey(Algorithm algorithm){
            if(algorithm == Algorithm.FUZZY_QUERIES){
                return "OLSAlgorithmType.fuzzy";
            }else if(algorithm == Algorithm.RADIX){
                return "OLSAlgorithmType.radix";
            }else{
                return "OLSAlgorithmType.term";
            }
        }
	
    public int getCodeAlgorithmSelected() {
        return codeAlgorithmSelected;
    }



    public void setCodeAlgorithmSelected(int codeAlgorithmSelected) {
        this.codeAlgorithmSelected = codeAlgorithmSelected;
    }

    public RFC59Tab(IModel<String> title) {
		super(title);
		algorithmList = new ArrayList<OLSAlgorithmType>();
		algorithmList.add(new OLSAlgorithmType(Algorithm.TERM_QUERIES, "OLSAlgorithmType.term"));//1
		algorithmList.add(new OLSAlgorithmType(Algorithm.FUZZY_QUERIES, "OLSAlgorithmType.fuzzy"));//2
		algorithmList.add(new OLSAlgorithmType(Algorithm.RADIX, "OLSAlgorithmType.radix"));//3
	}
	
	@Override
	public Panel getPanel(String panelId) {
//	        if(instancePanel == null)
	        instancePanel = new RFC59Panel(panelId);
		instancePanel.setActiveRFC59(activeRFC59);
		instancePanel.getCheckBoxRFC59().setModelObject(Boolean.parseBoolean(activeRFC59));
		instancePanel.setUrlRFC59(urlRFC59);
		instancePanel.setTimeoutRFC59(timeoutRFC59);
		instancePanel.setAlgorithmList(this.algorithmList);
		instancePanel.add(new AlgorithmDropDownChoise("algorithm", new PropertyModel<OLSAlgorithmType>(this, "selectedAlgorithm"), algorithmList));
		instancePanel.setSelectedAlgorithm(this.algorithmList.get(getCodeAlgorithmSelected()-1));
		
		return instancePanel;
	}
	
	private static class RFC59Panel extends Panel{
        	private String 					urlRFC59;
        	private String 					timeoutRFC59;
        	private String                                 activeRFC59;
        	private OLSAlgorithmType			selectedAlgorithm;
        	private CheckBox                               checkBoxRFC59;
        	private List<OLSAlgorithmType>			algorithmList = null;
        	
        	
        	public RFC59Panel(String id){
        	    super(id);
        		
        	    checkBoxRFC59 = new CheckBox("checkboxRFC59", Model.of(Boolean.FALSE)){
        		    
        	        @Override
        	        protected boolean wantOnSelectionChangedNotifications() {
        	            return true;
        	        }
            		    
        	        @Override
        	        public void onSelectionChanged() {
        	            super.onSelectionChanged();
        	            Boolean value = this.getModelObject();
        	            activeOnlyOneCheck(value);
        	            
        	            setActiveRFC59(this.getModelObject().toString());
        	        }    
        	        
        	        private void activeOnlyOneCheck(Boolean value){
                            OLS ols = OLS.get();
                            GeoServer gs = ols.getGeoServer();
                            OLSInfo olsInfo = gs.getService(OLSInfo.class);
                            List<OLSServiceProvider> serviceProvider = olsInfo.getServiceProvider();
                            
                            for (OLSServiceProvider provider : serviceProvider) {
                                if(provider.getServiceType() == OLSService.GEOCODING
                                        && !(provider instanceof RFC59ServiceProvider)
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
        	    add(checkBoxRFC59);
        	    add(new TextField("urlRFC59",new PropertyModel(this,"urlRFC59")));
        	    add(new TextField("timeoutRFC59",new PropertyModel(this,"timeoutRFC59")));
        	}
    
        	public String getUrlRFC59() {
        	    return urlRFC59;
        	}
        
        	public void setUrlRFC59(String urlRFC59) {
        	    this.urlRFC59 = urlRFC59;
        	}
        
        	public String getTimeoutRFC59() {
        	    return timeoutRFC59;
        	}
    
        	public void setTimeoutRFC59(String timeoutRFC59) {
        	    this.timeoutRFC59 = timeoutRFC59;
        	}
    
        	public List<OLSAlgorithmType> getAlgorithmList() {
        	    return algorithmList;
        	}
    
        	public void setAlgorithmList(List<OLSAlgorithmType> algorithmList) {
        	    this.algorithmList = algorithmList;
        	}
    
        	public OLSAlgorithmType getSelectedAlgorithm() {
        	    return selectedAlgorithm;
        	}
    
        	public void setSelectedAlgorithm(OLSAlgorithmType selectedAlgorithm) {
        	    this.selectedAlgorithm = selectedAlgorithm;
        	}
    
        	public String getActiveRFC59() {
        	    return activeRFC59;
        	}
            
        	public void setActiveRFC59(String activeRFC59) {
        	    this.activeRFC59 = activeRFC59;
        	}
    
        	public CheckBox getCheckBoxRFC59() {
        	    return checkBoxRFC59;
        	}
    
        	public void setCheckBoxRFC59(CheckBox checkBoxRFC59) {
        	    this.checkBoxRFC59 = checkBoxRFC59;
        	}
	}

	public String getUrlRFC59() {
	    if(instancePanel != null)
	        return instancePanel.getUrlRFC59();
	    return urlRFC59;
	}

	public void setUrlRFC59(String urlRFC59) {
	    if(instancePanel != null){
	        instancePanel.setUrlRFC59(urlRFC59);
	    }
	    this.urlRFC59 = urlRFC59;
	}

	public String getTimeoutRFC59() {
	    if(instancePanel != null)
	        return instancePanel.getTimeoutRFC59();
	    return timeoutRFC59;
	}

	public void setTimeoutRFC59(String timeoutRFC59) {
	    if(instancePanel != null){
	        instancePanel.setTimeoutRFC59(timeoutRFC59);
	    }
	    this.timeoutRFC59 = timeoutRFC59;
	}

	public OLSAlgorithmType getSelectedAlgorithm() {
	    if(instancePanel != null){
	        return instancePanel.getSelectedAlgorithm();
	    }
	    return selectedAlgorithm;
	}
	
	public String getActiveRFC59() {
	    if(instancePanel != null){
	        return instancePanel.getActiveRFC59();
	    }
	    return activeRFC59;
        }

	public void setActiveRFC59(String activeRFC59) {
	    if(instancePanel != null){
	        instancePanel.setActiveRFC59(activeRFC59);
	    }
            this.activeRFC59 = activeRFC59;
        }

	public OLSAlgorithmType createOLSAlgorithmType(Algorithm algorithm, String descKey){
	    return new OLSAlgorithmType(algorithm, descKey);
	}

	public void setSelectedAlgorithm(OLSAlgorithmType selectedAlgorithm) {
	    if(instancePanel != null){
	        instancePanel.setSelectedAlgorithm(selectedAlgorithm);
	    }
	    this.selectedAlgorithm = selectedAlgorithm;
	}
}
