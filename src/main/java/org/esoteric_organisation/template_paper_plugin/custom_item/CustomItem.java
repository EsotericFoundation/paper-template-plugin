package org.esoteric_organisation.template_paper_plugin.custom_item;

public enum CustomItem {
  ;

  public static boolean isEnabled() {
    return values().length != 0;
  }
}