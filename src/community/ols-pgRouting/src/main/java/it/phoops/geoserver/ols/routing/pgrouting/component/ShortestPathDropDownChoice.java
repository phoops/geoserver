package it.phoops.geoserver.ols.routing.pgrouting.component;

import it.phoops.geoserver.ols.routing.pgrouting.component.PgRoutingTab.ShortestPathAlgorithmType;

import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;

public class ShortestPathDropDownChoice extends DropDownChoice<ShortestPathAlgorithmType>{
    private ShortestPathAlgorithmType selectedAlgorithm;
    
    public ShortestPathDropDownChoice(String id, PropertyModel<ShortestPathAlgorithmType> model, List<ShortestPathAlgorithmType> displayData) {
                    super(id,model,displayData);
    }
    
    @Override
    protected void onSelectionChanged(ShortestPathAlgorithmType newSelection) {
            super.onSelectionChanged(newSelection);
            setSelectedAlgorithm(newSelection);
            System.out.println("Shortest Path Algorithm: "+newSelection.toString());
    }
    
    @Override
    protected boolean wantOnSelectionChangedNotifications() {
            return true;
    }
    
    public ShortestPathAlgorithmType getSelectedAlgorithm() {
            return selectedAlgorithm;
    }
    
    public void setSelectedAlgorithm(ShortestPathAlgorithmType selectedAlgorithm) {
            this.selectedAlgorithm = selectedAlgorithm;
    }
}
