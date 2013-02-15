package it.phoops.geoserver.ols.web.validator;

import org.apache.wicket.extensions.markup.html.tabs.ITab;

public interface ValidateCheckboxTab extends ITab{
    public String getCheckboxValue();
    public void setChecckboxValue(String value);
}
