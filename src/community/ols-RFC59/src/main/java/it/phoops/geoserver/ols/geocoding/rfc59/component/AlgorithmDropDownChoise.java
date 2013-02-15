package it.phoops.geoserver.ols.geocoding.rfc59.component;

import it.phoops.geoserver.ols.geocoding.rfc59.component.RFC59Tab.OLSAlgorithmType;

import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;

public class AlgorithmDropDownChoise extends DropDownChoice<OLSAlgorithmType>{
	private OLSAlgorithmType selectedAlgorithm;

	public AlgorithmDropDownChoise(String id, PropertyModel<OLSAlgorithmType> model, List<OLSAlgorithmType> displayData) {
			super(id,model,displayData);
	}
	@Override
	protected void onSelectionChanged(OLSAlgorithmType newSelection) {
		super.onSelectionChanged(newSelection);
		setSelectedAlgorithm(newSelection);
		System.out.println("Algoritmo Selezionato: "+newSelection.toString());
		System.out.println("Tipop Algoritmo Selezionato: "+newSelection.getService().getCode());
	}
	
	@Override
	protected boolean wantOnSelectionChangedNotifications() {
		return true;
	}
	public OLSAlgorithmType getSelectedAlgorithm() {
		return selectedAlgorithm;
	}
	public void setSelectedAlgorithm(OLSAlgorithmType selectedAlgorithm) {
		this.selectedAlgorithm = selectedAlgorithm;
	}

}

