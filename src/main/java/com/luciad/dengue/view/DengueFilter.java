package com.luciad.dengue.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomc on 26/11/2016.
 */
public class DengueFilter {

  private List<String> parameterNames;

  public DengueFilter() {
    parameterNames =  new ArrayList<>();
    parameterNames.add("Temperature");
    parameterNames.add("Humidity");
    parameterNames.add("Precipitation");
    parameterNames.add("Livestock");

//    parameterNames.add("Population");

//    parameterNames.add("Elevation");
    // population growth
    // urbanization
    // lack of sanitation
    // increased long-distance travel
    // ineffective mosquito control
    // and increased reporting capacity
  }

  public List<String> getParameterNames() {
    return parameterNames;
  }

  public void updateFilter(String aParameterName, boolean selected, double min, double max) {

  }
}
