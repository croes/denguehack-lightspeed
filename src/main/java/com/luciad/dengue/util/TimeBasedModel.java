package com.luciad.dengue.util;

import com.luciad.model.ILcdModel;

/**
 * @author Thomas De Bodt
 */
public interface TimeBasedModel extends ILcdModel {

  void setTime(long aTime);

  long getTime();

  long getDataTime();

}
