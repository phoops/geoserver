package it.phoops.geoserver.ols.reverse.solr.component;


import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

public class SOLRTabReverse extends AbstractTab{
	private String 					urlSOLRReverse;
	private String          		activeSOLRReverse;
	private SOLRReversePanel 		instancePanel;
	
	public String getUrlSOLRReverse() {
		if(instancePanel != null)
				return instancePanel.getUrlSOLRReverse();
		return urlSOLRReverse;
	}

	public void setUrlSOLRReverse(String urlSOLRReverse) {
		if(instancePanel != null)
			instancePanel.setUrlSOLRReverse(urlSOLRReverse);
		this.urlSOLRReverse = urlSOLRReverse;
	}

	public String getActiveSOLRReverse() {
		if(instancePanel != null)
			return instancePanel.getActiveSOLRReverse();
		return activeSOLRReverse;
	}

	public void setActiveSOLRReverse(String activeSOLRReverse) {
		if(instancePanel != null)
			instancePanel.setActiveSOLRReverse(activeSOLRReverse);
		this.activeSOLRReverse = activeSOLRReverse;
	}

	public SOLRReversePanel getInstancePanel() {
		return instancePanel;
	}

	public void setInstancePanel(SOLRReversePanel instancePanel) {
		this.instancePanel = instancePanel;
	}

	public SOLRTabReverse(IModel<String> title) {
		super(title);
	}

	@Override
	public Panel getPanel(String panelId) {
		if(instancePanel == null)
	        instancePanel = new SOLRReversePanel(panelId);
	    instancePanel.setActiveSOLRReverse(activeSOLRReverse);
	    instancePanel.getCheckboxSOLRReverse().setModelObject(Boolean.parseBoolean(activeSOLRReverse));
	    instancePanel.setUrlSOLRReverse(urlSOLRReverse);
	    return instancePanel;
	}
	
	private static class SOLRReversePanel extends Panel{
	    private String             urlSOLRReverse;
	    private String     		activeSOLRReverse;
	    private CheckBox   		checkboxSOLRReverse;
	
	    
	    public SOLRReversePanel(String id){
	        super(id);
	        checkboxSOLRReverse = new CheckBox("checkboxSOLRReverse", Model.of(Boolean.FALSE)){
                    
                    @Override
                    protected boolean wantOnSelectionChangedNotifications() {
                        return true;
                    }
                        
                    @Override
                    public void onSelectionChanged() {
                        super.onSelectionChanged();
                        setActiveSOLRReverse(this.getModelObject().toString());
                    }    
                };
                add(checkboxSOLRReverse);
	        add(new TextField("urlSOLRReverse",new PropertyModel(this,"urlSOLRReverse")));
    	    }
	    
		    public String getUrlSOLRReverse() {
		        return urlSOLRReverse;
		    }
	
		    public void setUrlSOLRReverse(String urlSOLRReverse) {
		        this.urlSOLRReverse = urlSOLRReverse;
		    }

            public String getActiveSOLRReverse() {
                return activeSOLRReverse;
            }
    
            public void setActiveSOLRReverse(String activeSOLRReverse) {
                this.activeSOLRReverse = activeSOLRReverse;
            }
    
            public CheckBox getCheckboxSOLRReverse() {
                return checkboxSOLRReverse;
            }
    
            public void setCheckboxSOLRReverse(CheckBox checkboxSOLRReverse) {
                this.checkboxSOLRReverse = checkboxSOLRReverse;
            }
	}
	

}
