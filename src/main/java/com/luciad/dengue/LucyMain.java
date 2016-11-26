package com.luciad.dengue;

import samples.lucy.frontend.mapcentric.MapCentricFrontendMain;

/**
 * Created by tomc on 25/11/2016.
 */
public class LucyMain {

  public static void main(String[] args) {
    String[] new_args = new String[args.length + 2];
    System.arraycopy(args, 0, new_args, 0, args.length);
    new_args[new_args.length - 2] = "-addons";
    new_args[new_args.length - 1] = "lucy/addons_dengue.xml";
    MapCentricFrontendMain.main(new_args);
  }
}
